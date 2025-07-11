package com.example.apptransportistas.registrardeposito

import android.content.ContentValues
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.apptransportistas.data.local.DatabaseHelper
import com.example.apptransportistas.util.DateInputMask
import java.util.Locale

class RegDepositoActivity : AppCompatActivity() {

    private var imagenUri: Uri? = null
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg_deposito)

        dbHelper = DatabaseHelper(this)
        FechaActual()

        val etFechaOperacion = findViewById<EditText>(R.id.etFechaOperacion)
        val btnSeleccionarImagen = findViewById<Button>(R.id.btnSeleccionarImagen)
        val btnRegDeposito = findViewById<Button>(R.id.btnRegDeposito)
        val imageView = findViewById<ImageView>(R.id.ivFotoVoucher)

        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT nombre FROM banco ORDER BY nombre", null)
        val bancos = mutableListOf<String>()

        DateInputMask(etFechaOperacion)

        while (cursor.moveToNext()) {
            bancos.add(cursor.getString(0))
        }
        cursor.close()

        val adapter = ArrayAdapter(this, R.layout.item_dropdown_banco, bancos)
        val autoBanco = findViewById<AutoCompleteTextView>(R.id.etBancoDeposito)
        autoBanco.setAdapter(adapter)

        autoBanco.setOnTouchListener { _, _ ->
            autoBanco.showDropDown()
            false
        }

        autoBanco.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                autoBanco.showDropDown()
            }
        }

        // Inicializa el ActivityResultLauncher
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    imagenUri = uri
                    imageView.setImageURI(uri)
                    Log.d("RegDeposito", "Imagen seleccionada: $uri")
                }
            }
        }

        btnSeleccionarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        }

        btnRegDeposito.setOnClickListener { registrarDeposito() }
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
