package com.example.apptransportistas.data.local

import android.content.ContentValues
import android.graphics.Bitmap
import com.example.apptransportistas.liquidacion.GuiaLiquidacion
import com.example.apptransportistas.liquidacion.Liquidacion
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

    fun obtenerLiquidacionDelDia(fechaEmision: String): Liquidacion {
        val db = dbHelper.readableDatabase
        val guias = mutableListOf<GuiaLiquidacion>()

        val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val cursorGuias = db.rawQuery("""
        SELECT numero, monto_cobrado 
        FROM guia 
        WHERE entregada = 1
        """.trimIndent(), null)

        while (cursorGuias.moveToNext()) {
            val numero = cursorGuias.getString(0)
            val importe = cursorGuias.getDouble(1)
            val fechaEntrega = hoy
            guias.add(GuiaLiquidacion(numero, fechaEntrega, importe))
        }

        cursorGuias.close()
        db.close()

        return Liquidacion(
            id = 114,
            rucTransportista = "20609128071",
            nombreTransportista = "INVERSIONES G & L SOCIEDAD ANONIMA CERRADA",
            fechaEmision = fechaEmision,
            fechaDesde = "21/07/2025",
            fechaHasta = "21/07/2025",
            tarifaCodigo = "TF001",
            tarifaNombre = "PRUEBA DE TARIFA FIJA",
            guias = guias
        )
    }


}