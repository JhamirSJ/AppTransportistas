package com.example.apptransportistas.verproductos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apptransportistas.R

class ProductoAdapter(private val productos: List<Producto>) :
    RecyclerView.Adapter<ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.tvNombre.text = producto.nombre
        holder.tvCantidad.text = "Cantidad: ${producto.cantidad}"
    }

    override fun getItemCount() = productos.size
}
