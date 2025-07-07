package com.example.apptransportistas.data.local

import android.content.ContentValues

class Repository(private val dbHelper: DatabaseHelper) {

    fun marcarGuiaComoEntregada(id: Long): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("entregada", 1)
        }
        val rowsUpdated = db.update("guia", values, "id = ?", arrayOf(id.toString()))
        db.close()
        return rowsUpdated > 0
    }

}