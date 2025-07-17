package com.example.apptransportistas.menu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.apptransportistas.R
import com.example.apptransportistas.registrardeposito.RegDepositoActivity
import com.example.apptransportistas.guias.misguias.MisGuiasActivity
import com.example.apptransportistas.registrarentregaguias.RegGuiasActivity
import com.example.apptransportistas.sincronizardata.SincronizarDataActivity
import com.example.apptransportistas.ubicacion.TrackingService
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileOutputStream

class MenuActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnMisGuias = findViewById<MaterialButton>(R.id.btnMisGuias)
        val btnRegGuias = findViewById<MaterialButton>(R.id.btnRegGuias)
        val btnSyncData = findViewById<MaterialButton>(R.id.btnSyncData)
        val btnRegDeposito = findViewById<MaterialButton>(R.id.btnRegDeposito)
        val btnGenerarPDF = findViewById<MaterialButton>(R.id.btnGenPDF)

        checkLocationPermission()

        btnMisGuias.setOnClickListener { navigateToMisGuias() }
        btnRegGuias.setOnClickListener { navigateToRegGuias() }
        btnSyncData.setOnClickListener { navigateToSincronizarData() }
        btnRegDeposito.setOnClickListener { navigateToRegDeposito() }
        btnGenerarPDF.setOnClickListener {
            val file = generarPDF(this)
            abrirPDFConAppExterna(this, file)
        }

    }

    private fun navigateToMisGuias() {
        val intent = Intent(this, MisGuiasActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToRegGuias() {
        val intent = Intent(this, RegGuiasActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSincronizarData() {
        val intent = Intent(this, SincronizarDataActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToRegDeposito() {
        val intent = Intent(this, RegDepositoActivity::class.java)
        startActivity(intent)
    }

    private fun startTrackingService() {
        val intent = Intent(this, TrackingService::class.java)
        startService(intent)
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            startTrackingService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTrackingService()
            } else {
                Toast.makeText(this, "El permiso de ubicación es obligatorio para usar la app", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun generarPDF(context: Context): File {
        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.textSize = 12f
        canvas.drawText("LIQUIDACIÓN DE TRANSPORTE", 10f, 25f, paint)
        canvas.drawText("Transportista: RUC 123456789 - Transportes S.A.", 10f, 50f, paint)
        canvas.drawText("Fecha de emisión: 2025-07-16", 10f, 75f, paint)

        pdfDocument.finishPage(page)

        val documentsDir = File(context.getExternalFilesDir(null), "Liquidaciones")
        if (!documentsDir.exists()) documentsDir.mkdirs()

        val file = File(documentsDir, "liquidacion.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        Toast.makeText(context, "PDF generado en: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        return file
    }


    fun abrirPDFConAppExterna(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Abrir PDF con...")

        if (chooser.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        } else {
            Toast.makeText(context, "No hay una app para abrir PDFs", Toast.LENGTH_SHORT).show()
        }
    }


}