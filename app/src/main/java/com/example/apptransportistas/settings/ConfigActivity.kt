package com.example.apptransportistas.settings

import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apptransportistas.R
import java.io.File

class ConfigActivity : AppCompatActivity() {

    private lateinit var etUserId: EditText
    private val fileName = "config.xml"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        etUserId = findViewById(R.id.etUserId)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        // Cargar valor actual de iduser
        val file = File(filesDir, fileName)
        if (file.exists()) {
            try {
                val xml = file.readText()
                val regex = Regex("iduser=\"(\\d+)\"")
                val match = regex.find(xml)
                val idUser = match?.groups?.get(1)?.value ?: ""
                etUserId.setText(idUser)
            } catch (e: Exception) {
                Toast.makeText(this, "Error leyendo configuración", Toast.LENGTH_SHORT).show()
            }
        }

        btnGuardar.setOnClickListener {
            val nuevoIdUser = etUserId.text.toString().trim()
            if (nuevoIdUser.isEmpty()) {
                Toast.makeText(this, "El ID de usuario no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                var xml = file.readText()
                // Reemplazar iduser en el XML
                xml = xml.replace(Regex("iduser=\"\\d+\""), "iduser=\"$nuevoIdUser\"")
                file.writeText(xml)
                Toast.makeText(this, "Configuración guardada ✅", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error guardando configuración", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

