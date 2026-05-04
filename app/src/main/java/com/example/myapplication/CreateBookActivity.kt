package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class CreateBookActivity : AppCompatActivity() {

    private lateinit var arrowBack: ImageView
    private lateinit var arrowExpand: ImageView
    private lateinit var layoutDetalhes: View
    private var detalhesAbertos = false

    private lateinit var btnAddAutor: ImageView

    private lateinit var autorBox2: View
    private lateinit var autorBox3: View
    private lateinit var autorBox4: View

    private lateinit var editAutor1: TextInputEditText
    private lateinit var editAutor2: TextInputEditText
    private lateinit var editAutor3: TextInputEditText
    private lateinit var editAutor4: TextInputEditText

    private lateinit var btnRemoveAutor2: View
    private lateinit var btnRemoveAutor3: View
    private lateinit var btnRemoveAutor4: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_book)

        arrowBack = findViewById(R.id.arrowBackId)
        arrowExpand = findViewById(R.id.arrowExpandId)
        layoutDetalhes = findViewById(R.id.layoutDetalhes)

        HeaderAdminNavigation.setup(this)
        FooterNavigation.setup(this)

        btnAddAutor = findViewById(R.id.btnAddAutor)

        autorBox2 = findViewById(R.id.autorBox2)
        autorBox3 = findViewById(R.id.autorBox3)
        autorBox4 = findViewById(R.id.autorBox4)

        editAutor1 = findViewById(R.id.editAutor1)
        editAutor2 = findViewById(R.id.editAutor2)
        editAutor3 = findViewById(R.id.editAutor3)
        editAutor4 = findViewById(R.id.editAutor4)

        btnRemoveAutor2 = findViewById(R.id.btnRemoveAutor2)
        btnRemoveAutor3 = findViewById(R.id.btnRemoveAutor3)
        btnRemoveAutor4 = findViewById(R.id.btnRemoveAutor4)

        arrowBack.setOnClickListener {
            voltarParaTelaAnterior()
        }

        arrowExpand.setOnClickListener {
            alternarDetalhse()
        }

        btnAddAutor.setOnClickListener {
            adicionarAutor()
            Log.d("TESTE_BOTAO", "Cliquei no botão adicionar autor")
        }

        btnRemoveAutor2.setOnClickListener {
            removerAutor(autorBox2, editAutor2)
        }

        btnRemoveAutor3.setOnClickListener {
            removerAutor(autorBox3, editAutor3)
        }

        btnRemoveAutor4.setOnClickListener {
            removerAutor(autorBox4, editAutor4)
        }
    }

    private fun voltarParaTelaAnterior() {
        startActivity(Intent(this, HomePageAdmin::class.java))
    }

    private fun alternarDetalhse() {
        detalhesAbertos = !detalhesAbertos

        if (detalhesAbertos) {
            layoutDetalhes.visibility = View.VISIBLE
            arrowExpand.rotation = 270f
        } else {
            layoutDetalhes.visibility = View.GONE
            arrowExpand.rotation = 90f
        }
    }

    private fun adicionarAutor() {
        when {
            autorBox2.visibility != View.VISIBLE -> {
                autorBox2.visibility = View.VISIBLE
            }

            autorBox3.visibility != View.VISIBLE -> {
                autorBox3.visibility = View.VISIBLE
            }

            autorBox4.visibility != View.VISIBLE -> {
                autorBox4.visibility = View.VISIBLE
            }

            else -> {
                Toast.makeText(
                    this,
                    "Você pode adicionar no máximo 4 autores",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun removerAutor(autorBox: View, editAutor: TextInputEditText) {
        editAutor.setText("")
        autorBox.visibility = View.GONE
    }

    private fun obterAutores(): List<String> {
        return listOf(
            editAutor1.text.toString().trim(),
            editAutor2.text.toString().trim(),
            editAutor3.text.toString().trim(),
            editAutor4.text.toString().trim()
        ).filter { it.isNotEmpty() }
    }
}