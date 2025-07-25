package com.example.apptransportistas.guias.misguias

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.EditText
import android.text.TextWatcher
import android.text.Editable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.example.apptransportistas.R
import com.example.apptransportistas.data.local.DatabaseHelper
import com.example.apptransportistas.guias.Guia
import com.example.apptransportistas.guias.GuiaAdapter
import java.util.Date
import java.util.Locale

class MisGuiasActivity : AppCompatActivity() {

    private lateinit var rvListaGuias: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etBuscarMiGuia: EditText
    private lateinit var adapter: GuiaAdapter

    private lateinit var listaGuiasOriginal: List<Guia>
    private var filtroActual = "TODAS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_guias)

        dbHelper = DatabaseHelper(this)
        rvListaGuias = findViewById(R.id.rvGuias)
        etBuscarMiGuia = findViewById(R.id.etBuscarMiGuia)

        listaGuiasOriginal = obtenerGuiasDB()
        adapter = GuiaAdapter(listaGuiasOriginal.toMutableList()) {}
        rvListaGuias.layoutManager = LinearLayoutManager(this)
        rvListaGuias.adapter = adapter

        FechaActual()
        initSearchBox()
        initFiltroBotones()
    }

    private fun FechaActual() {
        val tvFecha = findViewById<TextView>(R.id.tvFechaActual)
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        tvFecha.text = "Hoy: $fechaActual"
    }

    private fun obtenerGuiasDB(): List<Guia> {
        val lista = mutableListOf<Guia>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM guia", null)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val numero = cursor.getString(cursor.getColumnIndexOrThrow("numero"))
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
            val codigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo_cliente"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre_cliente"))
            val nroComprobante = cursor.getString(cursor.getColumnIndexOrThrow("nro_comprobante"))
            val importe = cursor.getDouble(cursor.getColumnIndexOrThrow("importe_x_cobrar"))
            val entregada = cursor.getInt(cursor.getColumnIndexOrThrow("entregada"))
            lista.add(Guia(id, numero, fecha, codigo, nombre, nroComprobante, importe, entregada))
        }
        cursor.close()
        return lista
    }

    private fun initSearchBox() {
        etBuscarMiGuia.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                aplicarFiltros()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun initFiltroBotones() {
        val grupo = findViewById<MaterialButtonToggleGroup>(R.id.filtroGuias)

        grupo.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                filtroActual = when (checkedId) {
                    R.id.btnEntregadas -> "ENTREGADAS"
                    R.id.btnNoEntregadas -> "NO_ENTREGADAS"
                    else -> "TODAS"
                }
                aplicarFiltros()
            }
        }
    }

    private fun aplicarFiltros() {
        val texto = etBuscarMiGuia.text.toString().lowercase()

        val filtradas = listaGuiasOriginal.filter { guia ->
            val coincideTexto = guia.numero.lowercase().contains(texto) ||
                    guia.nombre.lowercase().contains(texto) ||
                    guia.codigo.lowercase().contains(texto)

            val coincideFiltro = when (filtroActual) {
                "ENTREGADAS" -> guia.entregada == 1
                "NO_ENTREGADAS" -> guia.entregada == 0
                else -> true
            }

            coincideTexto && coincideFiltro
        }

        adapter.actualizarLista(filtradas)
    }
}
