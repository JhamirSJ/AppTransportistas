package com.example.apptransportistas.sincronizardata

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.apptransportistas.R
import com.example.apptransportistas.data.local.DatabaseHelper
import com.google.android.material.button.MaterialButton
import apptransportistaspb.DespachoRequest
import apptransportistaspb.AppTransportistasServiceGrpc
import apptransportistaspb.EntregaResponse
import apptransportistaspb.Guia
import apptransportistaspb.Producto
import apptransportistaspb.PruebaEntrega
import apptransportistaspb.PruebaEntregaResponse
import apptransportistaspb.Ubicacion
import apptransportistaspb.TrackingResponse
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.concurrent.CountDownLatch

class SincronizarDataActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private val grpcIp = "192.168.11.201"
    private var sincronizacionAutomaticaJob: Job? = null
    private val intervaloSincronizacion = 20_000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sincronizar_data)

        dbHelper = DatabaseHelper(this)

        val btnEnviarEntregas = findViewById<MaterialButton>(R.id.btnEnviarEntregas)
        val btnObtenerGuias = findViewById<MaterialButton>(R.id.btnObtenerGuias)

        btnEnviarEntregas.setOnClickListener {
            enviarEntregas()
            enviarPruebasEntrega()
            enviarTracking()
        }
        btnObtenerGuias.setOnClickListener { obtenerGuias() }

        iniciarSincronizacionAutomatica()
    }

    private fun obtenerGuias() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = dbHelper.writableDatabase

            val channel = ManagedChannelBuilder.forAddress(
                grpcIp, 50051
            ).usePlaintext().build()

            val stub = AppTransportistasServiceGrpc.newBlockingStub(channel)

            try {
                val request = DespachoRequest.newBuilder().build()
                val responseStream = stub.obtenerDespachos(request)

                var cantidad = 0

                for (guia in responseStream) {
                    Log.d("gRPC", "Recibida guía del backend: ${guia.numero}")

                    val cursor = db.rawQuery(
                        "SELECT id FROM guia WHERE numero = ?", arrayOf(guia.numero)
                    )

                    if (!cursor.moveToFirst()) {
                        Log.d("gRPC", "Insertando guía ${guia.numero} en SQLite")

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

    private fun enviarEntregas() {
        lifecycleScope.launch(Dispatchers.IO) {

            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT * FROM guia WHERE entregada = 1", null
            )

            // Si no hay resultados, mostrar Toast
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

            // PREPARAR CONEXIÓN gRPC
            val channel = ManagedChannelBuilder
                .forAddress(grpcIp, 50051)
                .usePlaintext()
                .build()

            val stub = AppTransportistasServiceGrpc.newStub(channel)

            // PREAPARAR RESPUESTA DEL SERVIDOR
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

            // Recorrer guías y enviar
            while (cursor.moveToNext()) {
                val guiaId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))

                val guia = Guia.newBuilder()
                    .setNumero(cursor.getString(cursor.getColumnIndexOrThrow("numero")))
                    .setFecha(fecha)
                    .setCodigoCliente(cursor.getString(cursor.getColumnIndexOrThrow("codigo_cliente")))
                    .setNombreCliente(cursor.getString(cursor.getColumnIndexOrThrow("nombre_cliente")))
                    .setNroComprobante(cursor.getString(cursor.getColumnIndexOrThrow("nro_comprobante")))
                    .setImporteXCobrar(cursor.getDouble(cursor.getColumnIndexOrThrow("importe_x_cobrar")))
                    .setMontoCobrado(cursor.getDouble(cursor.getColumnIndexOrThrow("monto_cobrado")))
                    .setEntregada(true)

                // Productos a la guía
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

                // Enviar la guía
                try {
                    requestObserver.onNext(guia.build())
                } catch (e: Exception) {
                    Log.e("gRPC", "Error enviando guía", e)
                }
            }

            requestObserver.onCompleted()
            cursor.close()
            latch.await() // Esperamos a que el servidor responda antes de continuar
            channel.shutdownNow()

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@SincronizarDataActivity, "Entregas enviadas con éxito", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun enviarPruebasEntrega() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val pruebasSincronizadas = mutableListOf<Long>()
            val cursor = db.rawQuery(
                """
            SELECT pe.*, g.numero 
            FROM prueba_entrega pe 
            JOIN guia g ON g.id = pe.guia_id 
            WHERE g.entregada = 1
            """.trimIndent(), null
            )

            if (cursor.count == 0) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SincronizarDataActivity,
                        "No hay pruebas por enviar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                cursor.close()
                return@launch
            }

            val channel = ManagedChannelBuilder
                .forAddress(grpcIp, 50051)
                .usePlaintext()
                .build()
            val stub = AppTransportistasServiceGrpc.newStub(channel)
            val latch = CountDownLatch(1)

            val requestObserver =
                stub.enviarPruebasEntrega(object : StreamObserver<PruebaEntregaResponse> {
                    override fun onNext(value: PruebaEntregaResponse) {
                        Log.d("gRPC", "Servidor respondió: ${value.mensaje} (${value.totalRegistradas})")
                    }

                    override fun onError(t: Throwable) {
                        Log.e("gRPC", "Error al enviar pruebas", t)
                        latch.countDown()
                    }

                    override fun onCompleted() {
                        latch.countDown()
                    }
                })

            while (cursor.moveToNext()) {
                val numeroGuia = cursor.getString(cursor.getColumnIndexOrThrow("numero"))
                val fechaRegistro = cursor.getString(cursor.getColumnIndexOrThrow("fecha_registro"))
                val firmaPath = cursor.getString(cursor.getColumnIndexOrThrow("firma_path")) ?: ""
                val imagenPath = cursor.getString(cursor.getColumnIndexOrThrow("imagen_path")) ?: ""
                val pruebaId = cursor.getLong(cursor.getColumnIndexOrThrow("id"))

                // Leer bytes de archivos desde las rutas
                val firmaBytes = try {
                    if (firmaPath.isNotEmpty()) {
                        val uri = firmaPath.toUri()
                        contentResolver.openInputStream(uri)?.readBytes() ?: ByteArray(0)
                    } else ByteArray(0)
                } catch (e: Exception) {
                    Log.e("gRPC", "Error leyendo firma desde URI: $firmaPath", e)
                    ByteArray(0)
                }

                val imagenBytes = try {
                    if (imagenPath.isNotEmpty()) {
                        val uri = imagenPath.toUri()
                        contentResolver.openInputStream(uri)?.readBytes() ?: ByteArray(0)
                    } else ByteArray(0)
                } catch (e: Exception) {
                    Log.e("gRPC", "Error leyendo imagen desde URI: $imagenPath", e)
                    ByteArray(0)
                }

                val prueba = PruebaEntrega.newBuilder()
                    .setNumeroGuia(numeroGuia)
                    .setFechaRegistro(fechaRegistro)

                if (firmaBytes.isNotEmpty()) prueba.setFirma(ByteString.copyFrom(firmaBytes))
                if (imagenBytes.isNotEmpty()) prueba.setImagen(ByteString.copyFrom(imagenBytes))

                try {
                    requestObserver.onNext(prueba.build())
                    pruebasSincronizadas.add(pruebaId)
                } catch (e: Exception) {
                    Log.e("gRPC", "Error enviando prueba", e)
                }
            }

            requestObserver.onCompleted()
            cursor.close()
            latch.await()
            channel.shutdownNow()

            // Marcar como sincronizado
            val dbWritable = dbHelper.writableDatabase
            for (id in pruebasSincronizadas) {
                dbWritable.execSQL(
                    "UPDATE prueba_entrega SET sincronizado = 1 WHERE id = ?", arrayOf(id)
                )
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@SincronizarDataActivity,
                    "Pruebas enviadas correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun enviarTracking() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val trackingSincronizado = mutableListOf<Long>()

            // Seleccionamos los registros de tracking que no se han sincronizado
            val cursor = db.rawQuery(
                "SELECT * FROM tracking WHERE sincronizado = 0", null
            )

            if (cursor.count == 0) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SincronizarDataActivity,
                        "No hay tracking por enviar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                cursor.close()
                return@launch
            }

            val channel = ManagedChannelBuilder.forAddress(grpcIp, 50051)
                .usePlaintext()
                .build()
            val stub = AppTransportistasServiceGrpc.newStub(channel)
            val latch = CountDownLatch(1)

            val requestObserver = stub.enviarTracking(object : StreamObserver<TrackingResponse> {
                override fun onNext(value: TrackingResponse) {
                    Log.d("gRPC", "Servidor respondió: ${value.mensaje} (${value.totalRegistrados})")
                }

                override fun onError(t: Throwable) {
                    Log.e("gRPC", "Error enviando tracking", t)
                    latch.countDown()
                }

                override fun onCompleted() {
                    latch.countDown()
                }
            })

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val deviceId = cursor.getString(cursor.getColumnIndexOrThrow("device_id"))
                val lat = cursor.getDouble(cursor.getColumnIndexOrThrow("latitud"))
                val lng = cursor.getDouble(cursor.getColumnIndexOrThrow("longitud"))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora"))

                val ubicacion = Ubicacion.newBuilder()
                    .setDeviceId(deviceId)
                    .setLatitud(lat)
                    .setLongitud(lng)
                    .setFechaHora(fecha)
                    .build()

                try {
                    requestObserver.onNext(ubicacion)
                    trackingSincronizado.add(id)
                } catch (e: Exception) {
                    Log.e("gRPC", "Error enviando ubicación", e)
                }
            }

            requestObserver.onCompleted()
            cursor.close()
            latch.await()
            channel.shutdownNow()

            // Marcar como sincronizado
            val dbWritable = dbHelper.writableDatabase
            for (id in trackingSincronizado) {
                dbWritable.execSQL(
                    "UPDATE tracking SET sincronizado = 1 WHERE id = ?", arrayOf(id)
                )
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@SincronizarDataActivity,
                    "Tracking enviado correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun iniciarSincronizacionAutomatica() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT 1 FROM guia WHERE entregada = 1 LIMIT 1", null)

            val hayGuiasEntregadas = cursor.moveToFirst()
            cursor.close()

            if (!hayGuiasEntregadas) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SincronizarDataActivity,
                        "No hay guías entregadas para sincronizar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }

            // Solo iniciamos la sincronización automática si hay guías
            sincronizacionAutomaticaJob?.cancel()
            sincronizacionAutomaticaJob = lifecycleScope.launch {
                while (isActive) {
                    enviarEntregas()
                    enviarPruebasEntrega()
                    enviarTracking()
                    delay(intervaloSincronizacion)
                }
            }
        }
    }

    override fun onDestroy() {
        sincronizacionAutomaticaJob?.cancel()
        super.onDestroy()
    }
}