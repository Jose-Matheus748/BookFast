package com.example.myapplication.model

import com.example.myapplication.R
data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val capaBase64: String? = null,
    val imageUrl: Int = R.drawable.fortaleza_300
)
