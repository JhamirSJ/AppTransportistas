package com.example.apptransportistas.guias.misguias

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apptransportistas.R
import com.example.apptransportistas.data.local.DatabaseHelper
import com.example.apptransportistas.guias.Guia
import com.example.apptransportistas.guias.GuiaAdapter
import java.util.Date
import java.util.Locale

class MisGuiasActivity : AppCompatActivity() {

    private lateinit var rvListaGuias: RecyclerView
    private lateinit var listaGuias: List<Guia>
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_guias)
        FechaActual()

        dbHelper = DatabaseHelper(this)
        rvListaGuias = findViewById(R.id.rvGuias)

        listaGuias = obtenerGuiasDB()

        val adapter = GuiaAdapter(listaGuias) {}
        rvListaGuias.layoutManager = LinearLayoutManager(this)
        rvListaGuias.adapter = adapter
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
}