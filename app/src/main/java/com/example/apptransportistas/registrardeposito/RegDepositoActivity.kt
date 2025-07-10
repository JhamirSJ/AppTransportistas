package com.example.apptransportistas.registrardeposito

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.apptransportistas.R
import java.util.Date
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.example.apptransportistas.data.local.DatabaseHelper
import com.example.apptransportistas.util.DateInputMask
import java.util.Locale

class RegDepositoActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 100
    private var imagenUri: Uri? = null
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg_deposito)

        dbHelper = DatabaseHelper(this)
        FechaActual()

        val etFechaOperacion = findViewById<EditText>(R.id.etFechaOperacion)
        val btnSeleccionarImagen = findViewById<Button>(R.id.btnSeleccionarImagen)
        val btnRegDeposito = findViewById<Button>(R.id.btnRegDeposito)

        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT nombre FROM banco ORDER BY nombre", null)
        val bancos = mutableListOf<String>()

        DateInputMask(etFechaOperacion)

        while (cursor.moveToNext()) {
            bancos.add(cursor.getString(0))
        }
        cursor.close()

        val adapter = ArrayAdapter(this, R.layout.item_dropdown_banco, bancos)
        findViewById<AutoCompleteTextView>(R.id.etBancoDeposito).setAdapter(adapter)
        val autoBanco = findViewById<AutoCompleteTextView>(R.id.etBancoDeposito)

        autoBanco.setOnTouchListener { _, _ ->
            autoBanco.showDropDown()
            false
        }

        autoBanco.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                autoBanco.showDropDown()
            }
        }

        btnSeleccionarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnRegDeposito.setOnClickListener { registrarDeposito() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imagenUri = data?.data
            val imageView = findViewById<ImageView>(R.id.ivFotoVoucher)
            imageView.setImageURI(imagenUri)
        }
    }

    private fun registrarDeposito() {
        val etOperacion = findViewById<EditText>(R.id.etNroOperacion)
        val etFecha = findViewById<EditText>(R.id.etFechaOperacion)
        val etBanco = findViewById<AutoCompleteTextView>(R.id.etBancoDeposito)
        val etMonto = findViewById<EditText>(R.id.etMontoDepositado)

        val nroOperacion = etOperacion.text.toString()
        val fecha = etFecha.text.toString()
        val bancoNombre = etBanco.text.toString()
        val monto = etMonto.text.toString().toDoubleOrNull()
        val imagenPath = imagenUri?.toString() ?: ""

        if (fecha.isBlank() || monto == null || bancoNombre.isBlank() || nroOperacion.isBlank()) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.writableDatabase
        val cursor = db.rawQuery("SELECT id FROM banco WHERE nombre = ?", arrayOf(bancoNombre))

        if (cursor.moveToFirst()) {
            val idBanco = cursor.getInt(0)

            val values = ContentValues().apply {
                put("fecha", fecha)
                put("monto", monto)
                put("id_banco", idBanco)
                put("nro_operacion", nroOperacion)
                put("comprobante_path", imagenPath)
                put("sincronizado", 0)
            }

            val newId = db.insert("deposito", null, values)

            if (newId != -1L) {
                Toast.makeText(this, "Depósito registrado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al registrar el depósito", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Banco no válido", Toast.LENGTH_SHORT).show()
        }

        cursor.close()
    }

    private fun FechaActual() {
        val tvFecha = findViewById<TextView>(R.id.tvFechaActual)
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        tvFecha.text = "Hoy: $fechaActual"
    }
}