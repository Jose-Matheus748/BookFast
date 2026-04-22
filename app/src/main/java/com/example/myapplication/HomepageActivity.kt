package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

class HomepageActivity : AppCompatActivity() {

    lateinit var img1: ImageView
    lateinit var img2: ImageView
    lateinit var img3: ImageView
    lateinit var btnAnterior: Button
    lateinit var btnProximo: Button
    lateinit var btnSearch: ImageButton
    lateinit var etSearch: EditText
    lateinit var mainLayout: View

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

        mainLayout = findViewById(R.id.mainLayoutHomepage)

        img1 = findViewById(R.id.img1)
        img2 = findViewById(R.id.img2)
        img3 = findViewById(R.id.img3)

        btnAnterior = findViewById(R.id.btnAnterior)
        btnProximo = findViewById(R.id.btnProximo)

        btnSearch = findViewById(R.id.btnSearch)
        etSearch = findViewById(R.id.etSearch)

        btnSearch.setOnClickListener {
          val intent = Intent(this, SearchListActivity::class.java)
            startActivity(intent)
        }

        etSearch.setOnClickListener {
            // impede fechar ao clicar dentro dele
        }

        mainLayout.setOnClickListener {
            if (etSearch.isVisible) {
                fecharBusca()
            }
        }

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

    override fun onBackPressed() {
        if (etSearch.isVisible) {
            fecharBusca()
        } else {
            super.onBackPressed()
        }
    }

    private fun fecharBusca() {
        etSearch.setText("")
        etSearch.visibility = View.GONE
        btnSearch.visibility = View.VISIBLE
    }

    private fun mostrarGrupo() {
        val inicio = grupoAtual * tamanhoGrupo

        img1.setImageResource(imagens[inicio])
        img2.setImageResource(imagens[inicio + 1])
        img3.setImageResource(imagens[inicio + 2])
    }
}