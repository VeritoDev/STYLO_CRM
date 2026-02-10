package com.example.tfg.model

data class Cliente(
    val id: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val email: String = "",
    val notas: String = "",
    val historial: Map<String, String>? = null
)