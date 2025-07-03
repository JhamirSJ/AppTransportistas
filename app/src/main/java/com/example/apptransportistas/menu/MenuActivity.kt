package com.example.apptransportistas.menu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.apptransportistas.R
import com.example.apptransportistas.registrardeposito.RegDepositoActivity
import com.example.apptransportistas.guias.misguias.MisGuiasActivity
import com.example.apptransportistas.registrarentregaguias.RegGuiasActivity
import com.example.apptransportistas.sincronizardata.SincronizarDataActivity
import com.google.android.material.button.MaterialButton

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnMisGuias = findViewById<MaterialButton>(R.id.btnMisGuias)
        val btnRegGuias = findViewById<MaterialButton>(R.id.btnRegGuias)
        val btnSyncData = findViewById<MaterialButton>(R.id.btnSyncData)
        val btnRegDeposito = findViewById<MaterialButton>(R.id.btnRegDeposito)

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
}