package com.example.apptransportistas.registrarentregaguias

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.apptransportistas.R
import com.example.apptransportistas.cobrarguias.CobrarGuiasActivity
import com.example.apptransportistas.seleccionarguia.SelecGuiasActivity
import com.example.apptransportistas.verproductos.VerProductosActivity
import com.google.android.material.button.MaterialButton
import java.util.Date
import java.util.Locale

class RegGuiasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg_guias)

        val btnSelecGuia = findViewById<MaterialButton>(R.id.btnSelecGuia)
        val btnVerProductos = findViewById<MaterialButton>(R.id.btnVerProductos)
        val btnCobrarGuia = findViewById<MaterialButton>(R.id.btnCobrarGuia)

        btnSelecGuia.setOnClickListener { navigateToSelecGuia() }
        btnVerProductos.setOnClickListener { navigateToVerProductos() }
        btnCobrarGuia.setOnClickListener { navigateToCobrarGuia() }


        FechaActual()
    }

    private fun FechaActual() {
        val tvFecha = findViewById<TextView>(R.id.tvFechaActual)
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        tvFecha.text = "FECHA (ACTUAL): $fechaActual"
    }

    private fun navigateToSelecGuia() {
        val intent = Intent(this, SelecGuiasActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToVerProductos() {
        val intent = Intent(this, VerProductosActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToCobrarGuia() {
        val intent = Intent(this, CobrarGuiasActivity::class.java)
        startActivity(intent)
    }

}