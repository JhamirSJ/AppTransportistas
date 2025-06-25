package com.example.apptransportistas.menu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.apptransportistas.R
import com.example.apptransportistas.misguias.MisGuiasActivity
import com.example.apptransportistas.registrarentregaguias.RegGuiasActivity
import com.google.android.material.button.MaterialButton

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnMisGuias = findViewById<MaterialButton>(R.id.btnMisGuias)
        val btnRegGuias = findViewById<MaterialButton>(R.id.btnRgGuias)

        btnMisGuias.setOnClickListener { navigateToMisGuias() }
        btnRegGuias.setOnClickListener { navigateToRgGuias() }
    }

    private fun navigateToMisGuias() {
        val intent = Intent(this, MisGuiasActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToRgGuias() {
        val intent = Intent(this, RegGuiasActivity::class.java)
        startActivity(intent)
    }
}