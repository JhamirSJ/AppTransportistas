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
import java.io.File
import java.io.FileOutputStream

class AdjuntarPruebaActivity : AppCompatActivity() {

    private var imagenSeleccionadaUri: Uri? = null
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var repository: Repository
    private var guiaId: Long = -1L

    private lateinit var imagenPickerLauncher: ActivityResultLauncher<Intent>

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
        val imageView = findViewById<ImageView>(R.id.ivFotoComprobante)

        signaturePad.setSaveEnabled(false)

        btnBorrarFirma.setOnClickListener { signaturePad.clear() }

        imagenPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode ==RESULT_OK) {
                val originalUri = result.data?.data
                if (originalUri != null) {
                    try {
                        val inputStream = contentResolver.openInputStream(originalUri)
                        val file = File(cacheDir, "prueba_entrega_${System.currentTimeMillis()}.jpg")
                        val outputStream = FileOutputStream(file)
                        inputStream?.copyTo(outputStream)
                        inputStream?.close()
                        outputStream.close()

                        imagenSeleccionadaUri = Uri.fromFile(file)
                        imageView.setImageURI(imagenSeleccionadaUri)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error al procesar imagen", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
            }
        }

        btnSeleccionarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagenPickerLauncher.launch(intent)
        }

        btnRegEntrega.setOnClickListener {
            val hayFirma = !signaturePad.isEmpty
            val hayImagen = imagenSeleccionadaUri != null

            if (!hayFirma && !hayImagen) {
                Toast.makeText(this, "Adjunta al menos una prueba (firma o imagen)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegEntrega.isEnabled = false
            val firmaBitmap = if (hayFirma) signaturePad.signatureBitmap else null

            lifecycleScope.launch(Dispatchers.IO) {
                val pruebaGuardada = repository.guardarPruebaEntrega(
                    guiaId,
                    firmaBitmap,
                    imagenSeleccionadaUri?.toString() ?: ""
                )
                val guiaMarcada = repository.marcarGuiaComoEntregada(guiaId)

                withContext(Dispatchers.Main) {
                    if (pruebaGuardada && guiaMarcada) {
                        Toast.makeText(this@AdjuntarPruebaActivity, "Entrega registrada correctamente ✅", Toast.LENGTH_SHORT).show()
                        navigateToMenu()
                    } else {
                        Toast.makeText(this@AdjuntarPruebaActivity, "❌ No se pudo registrar la entrega", Toast.LENGTH_SHORT).show()
                        btnRegEntrega.isEnabled = true
                    }
                }
            }
        }
    }

    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
