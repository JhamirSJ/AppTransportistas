package com.example.apptransportistas.seleccionarguia

import java.io.Serializable

data class Guia(
    val numero: String,
    val fecha: String,
    val codigo: String,
    val nombre: String,
    val nroComprobante: String,
    val importeXCobrar: Double
) : Serializable