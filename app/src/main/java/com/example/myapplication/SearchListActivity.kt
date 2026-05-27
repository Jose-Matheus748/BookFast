package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.BookAdapter
import com.example.myapplication.model.Book
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class SearchListActivity : AppCompatActivity() {

    private lateinit var btnSearch: ImageButton
    private lateinit var etSearchList: EditText
    private lateinit var recyclerBooks: RecyclerView
    private lateinit var adapter: BookAdapter

    private val db = Firebase.firestore
    private val todosOsLivros = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_list)

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)

        btnSearch    = findViewById(R.id.btnSearch)
        etSearchList = findViewById(R.id.etSearch)
        recyclerBooks = findViewById(R.id.recyclerBooks)

        prepararRecycler()
        setupRecycler()
        fazerPesquisa()
        carregarLivros()
    }


    private fun prepararRecycler() {
        adapter = BookAdapter(todosOsLivros) { livro ->
            val intent = Intent(this, BookpageActivity::class.java)
            intent.putExtra("LIVRO_ID", livro.id)

    private fun setupRecycler() {
        adapter = BookAdapter(todosOsLivros) { livro ->
            val intent = Intent(this, BookpageActivity::class.java)
            intent.putExtra("LIVRO_ID", livro.id)
            intent.putExtra("TITULO", livro.title)
            intent.putExtra("AUTOR", livro.author)
            startActivity(intent)
        }

        recyclerBooks.apply {
            layoutManager = GridLayoutManager(this@SearchListActivity, 2)
            setHasFixedSize(true)
            adapter = this@SearchListActivity.adapter
        }
    }

    private fun fazerPesquisa() {
        btnSearch.setOnClickListener {
            etSearchList.isVisible = true
            btnSearch.isVisible = false
            etSearchList.requestFocus()
        }

        etSearchList.addTextChangedListener { textoDigitado ->
            val query = textoDigitado.toString().trim()

            val filtrados = if (query.isEmpty()) {
                todosOsLivros
            } else {
                todosOsLivros.filter { livro ->
                    livro.title.contains(query, true) ||
                            livro.author.contains(query, true)
                }
            }

            adapter.atualizarLista(filtrados)
        }
    }

    override fun onBackPressed() {
        if (etSearchList.isVisible) fecharBusca() else super.onBackPressed()
    }

    private fun fecharBusca() {
        etSearchList.setText("")
        etSearchList.isVisible = false
        btnSearch.isVisible = true
    }

    private fun carregarLivros() {
        db.collection("Livros")
            .get()
            .addOnSuccessListener { resultado ->
                todosOsLivros.clear()

                for (documento in resultado) {
                    val autores = (documento.get("autores") as? List<*>)
                        ?.filterIsInstance<String>()
                        ?.joinToString(", ")
                        ?: "Autor não informado"

                    todosOsLivros.add(
                        Book(
                            id         = documento.id,
                            title      = documento.getString("titulo") ?: "Sem título",
                            author     = autores,
                            capaBase64 = documento.getString("capaUrl"),
                            imageUrl   = R.drawable.bg_book_cover_placeholder
                        )
                    )
                }
                adapter.atualizarLista(todosOsLivros)
            }
            .addOnFailureListener { erro ->
                Toast.makeText(this, "Erro ao carregar livros: ${erro.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
}

