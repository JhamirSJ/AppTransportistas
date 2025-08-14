package com.example.apptransportistas.menu

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.apptransportistas.R
import com.example.apptransportistas.guias.misguias.MisGuiasActivity
import com.example.apptransportistas.registrarentregaguias.RegGuiasActivity
import com.example.apptransportistas.sincronizardata.SincronizarDataActivity
import com.example.apptransportistas.ubicacion.TrackingService
import com.google.android.material.button.MaterialButton
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import apptransportistaspb.AppTransportistasServiceGrpc
import apptransportistaspb.LoginRequest
import com.example.apptransportistas.settings.ConfigActivity
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File

class MenuActivity : AppCompatActivity() {

    private val locationPermissionCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnConfig = findViewById<ImageButton>(R.id.btnConfig)
        val btnMisGuias = findViewById<MaterialButton>(R.id.btnMisGuias)
        val btnRegGuias = findViewById<MaterialButton>(R.id.btnRegGuias)
        val btnSyncData = findViewById<MaterialButton>(R.id.btnSyncData)

        checkLocationPermission()
        crearArchivoConfiguracion()

        btnConfig.setOnClickListener { navigateToConfig() }
        btnMisGuias.setOnClickListener { navigateToMisGuias() }
        btnRegGuias.setOnClickListener { navigateToRegGuias() }
        btnSyncData.setOnClickListener { navigateToSincronizarData() }
    }

    private fun navigateToConfig() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_login_admin)
        dialog.setCancelable(true)

        val etUser = dialog.findViewById<EditText>(R.id.etUser)
        val etPassword = dialog.findViewById<EditText>(R.id.etPassword)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelar)
        val btnIngresar = dialog.findViewById<Button>(R.id.btnIngresar)

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnIngresar.setOnClickListener {
            val usuario = etUser.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                var channel: ManagedChannel? = null
                try {
                    channel = ManagedChannelBuilder
                        .forAddress("192.168.11.201", 50051)
                        .usePlaintext()
                        .build()

                    val stub = AppTransportistasServiceGrpc.newBlockingStub(channel)
                    val request = LoginRequest.newBuilder()
                        .setUsuario(usuario)
                        .setPassword(password)
                        .build()

                    val response = stub.loginAdmin(request)

                    withContext(Dispatchers.Main) {
                        if (response.success) {
                            dialog.dismiss()
                            startActivity(Intent(this@MenuActivity, ConfigActivity::class.java))
                        } else {
                            Toast.makeText(this@MenuActivity, response.mensaje, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MenuActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    channel?.shutdownNow()
                }
            }
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun navigateToMisGuias() {
        val intent = Intent(this, MisGuiasActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToRegGuias() {
        val intent = Intent(this, RegGuiasActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSincronizarData() {
        val intent = Intent(this, SincronizarDataActivity::class.java)
        startActivity(intent)
    }

    private fun startTrackingService() {
        val intent = Intent(this, TrackingService::class.java)
        startService(intent)
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        } else {
            startTrackingService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTrackingService()
            } else {
                Toast.makeText(this, "El permiso de ubicación es obligatorio para usar la app", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun crearArchivoConfiguracion() {
        val fileName = "config.xml"
        val file = File(filesDir, fileName)

        if (!file.exists()) {
            try {
                val xmlContent = """
                    <settings>
                        <setting 
                            seriebve="306" 
                            seriefac="" 
                            idprinter="docker-LoadB-9a9aeca7SJv1-1afec3f4d8d2c026.elb.us-west-2.amazonaws.com:50050" 
                            iduser="316" 
                            pricelevel="2" 
                            idsvr2="docker-loadb-tiok8eoojtpa-8be2fc82639faebf.elb.us-west-2.amazonaws.com:50051" 
                            codgui="6fb57252-d2bc-11ef-95ad-0068eb7639db"
                        />
                    </settings>
                """.trimIndent()

                file.writeText(xmlContent)
                Log.d("Config", "Archivo de configuración creado en ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e("Config", "Error creando archivo de configuración", e)
            }
        } else {
            Log.d("Config", "Archivo de configuración ya existe")
        }
    }

}