package com.example.apptransportistas.guias.seleccionarguia

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apptransportistas.R
import com.example.apptransportistas.data.local.DatabaseHelper
import com.example.apptransportistas.guias.Guia
import com.example.apptransportistas.guias.GuiaAdapter

class SelecGuiaActivity : AppCompatActivity() {

    private lateinit var rvListaGuias: RecyclerView
    private lateinit var listaGuias: List<Guia>
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selec_guia)

        dbHelper = DatabaseHelper(this)
        rvListaGuias = findViewById(R.id.rvListaGuias)

        listaGuias = obtenerGuiasDB()

        val adapter = GuiaAdapter(listaGuias) { guiaSeleccionada ->
            val intent = Intent()
            intent.putExtra("guiaSeleccionada", guiaSeleccionada)
            setResult(RESULT_OK, intent)
            finish()
        }

        rvListaGuias.layoutManager = LinearLayoutManager(this)
        rvListaGuias.adapter = adapter
    }

    private fun obtenerGuiasDB(): List<Guia> {
        val lista = mutableListOf<Guia>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM guia WHERE entregada = 0", null)

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
}