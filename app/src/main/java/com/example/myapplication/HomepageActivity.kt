package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomepageActivity: AppCompatActivity() {

    lateinit var img1: ImageView
    lateinit var img2: ImageView
    lateinit var img3: ImageView
    lateinit var btnAnterior: Button
    lateinit var btnProximo: Button

    val imagens = mutableListOf(
        R.drawable.livro1,
        R.drawable.livro2,
        R.drawable.livro3,
        R.drawable.livro4,
        R.drawable.livro5,
        R.drawable.livro6
    )

    var grupoAtual = 0
    var tamanhoGrupo = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        img1 = findViewById(R.id.img1)
        img2 = findViewById(R.id.img2)
        img3 = findViewById(R.id.img3)

        btnAnterior = findViewById(R.id.btnAnterior)
        btnProximo = findViewById(R.id.btnProximo)

        mostrarGrupo()

        btnProximo.setOnClickListener {
            val totalGrupos = imagens.size / tamanhoGrupo
            grupoAtual++

            if (grupoAtual >= totalGrupos) grupoAtual = 0

            mostrarGrupo()
        }

        btnAnterior.setOnClickListener {
            val totalGrupos = imagens.size / tamanhoGrupo
            grupoAtual--

            if (grupoAtual < 0) grupoAtual = totalGrupos - 1

            mostrarGrupo()
        }
    }

    private fun mostrarGrupo() {
        val inicio = grupoAtual * tamanhoGrupo

        img1.setImageResource(imagens[inicio])
        img2.setImageResource(imagens[inicio + 1])
        img3.setImageResource(imagens[inicio + 2])
    }
}