package com.example.apptransportistas.data.local

import android.content.ContentValues

class Repository(private val dbHelper: DatabaseHelper) {

    fun marcarGuiaComoEntregada(guiaId: Long): Boolean {
        val db = dbHelper.writableDatabase

        // Obtiene el importe_x_cobrar actual
        val cursor = db.rawQuery("SELECT importe_x_cobrar FROM guia WHERE id = ?", arrayOf(guiaId.toString()))
        val importeXCobrar = if (cursor.moveToFirst()) cursor.getDouble(0) else null
        cursor.close()

        if (importeXCobrar == null) return false

        // Actualiza entregada = 1 y monto_cobrado = importe_x_cobrar
        val values = ContentValues().apply {
            put("entregada", 1)
            put("monto_cobrado", importeXCobrar)
        }
        val rowsUpdated = db.update("guia", values, "id = ?", arrayOf(guiaId.toString()))
        db.close()
        return rowsUpdated > 0
    }
}