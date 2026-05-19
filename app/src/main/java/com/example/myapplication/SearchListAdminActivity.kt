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

class SearchListAdminActivity : AppCompatActivity() {

    private lateinit var btnSearch: ImageButton
    private lateinit var etSearchList: EditText
    private lateinit var recyclerBooks: RecyclerView
    private lateinit var adapter: BookAdapter

    private val db = Firebase.firestore

    private val todosOsLivros = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_list_admin)

        iniciarViews()
        prepararRecycler()
        fazerPesquisa()
        carregarLivrosDoFirestore()

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)
    }

    private fun iniciarViews() {
        btnSearch = findViewById(R.id.btnSearchAdmin) // Encontra o botão de busca admin.
        etSearchList = findViewById(R.id.etSearch) // Encontra o campo de busca.
        recyclerBooks = findViewById(R.id.recyclerBooks) // Encontra o RecyclerView.
    }

    private fun prepararRecycler() {
        adapter = BookAdapter(todosOsLivros)

        recyclerBooks.apply {
            layoutManager = GridLayoutManager(this@SearchListAdminActivity, 2) // Mostra 2 livros por linha
            setHasFixedSize(true) // Otimiza a lista quando os itens têm tamanho previsível.
            adapter = this@SearchListAdminActivity.adapter // Liga o adapter ao RecyclerView.
        }
    }

    private fun carregarLivrosDoFirestore() {
        db.collection("Livros") // Entra na coleção chamada 'Livros'
            .get() // Faz uma leitura única dos documentos.
            .addOnSuccessListener { resultado -> // Executa se a consulta der certo.
                todosOsLivros.clear() // Limpa a lista para não duplicar dados.

                for (documento in resultado) {
                    val titulo = documento.getString("titulo") ?: "Sem título"
                    val listaAutores = documento.get("autores") as? List<*> // Lê o campo autores como lista.

                    val autores = listaAutores
                        ?.filterIsInstance<String>() // Mantém apenas valores que forem texto.
                        ?.joinToString(", ") // Junta os autores separados por vírgula.
                        ?: "Autor não informado"

                    val capaBase64 = documento.getString("capaUrl")

                    val livro = Book(
                        title = titulo,
                        author = autores,
                        imageUrl = R.drawable.bg_book_cover_placeholder, // Define capa padrão caso a real falhe.
                        capaBase64 = capaBase64
                    )
                    todosOsLivros.add(livro) // adiciona o livro na lista principal
                }
                adapter.atualizarLista(todosOsLivros) // Atualiza a tela com os livros carregados.
            }
            .addOnFailureListener { erro -> // Executa se a consulta der errado.
                Toast.makeText(this, "Erro ao carregar livros: ${erro.message}", Toast.LENGTH_SHORT).show()
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
                todosOsLivros.filter { livro -> // Percorre a lista procurando correspondências.
                    livro.title.contains(query, true) ||        // Verifica se o título contém a busca.
                            livro.author.contains(query, true)  // Verifica se o autor contém a busca.
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

