package com.example.apptransportistas.liquidacion

data class Liquidacion(
    val id: Int,
    val rucTransportista: String,
    val nombreTransportista: String,
    val fechaEmision: String,
    val fechaDesde: String,
    val fechaHasta: String,
    val tarifaCodigo: String,
    val tarifaNombre: String,
    val guias: List<GuiaLiquidacion>
)

data class GuiaLiquidacion(
    val numero: String,
    val fecha: String,
    val importe: Double
)
