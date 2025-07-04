package com.example.apptransportistas.registrarentregaguias

import android.content.ContentValues
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.apptransportistas.R
import com.example.apptransportistas.data.local.DatabaseHelper
import com.example.apptransportistas.menu.MenuActivity
import com.example.apptransportistas.guias.Guia
import com.example.apptransportistas.guias.seleccionarguia.SelecGuiaActivity
import com.example.apptransportistas.verproductos.VerProductosActivity
import com.google.android.material.button.MaterialButton
import java.util.Date
import java.util.Locale

class RegGuiasActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var guiaSeleccionada: Guia? = null

    private var guiaId: Long = -1

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
                    guiaId = it.id
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

        val btnSelecGuia = findViewById<MaterialButton>(R.id.btnSelecGuia)
        val btnVerProductos = findViewById<MaterialButton>(R.id.btnVerProductos)
        val btnRegEntrega = findViewById<MaterialButton>(R.id.btnRegEntrega)

        btnSelecGuia.setOnClickListener { navigateToSelecGuia() }
        btnVerProductos.setOnClickListener { navigateToVerProductos() }
        btnRegEntrega.setOnClickListener {
            if (guiaId != -1L) {
                val db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put("entregada", 1)
                }
                db.update("guia", values, "id = ?", arrayOf(guiaId.toString()))
                db.close()
            }
            Toast.makeText(this, "✅ Guía entregada correctamente", Toast.LENGTH_SHORT).show()
            navigatetoMenu()
        }

        dbHelper = DatabaseHelper(this)

        FechaActual()
    }

    private fun FechaActual() {
        val tvFecha = findViewById<TextView>(R.id.tvFechaActual)
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        tvFecha.text = "HOY: $fechaActual"
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

    private fun navigatetoMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
    }
}