package com.example.apptransportistas.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "sanjorge_transportistas_db"
        const val DATABASE_VERSION = 1

        const val TABLE_GUIA = "guia"
        const val TABLE_PRODUCTO = "producto"
    }
    override fun onCreate(db: SQLiteDatabase) {

        // Tabla guía
        val createGuia = """
            CREATE TABLE $TABLE_GUIA (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero TEXT,
                fecha TEXT,
                codigo_cliente TEXT,
                nombre_cliente TEXT,
                nro_comprobante TEXT,
                importe_x_cobrar REAL,
                monto_cobrado REAL DEFAULT 0,
                entregada INTEGER DEFAULT 0
            );
        """.trimIndent()

        // Tabla producto
        val createProducto = """
            CREATE TABLE $TABLE_PRODUCTO (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_guia INTEGER,
                nombre TEXT,
                cantidad INTEGER,
                FOREIGN KEY(id_guia) REFERENCES $TABLE_GUIA(id)
            );
        """.trimIndent()

        db.execSQL(createGuia)
        db.execSQL(createProducto)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}