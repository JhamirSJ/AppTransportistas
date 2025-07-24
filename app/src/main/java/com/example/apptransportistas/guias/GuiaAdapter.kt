package com.example.apptransportistas.guias

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apptransportistas.R

class GuiaAdapter(
    listaInicial: List<Guia>,
    private val onGuiaClick: (Guia) -> Unit
) : RecyclerView.Adapter<GuiaViewHolder>() {

    private val guias = listaInicial.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuiaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guia, parent, false)
        return GuiaViewHolder(view, onGuiaClick)
    }

    override fun getItemCount() = guias.size

    override fun onBindViewHolder(holder: GuiaViewHolder, position: Int) {
        holder.bind(guias[position])
    }

    fun actualizarLista(nuevaLista: List<Guia>) {
        guias.clear()
        guias.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}

