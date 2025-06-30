package com.example.apptransportistas.verproductos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apptransportistas.R
import com.example.apptransportistas.data.local.DatabaseHelper

class VerProductosActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_productos)

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.rvProductos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val guiaId = intent.getLongExtra("guia_id", -1)
        if (guiaId != -1L) {
            val productos = obtenerProductosDeGuia(guiaId)
            adapter = ProductoAdapter(productos)
            recyclerView.adapter = adapter
        }

    }

    private fun obtenerProductosDeGuia(guiaId: Long): List<Producto> {
        val lista = mutableListOf<Producto>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, nombre, cantidad FROM producto WHERE id_guia = ?", arrayOf(guiaId.toString()))
        while (cursor.moveToNext()) {
            val id = cursor.getLong(0)
            val nombre = cursor.getString(1)
            val cantidad = cursor.getInt(2)
            lista.add(Producto(id, nombre, cantidad))
        }
        cursor.close()
        return lista
    }
}