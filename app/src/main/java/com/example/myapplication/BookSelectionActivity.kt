package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.SelectedBookAdapter
import com.example.myapplication.model.Book
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore

class BookSelectionActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var painelSelecionados: LinearLayout
    private lateinit var painelReservas: ScrollView
    private lateinit var recyclerLivrosSelecionados: RecyclerView
    private lateinit var textNenhumLivroSelecionado: TextView
    private lateinit var btnReservarLivrosSelecionados: Button
    private lateinit var btnReservarTodosOsLivros: Button
    private lateinit var btnEntrarNaFila: Button

    private lateinit var cardLivros: View
    private lateinit var adapter: SelectedBookAdapter

    private val db = Firebase.firestore
    private val livrosSelecionados = mutableListOf<Book>()

    private val usuarioId = "kcPVgjQXRdJjqXkA85m8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_selection)

        iniciarViews()
        prepararRecyclerView()
        configurarAbas()

        salvarLivroRecebidoComoSelecionado()
        carregarLivrosSelecionados()

        btnReservarTodosOsLivros.setOnClickListener {
            reservarTodosOsLivrosSelecionados()
        }

        btnEntrarNaFila.setOnClickListener {
            val livroId = "id-livro-indisponivel"
            entrarNaFila(livroId)
        }

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)
    }

    private fun iniciarViews() {
        tabLayout                       = findViewById(R.id.tabLayout)
        painelSelecionados              = findViewById(R.id.painelSelecionados)
        painelReservas                  = findViewById(R.id.painelReservas)
        recyclerLivrosSelecionados      = findViewById(R.id.recyclerLivrosSelecionados)
        textNenhumLivroSelecionado      = findViewById(R.id.textNenhumLivroSelecionado)
        btnReservarLivrosSelecionados   = findViewById(R.id.btnReservarLivrosSelecionados)
        btnReservarTodosOsLivros        = findViewById(R.id.btnReservarTodosOsLivros)
        btnEntrarNaFila                 = findViewById(R.id.btnEntrarNaFila)

        cardLivros = findViewById(R.id.cardLivros)
        cardLivros.visibility = View.GONE // esconde o card mockado/antigo para deixar so a lista real aparecer
    }

    private fun prepararRecyclerView() {
        adapter = SelectedBookAdapter(
            livros = livrosSelecionados,
                onReservar =  { livro -> reservarLivro(livro) },
                onRemover  =  { livro -> removerLivroSelecionado(livro) }
        )

        recyclerLivrosSelecionados.layoutManager = LinearLayoutManager(this) // define que os cards aparecem um embaixo do outro
        recyclerLivrosSelecionados.adapter = adapter
    }

    private fun configurarAbas() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        painelSelecionados.visibility = View.VISIBLE
                        painelReservas.visibility = View.GONE
                    }

                    1 -> {
                        painelSelecionados.visibility = View.GONE
                        painelReservas.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {} // quando uma aba deixa de estar selecionada
            override fun onTabReselected(tab: TabLayout.Tab) {} // quanto o usuario clica de novo na aba que ja foi selecionada
        })
    }

    // pega o livro recebido da tela anterior e grava ele em Reservas
    private fun salvarLivroRecebidoComoSelecionado() {
        val livroId = intent.getStringExtra("LIVRO_ID") ?: return
        val reservaId = "$usuarioId-$livroId" // id unico para evitar duplicar o mesmo livro para o mesmo usuario

        db.collection("Livros")
            .document(livroId)
            .get() // consulta no banco de dados esse documento com esse livroId
            .addOnSuccessListener { docLivro ->
                if (!docLivro.exists()) return@addOnSuccessListener // se o documento não existir. pare somente este bloco do addSuccesListener

                val autores = (docLivro.get("autores") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.joinToString(", ") // "autor1", "autor2" -> "autor1, autor2"

                    ?: "Autor não informado"

                val reserva = hashMapOf(
                    "dataReserva"   to Timestamp.now(),
                    "usuarioId"     to usuarioId,
                    "livroId"       to livroId,
                    "tituloLivro"   to (docLivro.getString("titulo") ?: "Sem título"),
                    "autoresLivro"  to autores,
                    "capaUrl"       to (docLivro.getString("capaUrl") ?: ""),
                    "status"        to "selecionado"
                )

                db.collection("Reservas")
                    .document(reservaId)
                    .set(reserva) // salva no banco de dados, a reserva
                    .addOnSuccessListener { // quando salvar der certo, atualiza a tela, buscando novamente os livros selecionados pelo usuario
                        carregarLivrosSelecionados() // para poder aparecer na lista depois que eu salvar
                    }
            }
    }

    // lê da coleção Reservas todos os livros selecionados daquele usuário e atualiza a lista na tela.
    private fun carregarLivrosSelecionados() {
        db.collection("Reservas")
            .whereEqualTo("usuarioId", usuarioId)   // na coleção, quero campos onde o usuarioId seja igual ao usuarioId atual
            .whereEqualTo("status", "selecionado")  // filtro para trazer apenas livros selecionados
            .get() // consulta no banco de dados esses documentos
            .addOnSuccessListener { resultado ->
                livrosSelecionados.clear() // limpa a lista local antes de adicionar os livros de novo, para não duplicar os livros na tela

                for (docLivroSelecionado in resultado) {
                    val livro = Book(
                        id          = docLivroSelecionado.getString("livroId") ?: "",
                        title       = docLivroSelecionado.getString("tituloLivro") ?: "Sem título",
                        author      = docLivroSelecionado.getString("autoresLivro") ?: "Sem autores",
                        capaBase64  = docLivroSelecionado.getString("capaUrl"),
                        imageUrl    = R.drawable.bg_book_cover_placeholder // se a capa Base64 não carregar, o app usa essa imagem padrão
                    )
                    livrosSelecionados.add(livro) // adiciona esse livro na lista local, para cada volta do for
                }

                adapter.atualizarLista(livrosSelecionados) // adapter atualiza a tela com a nova lista de livros

                if (livrosSelecionados.isEmpty()) {
                    textNenhumLivroSelecionado.visibility = View.VISIBLE
                    recyclerLivrosSelecionados.visibility = View.GONE
                } else {
                    textNenhumLivroSelecionado.visibility = View.GONE
                    recyclerLivrosSelecionados.visibility = View.VISIBLE
                }
            }
    }


    private fun reservarLivro(livro: Book) { // a função precisa saber qual livro está sendo clicado
        val reservaId = "$usuarioId-${livro.id}"

        db.collection("Reservas")
            .document(reservaId)
            .update( // para atualizar campos de um documento ja existente
                mapOf( // lista de campos para serem atualizados dentro do documento
                    "status"      to "pendente",
                    "dataReserva" to Timestamp.now()
                )
            )
            .addOnSuccessListener { // usuario solicitou a reserva, agora precisa de aprovação do admin
                Toast.makeText(this, "Sua solicitação foi enviada, aguarde aprovação!", Toast.LENGTH_SHORT).show()
                finish() // termina, e volta a tela anterior
            }
            .addOnFailureListener { erro ->
                Toast.makeText(this, "Erro ao reservar: ${erro.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun reservarTodosOsLivrosSelecionados() {
        if (livrosSelecionados.isEmpty()) { // Isso impede que o código continue tentando reservar uma lista vazia.
            Toast.makeText(this, "Você não selecionou nenhum livro.", Toast.LENGTH_SHORT).show()
            return
        }

        for (livro in livrosSelecionados) {
            val reservaId = "$usuarioId-${livro.id}" // para cara livro, cria um id do documento no firestore

            db.collection("Reservas")
                .document(reservaId)
                .update(
                    mapOf(
                            "status"      to "pendente",
                            "dataReserva" to Timestamp.now()
                    )
                )
        }

        Toast.makeText(this, "Todos os livros selecionados foram enviados para reserva!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun removerLivroSelecionado(livro: Book) {
        val reservaId = "$usuarioId-${livro.id}"

        db.collection("Reservas")
            .document(reservaId)
            .delete() // apaga o livro no banco de dados, e ele deixa de estar selecionado para aquele usuario, caso eu desmarque o checkbox
            .addOnSuccessListener {
                carregarLivrosSelecionados() // atualiza a tela com a lista de livros selecionados, sem o livro que eu deletei
            }
    }

    private fun entrarNaFila(livroId: String) { // a função precisa saber o id do livro para por na fila
        if (livroId.isEmpty()) {
            Toast.makeText(this, "Livro não encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        val filaId = "$usuarioId-$livroId"

        val fila = hashMapOf( // cria um mapa de dados para salvar na coleção Filas do firestoe
            "usuarioId"       to usuarioId,
            "livroId"         to livroId,
            "dataEntradaFila" to Timestamp.now()
        )

        db.collection("Filas")
            .document(filaId)
            .set(fila) // atualiza no banco dados o documento com os dados da fila
            .addOnSuccessListener {
                Toast.makeText(this, "Você entrou na fila de espera!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { erro ->
                Toast.makeText(this, "Erro ao entrar na fila: ${erro.message}", Toast.LENGTH_SHORT).show()
            }
    }
}