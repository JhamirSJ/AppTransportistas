package com.example.apptransportistas.liquidacion

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.apptransportistas.R
import com.example.apptransportistas.data.local.DatabaseHelper
import com.example.apptransportistas.data.local.Repository
import java.io.File
import java.io.FileOutputStream

class GenerarLiquidacionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generar_liquidacion)

        val fecha = intent.getStringExtra("fecha") ?: return

        val dbHelper = DatabaseHelper(this)
        val repo = Repository(dbHelper)

        val liquidacion = repo.obtenerLiquidacionDelDia(fecha)

        val file = generarPDF(this, liquidacion)

        abrirPDFConAppExterna(this, file)

        finish()
    }

    fun generarPDF(context: Context, liquidacion: Liquidacion): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply { textSize = 10f }

        var y = 20f
        canvas.drawText("LIQUIDACIÓN DE TRANSPORTE", 10f, y, paint); y += 20
        canvas.drawText("Transportista: ${liquidacion.transportistaRuc} - ${liquidacion.transportistaNombre}", 10f, y, paint); y += 20
        canvas.drawText("Fecha de emisión: ${liquidacion.fechaEmision}", 10f, y, paint); y += 30

        liquidacion.guias.forEach { guia ->
            canvas.drawText("Guía: ${guia.idGuia}", 10f, y, paint); y += 15
            guia.productos.forEach { producto ->
                canvas.drawText("- ${producto.nombre} x${producto.cantidad}", 15f, y, paint)
                y += 15
            }
            y += 10
        }

        y += 20
        canvas.drawText("Subtotal: S/${"%.2f".format(liquidacion.importeBruto)}", 10f, y, paint); y += 15
        canvas.drawText("IGV (18%): S/${"%.2f".format(liquidacion.importeIgv)}", 10f, y, paint); y += 15
        canvas.drawText("TOTAL: S/${"%.2f".format(liquidacion.importeTotal)}", 10f, y, paint)

        pdfDocument.finishPage(page)

        val dir = File(context.getExternalFilesDir(null), "Liquidaciones")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "liquidacion_${liquidacion.fechaEmision}.pdf")
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