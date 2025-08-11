package com.example.apptransportistas.verproductos

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apptransportistas.R

class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tvNombre: TextView = itemView.findViewById(R.id.tvNombreProducto)
    val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidadProducto)
}
