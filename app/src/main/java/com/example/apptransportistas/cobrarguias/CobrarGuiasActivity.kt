package com.example.apptransportistas.cobrarguias

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.apptransportistas.R
import com.example.apptransportistas.data.local.DatabaseHelper
import com.example.apptransportistas.registrarentregaguias.RegGuiasActivity
import com.google.android.material.button.MaterialButton

class CobrarGuiasActivity : AppCompatActivity() {

    private lateinit var tvTotalXCobrar: TextView
    private lateinit var etMontoCobrado: EditText
    private lateinit var tvPendienteXCobrar: TextView
    private lateinit var dbHelper: DatabaseHelper

    private var importeXCobrar: Double = 0.0
    private var guiaId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cobrar_guias)

        dbHelper = DatabaseHelper(this)
        tvTotalXCobrar = findViewById(R.id.tvTotalXCobrar)
        etMontoCobrado = findViewById(R.id.etMontoCobrado)
        tvPendienteXCobrar = findViewById(R.id.tvPendienteXCobrar)
        val btnCerrarDespacho = findViewById<MaterialButton>(R.id.btnCerrarDespacho)
        val btnCancelar = findViewById<MaterialButton>(R.id.btnCancelar)

        importeXCobrar = intent.getDoubleExtra("importe_x_cobrar", 0.0)
        guiaId = intent.getLongExtra("guia_id", -1)

        tvTotalXCobrar.text = String.format("%.2f", importeXCobrar)

        etMontoCobrado.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val montoCobrado = s.toString().toDoubleOrNull() ?: 0.0
                val pendiente = importeXCobrar - montoCobrado
                tvPendienteXCobrar.text = String.format("%.2f", if (pendiente < 0) 0.0 else pendiente)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnCerrarDespacho.setOnClickListener { registrarDespachoYVolver() }

        btnCancelar.setOnClickListener { finish() }
    }

    private fun registrarDespachoYVolver() {
        val montoCobrado = etMontoCobrado.text.toString().toDoubleOrNull() ?: 0.0

        if (guiaId != -1L) {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("monto_cobrado", montoCobrado)
                put("entregada", 1)
            }
            db.update("guia", values, "id = ?", arrayOf(guiaId.toString()))
            db.close()
        }

        val intent = Intent(this, RegGuiasActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
