package com.example.apptransportistas.registrarentregaguias

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.apptransportistas.R
import com.example.apptransportistas.data.local.DatabaseHelper
import com.example.apptransportistas.guias.Guia
import com.example.apptransportistas.guias.seleccionarguia.SelecGuiaActivity
import com.example.apptransportistas.registrarentregaguias.adjuntarpruebaentrega.AdjuntarPruebaActivity
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
                val guia = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getSerializableExtra("guiaSeleccionada", Guia::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getSerializableExtra("guiaSeleccionada") as? Guia
                }

                guia?.let {
                    guiaSeleccionada = it
                    findViewById<TextView>(R.id.tvNroGuiaSelec).text = it.numero
                    findViewById<TextView>(R.id.tvFechaGuiaSelec).text = it.fecha
                    findViewById<TextView>(R.id.tvCodGuiaSelec).text = it.codigo
                    findViewById<TextView>(R.id.tvNombreGuiaSelec).text = it.nombre
                    findViewById<TextView>(R.id.tvNroComprobante).text = it.nroComprobante
                    findViewById<TextView>(R.id.tvImporteXCobrar).text =
                        String.format("%.2f", it.importeXCobrar)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg_guias)
        fechaActual()

        val btnSelecGuia = findViewById<MaterialButton>(R.id.btnSelecGuia)
        val btnVerProductos = findViewById<MaterialButton>(R.id.btnVerProductos)
        val btnContinuarReg = findViewById<MaterialButton>(R.id.btnContinuarReg)

        btnSelecGuia.setOnClickListener { navigateToSelecGuia() }
        btnVerProductos.setOnClickListener { navigateToVerProductos() }
        btnContinuarReg.setOnClickListener { navigateToAdjuntarPrueba() }

        dbHelper = DatabaseHelper(this)
    }

    private fun fechaActual() {
        val tvFecha = findViewById<TextView>(R.id.tvFechaActual)
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        tvFecha.text = "Hoy: $fechaActual"
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

    private fun navigateToAdjuntarPrueba() {
        val guiaId = guiaSeleccionada?.id ?: return
        val intent = Intent(this, AdjuntarPruebaActivity::class.java)
        intent.putExtra("guiaId", guiaId)
        startActivity(intent)
    }
}