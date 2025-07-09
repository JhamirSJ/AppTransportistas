package com.example.apptransportistas.menu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.apptransportistas.R
import com.example.apptransportistas.registrardeposito.RegDepositoActivity
import com.example.apptransportistas.guias.misguias.MisGuiasActivity
import com.example.apptransportistas.registrarentregaguias.RegGuiasActivity
import com.example.apptransportistas.sincronizardata.SincronizarDataActivity
import com.example.apptransportistas.ubicacion.TrackingService
import com.google.android.material.button.MaterialButton

class MenuActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnMisGuias = findViewById<MaterialButton>(R.id.btnMisGuias)
        val btnRegGuias = findViewById<MaterialButton>(R.id.btnRegGuias)
        val btnSyncData = findViewById<MaterialButton>(R.id.btnSyncData)
        val btnRegDeposito = findViewById<MaterialButton>(R.id.btnRegDeposito)

        checkLocationPermission()

        btnMisGuias.setOnClickListener { navigateToMisGuias() }
        btnRegGuias.setOnClickListener { navigateToRegGuias() }
        btnSyncData.setOnClickListener { navigateToSincronizarData() }
        btnRegDeposito.setOnClickListener { navigateToRegDeposito() }
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

    private fun navigateToRegDeposito() {
        val intent = Intent(this, RegDepositoActivity::class.java)
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
                LOCATION_PERMISSION_CODE
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

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTrackingService()
            } else {
                Toast.makeText(this, "El permiso de ubicación es obligatorio para usar la app", Toast.LENGTH_LONG).show()
            }
        }
    }

}