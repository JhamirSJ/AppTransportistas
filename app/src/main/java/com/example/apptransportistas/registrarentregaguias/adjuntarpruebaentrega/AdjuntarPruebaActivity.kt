package com.example.apptransportistas.registrarentregaguias.adjuntarpruebaentrega

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apptransportistas.R
import com.example.apptransportistas.data.local.DatabaseHelper
import com.example.apptransportistas.data.local.Repository
import com.example.apptransportistas.menu.MenuActivity
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.android.material.button.MaterialButton

class AdjuntarPruebaActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 100
    private var imagenUri: Uri? = null
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var repository: Repository
    private var guiaId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adjuntar_prueba)

        dbHelper = DatabaseHelper(this)
        repository = Repository(dbHelper)
        guiaId = intent.getLongExtra("guiaId", -1L)

        val signaturePad = findViewById<SignaturePad>(R.id.signaturePad)
        val btnBorrarFirma = findViewById<Button>(R.id.btnBorrarFirma)
        val btnSeleccionarImagen = findViewById<Button>(R.id.btnSeleccionarImagen)
        val btnRegEntrega = findViewById<MaterialButton>(R.id.btnRegEntrega)

        signaturePad.setSaveEnabled(false)

        btnBorrarFirma.setOnClickListener {
            signaturePad.clear()
        }

        btnSeleccionarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnRegEntrega.setOnClickListener {
            btnRegEntrega.isEnabled = false
            if (guiaId != -1L) {

                val success = repository.marcarGuiaComoEntregada(guiaId)

                if (success) {
                    Toast.makeText(this, "Entrega registrada correctamente ✅", Toast.LENGTH_SHORT)
                        .show()
                    navigateToMenu()
                } else {
                    Toast.makeText(this, "❌ No se encontró la guía", Toast.LENGTH_SHORT).show()
                    btnRegEntrega.isEnabled = true
                }
            } else {
                Toast.makeText(this, "❌ Guía no válida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imagenUri = data?.data
            val imageView = findViewById<ImageView>(R.id.ivFotoComprobante)
            imageView.setImageURI(imagenUri)
        }
    }

    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
