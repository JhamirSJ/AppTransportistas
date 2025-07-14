package com.example.apptransportistas.data.local

import android.content.ContentValues
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Repository(private val dbHelper: DatabaseHelper) {

    fun marcarGuiaComoEntregada(guiaId: Long): Boolean {
        val db = dbHelper.writableDatabase

        // Obtiene el importe_x_cobrar actual
        val cursor = db.rawQuery(
            "SELECT importe_x_cobrar FROM guia WHERE id = ?", arrayOf(guiaId.toString())
        )
        val importeXCobrar = if (cursor.moveToFirst()) cursor.getDouble(0) else null
        cursor.close()

        if (importeXCobrar == null) return false

        // Actualiza entregada y monto_cobrado = importe_x_cobrar
        val values = ContentValues().apply {
            put("entregada", 1)
            put("monto_cobrado", importeXCobrar)
        }
        val rowsUpdated = db.update("guia", values, "id = ?", arrayOf(guiaId.toString()))
        db.close()
        return rowsUpdated > 0
    }

    fun guardarPruebaEntrega(guiaId: Long, firmaBitmap: Bitmap?, imagenPath: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("guia_id", guiaId)
            put(
                "fecha_registro", SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            )
            firmaBitmap?.let { put("firma", bitmapToByteArray(it)) }
            put("imagen_path", imagenPath)
            put("sincronizado", 0)
        }
        return db.insert("prueba_entrega", null, values) != -1L
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}