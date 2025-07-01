package com.example.apptransportistas.sincronizardata

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.apptransportistas.R
import com.example.apptransportistas.data.local.DatabaseHelper
import com.google.android.material.button.MaterialButton
import despachopb.DespachoRequest
import despachopb.DespachoServiceGrpc
import despachopb.EntregaResponse
import despachopb.Guia
import despachopb.Producto
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.grpc.stub.StreamObserver
import java.util.concurrent.CountDownLatch


class SincronizarDataActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sincronizar_data)

        dbHelper = DatabaseHelper(this)

        val btnEnviar = findViewById<MaterialButton>(R.id.btnEnviarEntregas)
        btnEnviar.setOnClickListener {
            enviarEntregas()
        }
        val btnObtener = findViewById<MaterialButton>(R.id.btnObtenerGuias)
        btnObtener.setOnClickListener {
            obtenerGuias()
        }
    }

    private fun enviarEntregas() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT * FROM guia WHERE entregada = 1",
                null
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
            val channel = ManagedChannelBuilder
                .forAddress("192.168.10.159", 50051)
                .usePlaintext()
                .build()

            val stub = DespachoServiceGrpc.newStub(channel)

            val latch = CountDownLatch(1) // Esperará hasta que el servidor responda

            val requestObserver = stub.enviarEntregas(object : StreamObserver<EntregaResponse> {
                override fun onNext(value: EntregaResponse) {
                    Log.d("gRPC", "Servidor respondió: ${value.mensaje} (${value.totalRegistradas})")
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
                val guia = Guia.newBuilder()
                    .setNumero(cursor.getString(cursor.getColumnIndexOrThrow("numero")))
                    .setFecha(cursor.getString(cursor.getColumnIndexOrThrow("fecha")))
                    .setCodigoCliente(cursor.getString(cursor.getColumnIndexOrThrow("codigo_cliente")))
                    .setNombreCliente(cursor.getString(cursor.getColumnIndexOrThrow("nombre_cliente")))
                    .setNroComprobante(cursor.getString(cursor.getColumnIndexOrThrow("nro_comprobante")))
                    .setImporteXCobrar(cursor.getDouble(cursor.getColumnIndexOrThrow("importe_x_cobrar")))
                    .setMontoCobrado(cursor.getDouble(cursor.getColumnIndexOrThrow("monto_cobrado")))
                    .setEntregada(true)

                // Productos
                val prodCursor = db.rawQuery(
                    "SELECT * FROM producto WHERE id_guia = ?",
                    arrayOf(guiaId.toString())
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
                    this@SincronizarDataActivity,
                    "Entregas enviadas con éxito",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun obtenerGuias() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = dbHelper.writableDatabase

            val channel = ManagedChannelBuilder
                .forAddress("192.168.10.159", 50051) // o IP local si usas un celular
                .usePlaintext()
                .build()

            val stub = DespachoServiceGrpc.newBlockingStub(channel)

            try {
                val request = DespachoRequest.newBuilder().build()
                val responseStream = stub.obtenerDespachos(request)

                var cantidad = 0

                for (guia in responseStream) {
                    Log.d("gRPC", "📥 Recibida guía del backend: ${guia.numero}")

                    val cursor = db.rawQuery(
                        "SELECT id FROM guia WHERE numero = ?",
                        arrayOf(guia.numero)
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
                            "SELECT id FROM guia WHERE numero = ?",
                            arrayOf(guia.numero)
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
                                    idGuia,
                                    p.nombre,
                                    p.cantidad
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
                        this@SincronizarDataActivity,
                        "❌ Error al obtener guías",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                channel.shutdownNow()
            }
        }
    }
}