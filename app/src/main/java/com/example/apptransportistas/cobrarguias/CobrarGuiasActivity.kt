package com.example.apptransportistas.cobrarguias

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.apptransportistas.R
import com.example.apptransportistas.menu.MenuActivity
import com.google.android.material.button.MaterialButton

class CobrarGuiasActivity : AppCompatActivity() {

    private lateinit var tvTotalXCobrar: TextView
    private lateinit var etMontoCobrado: EditText
    private lateinit var tvPendienteXCobrar: TextView

    private var importeXCobrar: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cobrar_guias)

        tvTotalXCobrar = findViewById(R.id.tvTotalXCobrar)
        etMontoCobrado = findViewById(R.id.etMontoCobrado)
        tvPendienteXCobrar = findViewById(R.id.tvPendienteXCobrar)
        val btnCerrarDespacho = findViewById<MaterialButton>(R.id.btnCerrarDespacho)
        val btnCancelar = findViewById<MaterialButton>(R.id.btnCancelar)

        importeXCobrar = intent.getDoubleExtra("importe_x_cobrar", 0.0)
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

        btnCerrarDespacho.setOnClickListener { navigateToMenu() }

        btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
