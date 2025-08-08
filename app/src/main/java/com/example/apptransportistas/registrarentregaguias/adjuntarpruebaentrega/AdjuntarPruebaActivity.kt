package com.example.apptransportistas.registrarentregaguias.adjuntarpruebaentrega

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.apptransportistas.R
import com.example.apptransportistas.data.local.DatabaseHelper
import com.example.apptransportistas.data.local.Repository
import com.example.apptransportistas.menu.MenuActivity
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore

class AdjuntarPruebaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var repository: Repository
    private var guiaId: Long = -1L

    private var imagenUri: Uri? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adjuntar_prueba)

        dbHelper = DatabaseHelper(this)
        repository = Repository(dbHelper)
        guiaId = intent.getLongExtra("guiaId", -1L)

        val signaturePad = findViewById<SignaturePad>(R.id.signaturePad)
        val btnBorrarFirma = findViewById<Button>(R.id.btnBorrarFirma)
        val btnTomarFoto = findViewById<Button>(R.id.btnTomarFoto)
        val btnRegEntrega = findViewById<MaterialButton>(R.id.btnRegEntrega)
        val fotoComprobante = findViewById<ImageView>(R.id.ivFotoComprobante)

        signaturePad.setSaveEnabled(false)

        btnBorrarFirma.setOnClickListener { signaturePad.clear() }

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    fotoComprobante.setImageURI(imagenUri)
                } else {
                    // Si falla, elimina la entrada en MediaStore
                    imagenUri?.let { contentResolver.delete(it, null, null) }
                    imagenUri = null
                    Toast.makeText(this, "Foto cancelada o fallida", Toast.LENGTH_SHORT).show()
                }
            }

        btnTomarFoto.setOnClickListener {
            tomarFoto()
        }

        btnRegEntrega.setOnClickListener {
            val hayFirma = !signaturePad.isEmpty
            val hayImagen = imagenUri != null

            if (!hayFirma && !hayImagen) {
                Toast.makeText(
                    this, "Adjunta al menos una prueba (firma o imagen)", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            btnRegEntrega.isEnabled = false
            val firmaBitmap = if (hayFirma) signaturePad.signatureBitmap else null

            lifecycleScope.launch(Dispatchers.IO) {
                val pruebaGuardada = repository.guardarPruebaEntrega(
                    guiaId, firmaBitmap, imagenUri?.toString() ?: ""
                )
                val guiaMarcada = repository.marcarGuiaComoEntregada(guiaId)

                withContext(Dispatchers.Main) {
                    if (pruebaGuardada && guiaMarcada) {
                        Toast.makeText(
                            this@AdjuntarPruebaActivity,
                            "Entrega registrada correctamente ✅",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToMenu()
                    } else {
                        Toast.makeText(
                            this@AdjuntarPruebaActivity,
                            "❌ No se pudo registrar la entrega",
                            Toast.LENGTH_SHORT
                        ).show()
                        btnRegEntrega.isEnabled = true
                    }
                }
            }
        }
    }

    private fun tomarFoto() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "foto_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/AppTransportistas"
            )
        }

        imagenUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        )

        imagenUri?.let { uri ->
            cameraLauncher.launch(uri)
        } ?: Toast.makeText(this, "No se pudo crear la imagen", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
