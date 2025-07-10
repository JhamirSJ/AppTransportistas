package com.example.apptransportistas.sincronizardata

import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.apptransportistas.R
import com.example.apptransportistas.data.local.DatabaseHelper
import com.google.android.material.button.MaterialButton
import apptransportistaspb.DespachoRequest
import apptransportistaspb.AppTransportistasServiceGrpc
import apptransportistaspb.Deposito
import apptransportistaspb.DepositoResponse
import apptransportistaspb.EntregaResponse
import apptransportistaspb.Guia
import apptransportistaspb.Producto
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.grpc.stub.StreamObserver
import java.util.Locale
import java.util.concurrent.CountDownLatch


class SincronizarDataActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sincronizar_data)

        dbHelper = DatabaseHelper(this)

        val btnEnviarEntregas = findViewById<MaterialButton>(R.id.btnEnviarEntregas)
        btnEnviarEntregas.setOnClickListener { enviarEntregas() }
        val btnObtenerGuias = findViewById<MaterialButton>(R.id.btnObtenerGuias)
        btnObtenerGuias.setOnClickListener { obtenerGuias() }
        val btnEnviarDepositos = findViewById<MaterialButton>(R.id.btnEnviarDepositos)
        btnEnviarDepositos.setOnClickListener { enviarDepositos() }
    }

    private fun enviarEntregas() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT * FROM guia WHERE entregada = 1", null
            )

            if (cursor.count == 0) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SincronizarDataActivity,
                        "No hay entregas por sincronizar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                cursor.close()
                return@launch
            }

            // Establecer canal gRPC
            val channel =
                ManagedChannelBuilder.forAddress("192.168.10.159", 50051).usePlaintext().build()

            val stub = AppTransportistasServiceGrpc.newStub(channel)

            val latch = CountDownLatch(1) // Esperará hasta que el servidor responda

            val requestObserver = stub.enviarEntregas(object : StreamObserver<EntregaResponse> {
                override fun onNext(value: EntregaResponse) {
                    Log.d(
                        "gRPC", "Servidor respondió: ${value.mensaje} (${value.totalRegistradas})"
                    )
                }

                override fun onError(t: Throwable) {
                    Log.e("gRPC", "Error al enviar entregas", t)
                    latch.countDown()
                }

                override fun onCompleted() {
                    latch.countDown()
                }
            })

            // Recorremos las guías y las enviamos
            while (cursor.moveToNext()) {
                val guiaId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val fechaOriginal = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
                val fechaMysql = fechaOriginal.takeWhile { it != 'T' }

                val guia = Guia.newBuilder()
                    .setNumero(cursor.getString(cursor.getColumnIndexOrThrow("numero")))
                    .setFecha(fechaMysql)
                    .setCodigoCliente(cursor.getString(cursor.getColumnIndexOrThrow("codigo_cliente")))
                    .setNombreCliente(cursor.getString(cursor.getColumnIndexOrThrow("nombre_cliente")))
                    .setNroComprobante(cursor.getString(cursor.getColumnIndexOrThrow("nro_comprobante")))
                    .setImporteXCobrar(cursor.getDouble(cursor.getColumnIndexOrThrow("importe_x_cobrar")))
                    .setMontoCobrado(cursor.getDouble(cursor.getColumnIndexOrThrow("monto_cobrado")))
                    .setEntregada(true)


                // Productos
                val prodCursor = db.rawQuery(
                    "SELECT * FROM producto WHERE id_guia = ?", arrayOf(guiaId.toString())
                )

                while (prodCursor.moveToNext()) {
                    val producto = Producto.newBuilder()
                        .setNombre(prodCursor.getString(prodCursor.getColumnIndexOrThrow("nombre")))
                        .setCantidad(prodCursor.getInt(prodCursor.getColumnIndexOrThrow("cantidad")))
                        .build()
                    guia.addProductos(producto)
                }
                prodCursor.close()

                try {
                    requestObserver.onNext(guia.build())
                } catch (e: Exception) {
                    Log.e("gRPC", "Error enviando guía", e)
                }
            }

            requestObserver.onCompleted()
            cursor.close()

            // Esperamos a que el servidor responda antes de continuar
            latch.await()

            channel.shutdownNow()

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@SincronizarDataActivity, "Entregas enviadas con éxito", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun obtenerGuias() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = dbHelper.writableDatabase

            val channel = ManagedChannelBuilder.forAddress(
                    "192.168.10.159",
                    50051
                ) // o IP local si usas un celular
                .usePlaintext().build()

            val stub = AppTransportistasServiceGrpc.newBlockingStub(channel)

            try {
                val request = DespachoRequest.newBuilder().build()
                val responseStream = stub.obtenerDespachos(request)

                var cantidad = 0

                for (guia in responseStream) {
                    Log.d("gRPC", "📥 Recibida guía del backend: ${guia.numero}")

                    val cursor = db.rawQuery(
                        "SELECT id FROM guia WHERE numero = ?", arrayOf(guia.numero)
                    )

                    if (!cursor.moveToFirst()) {
                        Log.d("gRPC", "📦 Insertando guía ${guia.numero} en SQLite")

                        // Insertar guía
                        db.execSQL(
                            """
                                INSERT INTO guia (numero, fecha, codigo_cliente, nombre_cliente, nro_comprobante,
                                                   importe_x_cobrar, monto_cobrado, entregada)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            """.trimIndent(), arrayOf(
                                guia.numero,
                                guia.fecha,
                                guia.codigoCliente,
                                guia.nombreCliente,
                                guia.nroComprobante,
                                guia.importeXCobrar,
                                guia.montoCobrado,
                                if (guia.entregada) 1 else 0
                            )
                        )

                        // Obtener ID guía
                        val idCursor = db.rawQuery(
                            "SELECT id FROM guia WHERE numero = ?", arrayOf(guia.numero)
                        )
                        idCursor.moveToFirst()
                        val idGuia = idCursor.getInt(0)
                        idCursor.close()

                        // Insertar productos
                        for (p in guia.productosList) {
                            db.execSQL(
                                """
                                    INSERT INTO producto (id_guia, nombre, cantidad)
                                    VALUES (?, ?, ?)
                                """.trimIndent(), arrayOf(
                                    idGuia, p.nombre, p.cantidad
                                )
                            )
                        }

                        cantidad++
                    } else {
                        Log.d("gRPC", "⚠️ Guía ${guia.numero} ya existe en SQLite, no se inserta")
                    }

                    cursor.close()
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SincronizarDataActivity,
                        "✅ $cantidad guías nuevas recibidas",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("gRPC", "Er   ror recibiendo guías", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SincronizarDataActivity, "❌ Error al obtener guías", Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                channel.shutdownNow()
            }
        }
    }

    private fun enviarDepositos() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM deposito WHERE sincronizado = 0", null)

            if (!cursor.moveToFirst()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SincronizarDataActivity,
                        "No hay depósitos por sincronizar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                cursor.close()
                return@launch
            }

            val channel =
                ManagedChannelBuilder.forAddress("192.168.10.159", 50051).usePlaintext().build()

            val stub = AppTransportistasServiceGrpc.newStub(channel)
            val latch = CountDownLatch(1)

            val requestObserver = stub.enviarDepositos(object : StreamObserver<DepositoResponse> {
                override fun onNext(value: DepositoResponse) {
                    Log.d(
                        "gRPC", "Servidor respondió: ${value.mensaje} (${value.totalRegistrados})"
                    )
                }

                override fun onError(t: Throwable) {
                    Log.e("gRPC", "Error al enviar depósitos", t)
                    latch.countDown()
                }

                override fun onCompleted() {
                    latch.countDown()
                }
            })

            val writableDb = dbHelper.writableDatabase

            do {
                val nroOperacion = cursor.getString(cursor.getColumnIndexOrThrow("nro_operacion"))
                val fechaCruda = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
                val idBanco = cursor.getInt(cursor.getColumnIndexOrThrow("id_banco"))
                val monto = cursor.getDouble(cursor.getColumnIndexOrThrow("monto"))
                val imagenPath = cursor.getString(cursor.getColumnIndexOrThrow("comprobante_path"))

                // Convertir fecha a formato MySQL
                val fecha = try {
                    val sdfIn = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                    val sdfOut = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    sdfOut.format(sdfIn.parse(fechaCruda)!!)
                } catch (e: Exception) {
                    Log.e("Fecha", "Error convirtiendo fecha: $fechaCruda", e)
                    fechaCruda
                }

                // Leer imagen
                val imageBytes = try {
                    val uri = Uri.parse(imagenPath)
                    val inputStream = contentResolver.openInputStream(uri)
                    inputStream?.readBytes() ?: ByteArray(0)
                } catch (e: Exception) {
                    Log.e("gRPC", "No se pudo leer imagen: $imagenPath", e)
                    ByteArray(0)
                }

                // Enviar depósito
                val deposito = Deposito.newBuilder().setNroOperacion(nroOperacion).setFecha(fecha)
                    .setIdBanco(idBanco).setMonto(monto)
                    .setComprobante(ByteString.copyFrom(imageBytes)).build()

                try {
                    requestObserver.onNext(deposito)
                    writableDb.execSQL(
                        "UPDATE deposito SET sincronizado = 1 WHERE nro_operacion = ?",
                        arrayOf(nroOperacion)
                    )
                } catch (e: Exception) {
                    Log.e("gRPC", "Error enviando depósito", e)
                }
            } while (cursor.moveToNext())

            requestObserver.onCompleted()
            cursor.close()
            latch.await()
            channel.shutdownNow()

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@SincronizarDataActivity, "Depósitos enviados con éxito", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}