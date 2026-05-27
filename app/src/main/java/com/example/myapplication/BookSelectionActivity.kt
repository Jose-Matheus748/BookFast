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

        cardLivros = findViewById(R.id.cardLivros)
        cardLivros.visibility = View.GONE
    }

    private fun prepararRecyclerView() {
        adapter = SelectedBookAdapter(
            livros = livrosSelecionados,
                onReservar =  { livro -> reservarLivro(livro) },
                onRemover  =  { livro -> removerLivroSelecionado(livro) }
        )

        recyclerLivrosSelecionados.layoutManager = LinearLayoutManager(this)
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

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun salvarLivroRecebidoComoSelecionado() {
        val livroId = intent.getStringExtra("LIVRO_ID") ?: return
        val reservaId = "$usuarioId-$livroId"

        db.collection("Livros")
            .document(livroId)
            .get()
            .addOnSuccessListener { docLivro ->
                if (!docLivro.exists()) return@addOnSuccessListener

                val autores = (docLivro.get("autores") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.joinToString(", ")
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
                    .set(reserva)
                    .addOnSuccessListener {
                        carregarLivrosSelecionados()
                    }
            }
    }

    private fun carregarLivrosSelecionados() {
        db.collection("Reservas")
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("status", "selecionado")
            .get()
            .addOnSuccessListener { resultado ->
                livrosSelecionados.clear()

                for (docLivroSelecionado in resultado) {
                    val livro = Book(
                        id          = docLivroSelecionado.getString("livroId") ?: "",
                        title       = docLivroSelecionado.getString("tituloLivro") ?: "Sem título",
                        author      = docLivroSelecionado.getString("autoresLivro") ?: "Sem autores",
                        capaBase64  = docLivroSelecionado.getString("capaUrl"),
                        imageUrl    = R.drawable.bg_book_cover_placeholder
                    )
                    livrosSelecionados.add(livro)
                }

                adapter.atualizarLista(livrosSelecionados)

                if (livrosSelecionados.isEmpty()) {
                    textNenhumLivroSelecionado.visibility = View.VISIBLE
                    recyclerLivrosSelecionados.visibility = View.GONE
                } else {
                    textNenhumLivroSelecionado.visibility = View.GONE
                    recyclerLivrosSelecionados.visibility = View.VISIBLE
                }
            }
    }

    private fun reservarLivro(livro: Book) {
        val reservaId = "$usuarioId-${livro.id}"

        db.collection("Reservas")
            .document(reservaId)
            .update(
                mapOf(
                    "status"      to "pendente",
                    "dataReserva" to Timestamp.now()
                )
            )
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Sua solicitação foi enviada, aguarde aprovação!",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
            .addOnFailureListener { erro ->
                Toast.makeText(
                    this,
                    "Erro ao reservar: ${erro.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun removerLivroSelecionado(livro: Book) {
        val reservaId = "$usuarioId-${livro.id}"

        db.collection("Reservas")
            .document(reservaId)
            .delete()
            .addOnSuccessListener {
                carregarLivrosSelecionados()
            }
    }
}