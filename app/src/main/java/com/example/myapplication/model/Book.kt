package com.example.myapplication.model

data class Book(
    val title: String,
    val author: String,
    val imageUrl: Int, // Guarda uma imagem local padrão, usada se não tiver capa real.
    val capaBase64: String? = null // Guarda a capa real vinda do Firestore em Base64; pode ser nula.
)