package com.example.tfg.model

data class Cliente(
    val id: String = "",
    val nombreCliente: String = "",
    val telefono: String = "",
    val email: String = "",
    val notas: String = "",
    val ultimoServicio: String = "",
    val ultimaCita: String = "",
    val historial: Map<String, String>? = null
)