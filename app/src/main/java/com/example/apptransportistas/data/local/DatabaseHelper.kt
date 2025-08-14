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
        const val TABLE_PRUEBA = "prueba_entrega"
        const val TABLE_UBICACION = "tracking"
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

        // Tabla prueba
        val createPrueba = """
            CREATE TABLE $TABLE_PRUEBA (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                guia_id INTEGER NOT NULL,
                firma_path TEXT,
                imagen_path TEXT,
                fecha_registro TEXT,
                sincronizado INTEGER DEFAULT 0,
                FOREIGN KEY (guia_id) REFERENCES guia(id)
            );
        """.trimIndent()

        // Tabla ubicación transportista
        val createTracking = """
            CREATE TABLE $TABLE_UBICACION (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                device_id TEXT NOT NULL,
                latitud REAL NOT NULL,
                longitud REAL NOT NULL,
                fecha_hora TEXT NOT NULL,
                sincronizado INTEGER DEFAULT 0
            );
        """.trimIndent()

        db.execSQL(createGuia)
        db.execSQL(createProducto)
        db.execSQL(createPrueba)
        db.execSQL(createTracking)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}
