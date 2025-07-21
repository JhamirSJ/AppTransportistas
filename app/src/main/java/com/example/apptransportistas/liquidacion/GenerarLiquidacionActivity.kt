package com.example.apptransportistas.liquidacion

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
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
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.typeface = Typeface.MONOSPACE

        val leftMargin = 40f
        var currentY = 60f

        // TÍTULO
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 16f
        canvas.drawText("LIQUIDACIÓN DE TRANSPORTE", pageInfo.pageWidth / 2f, currentY, paint)
        currentY += 30f

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT

        // CABECERA
        val infoLines = listOf(
            "Liquidación ID: ${liquidacion.id}",
            "Transportista: ${liquidacion.rucTransportista} - ${liquidacion.nombreTransportista}",
            "Fecha de Emisión: ${liquidacion.fechaEmision}",
            "Fecha Desde: ${liquidacion.fechaDesde}",
            "Fecha Hasta: ${liquidacion.fechaHasta}",
            "Tarifa: ${liquidacion.tarifaCodigo} - ${liquidacion.tarifaNombre}"
        )

        for (line in infoLines) {
            canvas.drawText(line, leftMargin, currentY, paint)
            currentY += 18f
        }

        currentY += 20f

        // TABLA
        val tableStartX = leftMargin
        val tableWidth = pageInfo.pageWidth - 2 * leftMargin
        val columnWidths = listOf(200f, 100f, 100f)
        val headers = listOf("GUÍA", "FECHA", "IMPORTE")
        val rowHeight = 25f

        // Encabezados
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 12f

        var colX = tableStartX
        for ((i, header) in headers.withIndex()) {
            val cellRight = colX + columnWidths[i]

            // Dibujar borde
            paint.style = Paint.Style.STROKE
            canvas.drawRect(colX, currentY, cellRight, currentY + rowHeight, paint)

            // Dibujar texto centrado
            paint.style = Paint.Style.FILL
            canvas.drawText(header, (colX + cellRight) / 2, currentY + 17f, paint)

            colX = cellRight
        }

        currentY += rowHeight

        // Filas
        paint.typeface = Typeface.DEFAULT
        for (guia in liquidacion.guias) {
            colX = tableStartX
            val rowTop = currentY
            val rowBottom = rowTop + rowHeight

            val cells = listOf(guia.numero, guia.fecha, "S/ %.2f".format(guia.importe))
            for ((i, cellText) in cells.withIndex()) {
                val cellRight = colX + columnWidths[i]

                // Borde
                paint.style = Paint.Style.STROKE
                canvas.drawRect(colX, rowTop, cellRight, rowBottom, paint)

                // Texto
                paint.style = Paint.Style.FILL
                canvas.drawText(cellText, (colX + cellRight) / 2, rowTop + 17f, paint)

                colX = cellRight
            }
            currentY += rowHeight
        }

        // Total
        currentY += 30f
        val total = liquidacion.guias.sumOf { it.importe }
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.style = Paint.Style.FILL
        canvas.drawText("TOTAL: S/ %.2f".format(total), leftMargin, currentY, paint)

        pdfDocument.finishPage(page)

        // Guardar
        val file = File(context.getExternalFilesDir(null), "liquidacion.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

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