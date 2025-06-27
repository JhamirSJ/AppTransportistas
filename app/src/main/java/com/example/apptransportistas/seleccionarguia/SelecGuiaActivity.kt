package com.example.apptransportistas.seleccionarguia

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apptransportistas.R

class SelecGuiaActivity : AppCompatActivity() {

    private lateinit var rvListaGuias: RecyclerView
    private lateinit var listaGuias: List<Guia>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selec_guias)

        rvListaGuias = findViewById(R.id.rvListaGuias)

        listaGuias = generarGuiasDePrueba()

        val adapter = GuiaAdapter(listaGuias) { guiaSeleccionada ->
            val intent = Intent()
            intent.putExtra("guiaSeleccionada", guiaSeleccionada) // Serializable o Parcelable
            setResult(RESULT_OK, intent)
            finish()
        }

        rvListaGuias.layoutManager = LinearLayoutManager(this)
        rvListaGuias.adapter = adapter
    }

    private fun generarGuiasDePrueba(): List<Guia> {
        return listOf(
            Guia("001", "2025-06-01", "COD001", "Juan Pérez", "FV001-0001", 150.0),
            Guia("002", "2025-06-02", "COD002", "Ana Torres", "FV001-0002", 210.5),
            Guia("003", "2025-06-03", "COD003", "Carlos Gómez", "FV001-0003", 89.9)
        )
    }
}