package com.example.apptransportistas.registrarentregaguias

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.apptransportistas.R
import com.example.apptransportistas.cobrarguias.CobrarGuiasActivity
import com.example.apptransportistas.data.local.DatabaseHelper
import com.example.apptransportistas.menu.MenuActivity
import com.example.apptransportistas.seleccionarguia.Guia
import com.example.apptransportistas.seleccionarguia.SelecGuiaActivity
import com.example.apptransportistas.verproductos.VerProductosActivity
import com.google.android.material.button.MaterialButton
import java.util.Date
import java.util.Locale

class RegGuiasActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var guiaSeleccionada: Guia? = null

    private val guiaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val guia = result.data?.getSerializableExtra("guiaSeleccionada") as? Guia
                guia?.let {
                    guiaSeleccionada = it
                    findViewById<TextView>(R.id.tvNroGSeleccionada).text = it.numero
                    findViewById<TextView>(R.id.tvFechaGSeleccionada).text = it.fecha
                    findViewById<TextView>(R.id.tvCodGSeleccionada).text = it.codigo
                    findViewById<TextView>(R.id.tvNombreGSeleccionada).text = it.nombre
                    findViewById<TextView>(R.id.tvNroComprobante).text = it.nroComprobante
                    findViewById<TextView>(R.id.tvImporteXCobrar).text =
                        String.format("%.2f", it.importeXCobrar)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg_guias)

        val btnSelecGuia = findViewById<MaterialButton>(R.id.btnSelecGuia)
        val btnVerProductos = findViewById<MaterialButton>(R.id.btnVerProductos)
        val btnCobrarGuia = findViewById<MaterialButton>(R.id.btnCobrarGuia)
        val btnRegistrarEntrega = findViewById<MaterialButton>(R.id.btnRegistrarEntrega)

        btnSelecGuia.setOnClickListener { navigateToSelecGuia() }
        btnVerProductos.setOnClickListener { navigateToVerProductos() }
        btnCobrarGuia.setOnClickListener { navigateToCobrarGuia() }
        btnRegistrarEntrega.setOnClickListener { navigatetoMenu() }

        dbHelper = DatabaseHelper(this)

        FechaActual()
    }

    private fun FechaActual() {
        val tvFecha = findViewById<TextView>(R.id.tvFechaActual)
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        tvFecha.text = "FECHA (ACTUAL): $fechaActual"
    }

    private fun navigateToSelecGuia() {
        val intent = Intent(this, SelecGuiaActivity::class.java)
        guiaLauncher.launch(intent)
    }

    private fun navigateToVerProductos() {
        val guiaId = guiaSeleccionada?.id ?: return
        val intent = Intent(this, VerProductosActivity::class.java)
        intent.putExtra("guia_id", guiaId)
        startActivity(intent)
    }

    private fun navigateToCobrarGuia() {
        val tvImporte = findViewById<TextView>(R.id.tvImporteXCobrar)
        val importe = tvImporte.text.toString().toDoubleOrNull() ?: 0.0
        val guiaId = guiaSeleccionada?.id ?: return

        val intent = Intent(this, CobrarGuiasActivity::class.java)
        intent.putExtra("importe_x_cobrar", importe)
        intent.putExtra("guia_id", guiaId)
        startActivity(intent)
    }

    private fun navigatetoMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }
}