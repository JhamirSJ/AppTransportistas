package com.example.apptransportistas.cobrarguias

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.apptransportistas.R
import com.example.apptransportistas.menu.MenuActivity
import com.example.apptransportistas.registrarentregaguias.RegGuiasActivity
import com.google.android.material.button.MaterialButton

class CobrarGuiasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cobrar_guias)

        val btnCancelar= findViewById<MaterialButton>(R.id.btnCancelar)
        val btnCerrarDespacho = findViewById<MaterialButton>(R.id.btnCerrarDespacho)

        btnCancelar.setOnClickListener { navigateToRgGuias() }
        btnCerrarDespacho.setOnClickListener { navigateToMenu() }
    }

    private fun navigateToRgGuias() {
        val intent = Intent(this, RegGuiasActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }
}