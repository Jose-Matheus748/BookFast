package com.example.myapplication

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
    private lateinit var recyclerBooks: RecyclerView // RecyclerView para mostrar os livros
    private lateinit var adapter: BookAdapter // Adapter para a RecyclerView

    private val db = Firebase.firestore // Conexão com o Firestore

    private val todosOsLivros = mutableListOf<Book>() // Lista para armazenar todos os livros

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_list)

        iniciarViews()
        prepararRecycler()
        fazerPesquisa()
        carregarLivrosDoFirestore()

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)
    }

    // função para iniciar as views
    private fun iniciarViews() {
        btnSearch = findViewById(R.id.btnSearch)
        etSearchList = findViewById(R.id.etSearch)
        recyclerBooks = findViewById(R.id.recyclerBooks)
    }

    private fun prepararRecycler() {
        adapter = BookAdapter(todosOsLivros)

        recyclerBooks.apply {
            layoutManager = GridLayoutManager(this@SearchListActivity, 2) // divide a coluna do layout em 2
            setHasFixedSize(true) // otimiza a lista quando os itens têm tamanho previsível
            adapter = this@SearchListActivity.adapter // Liga o adapter ao RecyclerView
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

    private fun carregarLivrosDoFirestore() {
        db.collection("Livros")
            .get()
            .addOnSuccessListener { resultado ->
                todosOsLivros.clear()

                for (documento in resultado) {
                    val titulo = documento.getString("titulo") ?: "Sem título"
                    val listaAutores = documento.get("autores") as? List<*>

                    val autores = listaAutores
                        ?.filterIsInstance<String>()
                        ?.joinToString(", ")
                        ?: "Autor não informado"

                    val capaBase64 = documento.getString("capaUrl")

                    val livro = Book(
                        title = titulo,
                        author = autores,
                        imageUrl = R.drawable.bg_book_cover_placeholder,
                        capaBase64 = capaBase64
                    )
                    todosOsLivros.add(livro)
                }
                adapter.atualizarLista(todosOsLivros)
            }
            .addOnFailureListener { erro ->
                Toast.makeText(this, "Erro ao carregar livros: ${erro.message}", Toast.LENGTH_SHORT).show()
            }
    }
}