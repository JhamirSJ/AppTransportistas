package com.example.apptransportistas.registrardeposito

import android.content.ContentValues
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.text.Editable
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
import android.text.TextWatcher
import android.provider.MediaStore

class RegDepositoActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private var imagenUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg_deposito)

        dbHelper = DatabaseHelper(this)
        FechaActual()

        val etFechaOperacion = findViewById<EditText>(R.id.etFechaOperacion)
        val btnTomarFoto = findViewById<Button>(R.id.btnTomarFoto)
        val btnRegDeposito = findViewById<Button>(R.id.btnRegDeposito)
        val ivFotoVoucher = findViewById<ImageView>(R.id.ivFotoVoucher)
        val etMontoDepositado = findViewById<EditText>(R.id.etMontoDepositado)

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

        etMontoDepositado.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                actualizarDepositoPendiente()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imagenUri?.let { uri ->
                    findViewById<ImageView>(R.id.ivFotoVoucher).setImageURI(uri)
                    Log.d("RegDeposito", "Foto tomada: $uri")
                }
            } else {
                imagenUri = null
                Toast.makeText(this, "No se tomó la foto", Toast.LENGTH_SHORT).show()
            }
        }

        btnTomarFoto.setOnClickListener {
            imagenUri = crearUriImagen()
            imagenUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        }

        btnRegDeposito.setOnClickListener { registrarDeposito() }
        actualizarDepositoPendiente()
    }

    private fun crearUriImagen(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "deposito_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Depositos")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun obtenerTotalPorCobrar(): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(monto_cobrado) FROM guia WHERE entregada = 1", null
        )
        val total = if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
        cursor.close()
        return total
    }

    private fun actualizarDepositoPendiente() {
        val etMonto = findViewById<EditText>(R.id.etMontoDepositado)
        val tvPendiente = findViewById<TextView>(R.id.tvDepositoPendiente)

        val totalPorCobrar = obtenerTotalPorCobrar()
        val montoDepositado = etMonto.text.toString().toDoubleOrNull() ?: 0.0
        val diferencia = montoDepositado - totalPorCobrar

        val texto = if (diferencia >= 0) {
            "- %.2f".format(diferencia)
        } else {
            "%.2f".format(diferencia * -1)
        }

        tvPendiente.text = texto
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
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT)
                .show()
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
