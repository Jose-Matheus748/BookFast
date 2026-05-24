package com.example.myapplication

data class BookItem(
    val id: String = "",
    val titulo: String = "",
    val autores: List<String> = emptyList(),
    val material: String = "",
    val idioma: String = "",
    val publicacao: String = "",
    val edicao: String = "",
    val serie: String = "",
    val assuntos: String = "",
    val referencia: String = "",
    val capaUrl: String = ""
)