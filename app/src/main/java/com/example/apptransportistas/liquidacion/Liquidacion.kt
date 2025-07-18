package com.example.apptransportistas.liquidacion

data class Liquidacion(
    val id: Int,
    val fechaEmision: String,
    val transportistaNombre: String,
    val transportistaRuc: String,
    val guias: List<GuiaLiquidacion>,
    val importeBruto: Double,
    val importeIgv: Double,
    val importeTotal: Double,
    val incluyeIgv: Boolean
)

data class GuiaLiquidacion(
    val idGuia: String,
    val productos: List<ProductoLiquidacion>
)

data class ProductoLiquidacion(
    val nombre: String,
    val cantidad: Int
)
