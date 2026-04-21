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
import com.example.myapplication.model.Book

class SearchListActivity : AppCompatActivity() {

    lateinit var btnSearch: ImageButton
    lateinit var etSearchList: EditText
    lateinit var adapter: BookAdapter
    lateinit var recyclerBooks: RecyclerView
    lateinit var mainSearchListLayout: View

    val todosOsLivros = mutableListOf(
        Book("Livro 10", "Autor 10", R.drawable.livro11),
        Book("Livro 11", "Autor 11", R.drawable.livro12),
        Book("Livro 12", "Autor 12", R.drawable.livro13),
        Book("Livro 13", "Autor 13", R.drawable.livro14),
        Book("Livro 14", "Autor 14", R.drawable.livro15),
        Book("Livro 15", "Autor 15", R.drawable.livro16),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search_list)

        btnSearch = findViewById(R.id.btnSearch)
        etSearchList = findViewById(R.id.etSearch)
        recyclerBooks = findViewById(R.id.recyclerBooks)
        mainSearchListLayout = findViewById(R.id.mainSearchListLayout)

        adapter = BookAdapter(todosOsLivros)

        recyclerBooks.layoutManager = GridLayoutManager(this, 2)

        recyclerBooks.adapter = adapter

        btnSearch.setOnClickListener {
            etSearchList.visibility = View.VISIBLE
            btnSearch.visibility = View.GONE
        }

        etSearchList.setOnClickListener {
            // impede fechar ao clicar dentro dele
        }

        mainSearchListLayout.setOnClickListener {
            if (etSearchList.isVisible) {
                fecharBusca()
            }
        }

        etSearchList.addTextChangedListener { text ->

            val query = text.toString().trim()

            val livrosFiltrados = todosOsLivros.filter { livro ->
                livro.title.contains(query, ignoreCase = true) ||
                livro.author.contains(query, ignoreCase = true)
            }

            adapter.atualizarLista(livrosFiltrados)
        }
    }

    override fun onBackPressed() {
        if (etSearchList.isVisible) {
            fecharBusca()
        } else {
            return super.onBackPressed()
        }
    }

    private fun fecharBusca() {
        etSearchList.setText("")
        etSearchList.visibility = View.GONE
        btnSearch.visibility = View.VISIBLE
    }
}