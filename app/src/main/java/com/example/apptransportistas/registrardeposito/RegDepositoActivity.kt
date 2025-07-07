package com.example.apptransportistas.registrardeposito

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.apptransportistas.R
import java.util.Date
import java.util.Locale

class RegDepositoActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 100
    private var imagenUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg_deposito)
        FechaActual()

        val btnSeleccionarImagen = findViewById<Button>(R.id.btnSeleccionarImagen)

        btnSeleccionarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imagenUri = data?.data
            val imageView = findViewById<ImageView>(R.id.ivFotoVoucher)
            imageView.setImageURI(imagenUri)
        }
    }

    private fun FechaActual() {
        val tvFecha = findViewById<TextView>(R.id.tvFechaActual)
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        tvFecha.text = "Hoy: $fechaActual"
    }
}