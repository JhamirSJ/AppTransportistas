package com.example.apptransportistas.guias

import java.io.Serializable

data class Guia(
    val id: Long,
    val numero: String,
    val fecha: String,
    val codigo: String,
    val nombre: String,
    val nroComprobante: String,
    val importeXCobrar: Double,
    val entregada: Int = 0
) : Serializable