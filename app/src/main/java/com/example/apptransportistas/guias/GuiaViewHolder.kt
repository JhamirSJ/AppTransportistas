package com.example.apptransportistas.guias

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptransportistas.R

class GuiaViewHolder(
    itemView: View,
    private val onClick: (Guia) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val tvNumero = itemView.findViewById<TextView>(R.id.tvNumero)
    private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombre)
    private val tvImporte = itemView.findViewById<TextView>(R.id.tvImporte)

    fun bind(guia: Guia) {
        val estado = if (guia.entregada == 1) "✅ Entregada" else "🕒 Pendiente"
        tvNumero.text = "Guía #${guia.numero} - $estado"
        tvNombre.text = "Nombre: ${guia.nombre}"
        tvImporte.text = "Importe: S/. ${guia.importeXCobrar}"

        itemView.setOnClickListener { onClick(guia) }
    }
}