package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.BookAdapter
import com.example.myapplication.model.Book

class SearchListActivity : AppCompatActivity() {

    private lateinit var btnSearch: ImageButton
    private lateinit var etSearchList: EditText
    private lateinit var recyclerBooks: RecyclerView
    private lateinit var adapter: BookAdapter

    private val todosOsLivros = listOf(
        Book("Como elaborar projetos de pesquisa", "Antonio Carlos Gil", R.drawable.livro11),
        Book("Metodologia Científica na era digital", "João Mattar", R.drawable.livro12),
        Book("O mito da neutralidade científica", "Hilton Japiassu", R.drawable.livro13),
        Book("Os usos sociais das ciências", "Pierre Bourdieu", R.drawable.livro14),
        Book("Um discurso sobre as ciências", "Sousa de Santos", R.drawable.livro15),
        Book("As árvores de conhecimento", "Pierre Lévy", R.drawable.livro16),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_list)

        initViews()
        setupRecycler()
        setupSearch()
        FooterNavigation.setup(this)
    }

    private fun initViews() {
        btnSearch = findViewById(R.id.btnSearch)
        etSearchList = findViewById(R.id.etSearch)
        recyclerBooks = findViewById(R.id.recyclerBooks)
    }

    private fun setupRecycler() {
        adapter = BookAdapter(todosOsLivros)

        recyclerBooks.apply {
            layoutManager = GridLayoutManager(this@SearchListActivity, 2)
            setHasFixedSize(true)
            adapter = this@SearchListActivity.adapter
        }
    }

    private fun setupSearch() {

        btnSearch.setOnClickListener {
            etSearchList.isVisible = true
            btnSearch.isVisible = false
            etSearchList.requestFocus()
        }

        etSearchList.addTextChangedListener { text ->
            val query = text.toString().trim()

            val filtrados = if (query.isEmpty()) {
                todosOsLivros
            } else {
                todosOsLivros.filter {
                    it.title.contains(query, true) ||
                            it.author.contains(query, true)
                }
            }

            adapter.atualizarLista(filtrados)
        }
    }

    override fun onBackPressed() {
        if (etSearchList.isVisible) {
            fecharBusca()
        } else {
            super.onBackPressed()
        }
    }

    private fun fecharBusca() {
        etSearchList.setText("")
        etSearchList.isVisible = false
        btnSearch.isVisible = true
    }
}