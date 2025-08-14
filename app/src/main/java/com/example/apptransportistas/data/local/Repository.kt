package com.example.apptransportistas.data.local

import android.content.ContentValues
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

    fun guardarPruebaDeEntrega(guiaId: Long, firmaPath: String, imagenPath: String): Boolean {
        val db = dbHelper.writableDatabase
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fechaActual = formatoFecha.format(Date())

        val values = ContentValues().apply {
            put("guia_id", guiaId)
            put("fecha_registro", fechaActual)
            put("firma_path", firmaPath)
            put("imagen_path", imagenPath)
            put("sincronizado", 0)
        }
        return db.insert("prueba_entrega", null, values) != -1L
    }

    fun insertarUbicacion(lat: Double, lon: Double, fechaHora: String, deviceId: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("device_id", deviceId)
            put("latitud", lat)
            put("longitud", lon)
            put("fecha_hora", fechaHora)
            put("sincronizado", 0)
        }
        db.insert(DatabaseHelper.TABLE_UBICACION, null, values)
    }
}