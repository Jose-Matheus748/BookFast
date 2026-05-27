package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.firebase.Timestamp
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.BookAdapter
import com.example.myapplication.model.Book
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class BookSelectionActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var painelSelecionados: ScrollView
    private lateinit var painelReservas: ScrollView
    private lateinit var recyclerLivrosSelecionados: RecyclerView
    private lateinit var adapter: BookAdapter

    // IDs dos livros selecionados na activity
    private lateinit var tituloLivroSelecionado: TextView
    private lateinit var autoresLivroSelecionado: TextView
    private lateinit var capaLivroSelecionado: ImageView

    private lateinit var btnReservarLivros: Button
    private var livroSelecionadoId: String = ""

    private val db = Firebase.firestore

    private val livrosDoFirestore = mutableListOf<Book>()

    lateinit var imgFortaleza300 : ImageView
    lateinit var imgFortaleza300Alugado : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_selection)

        iniciarViews()
        prepararRecyclerView()
        configurarAbas()
        carregarLivroSelecionado()

        btnReservarLivros.setOnClickListener {
            reservarLivro()
        }

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)
    }

    private fun iniciarViews() {
        tabLayout = findViewById(R.id.tabLayout)
        painelSelecionados = findViewById(R.id.painelSelecionados)
        painelReservas = findViewById(R.id.painelReservas)
        recyclerLivrosSelecionados = findViewById(R.id.recyclerLivrosSelecionados)

        tituloLivroSelecionado = findViewById(R.id.tituloLivro)
        autoresLivroSelecionado = findViewById(R.id.autoresLivro)
        capaLivroSelecionado = findViewById(R.id.capaFortaleza)
        btnReservarLivros = findViewById(R.id.btnReservarLivros)

        imgFortaleza300 = findViewById(R.id.capaFortaleza)
        imgFortaleza300Alugado = findViewById(R.id.capaFortalezaAlugado)
    }

    private fun prepararRecyclerView() {
        adapter = BookAdapter(livrosDoFirestore) { livro ->
            val intent = Intent(this, BookpageActivity::class.java)
            intent.putExtra("LIVRO_ID", livro.id) // envia o ID do livro para a BookpageActivity; esse ID vem do firestore
            startActivity(intent)
        }

        recyclerLivrosSelecionados.apply {
            layoutManager = GridLayoutManager(this@BookSelectionActivity, 2)
            adapter = this@BookSelectionActivity.adapter
        }
    }

    private fun configurarAbas() {
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> { // aba de selecionados
                        painelSelecionados.visibility = View.VISIBLE
                        painelReservas.visibility     = View.GONE
                    }

                    1 -> { // aba de reservados
                        painelSelecionados.visibility = View.GONE
                        painelReservas.visibility     = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun carregarIdDoLivroSelecionado(): String? {
        val livroId = intent.getStringExtra("LIVRO_ID") ?: ""

        if (livroId.isEmpty()) {
            Toast.makeText(
                this,
                "Livro não encontrado",
                Toast.LENGTH_SHORT)
                .show()
            finish()
            return null
        }

        Log.d("BookFast", "ID do livro recebido: $livroId")

        return livroId
    }

    private fun carregarLivroSelecionado() {
        val livroSelecionadoId = carregarIdDoLivroSelecionado() ?: return

        db.collection("Livros")
            .document(livroSelecionadoId) // pega APENAS o livro clicado
            .get()
            .addOnSuccessListener { documentoLivro ->
                val titulo = documentoLivro.getString("titulo") ?: "Sem título"

                val autores = (documentoLivro.get("autores") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.joinToString(", ")
                    ?: "Autor não informado"

                val capaBase64 = documentoLivro.getString("capaUrl") ?: ""

                carregarCapaDoLivro(capaBase64)

                tituloLivroSelecionado.text = titulo
                autoresLivroSelecionado.text = autores
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar livro selecionado", Toast.LENGTH_SHORT).show()
            }

    }

    private fun carregarCapaDoLivro(capaDoLivro: String) {
        if (capaDoLivro.isNotEmpty()) {
            try {
                val bytes = Base64.decode(capaDoLivro, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                capaLivroSelecionado.setImageBitmap(bitmap)
            } catch (erro: Exception) {
                Log.d("BookFast", "Erro ao carregar capa do livro selecionado: $erro")
                capaLivroSelecionado.setImageResource(R.drawable.bg_book_cover_placeholder)
            }
        } else {
            Log.d("Bookfast", "capa do livro pode estar vazia, ou não existir")
            capaLivroSelecionado.setImageResource(R.drawable.bg_book_cover_placeholder)
        }
    }

    private fun reservarLivro() {
        livroSelecionadoId = carregarIdDoLivroSelecionado() ?: return

        if (livroSelecionadoId.isEmpty()) {
            Toast.makeText(this, "Livro não selecionado", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("BookFast", "Reservando livro com ID: $livroSelecionadoId")

        val usuarioId = "PD5OhKp1uVrtfkOnsEI2" // ID de usuario para teste, ainda tem que mudar para ser o usuario logado

        val reserva = hashMapOf(
            "dataReserva"   to  Timestamp.now(),
            "usuarioId"     to  usuarioId,
            "tituloLivro"   to  tituloLivroSelecionado.text.toString(),
            "livroId"       to  livroSelecionadoId,
            "autoresLivro"  to  autoresLivroSelecionado.text.toString(),
            "status"        to  "pendente" // usuario solicita reserva do livro, e inicialmente fica pendente para o admin aprovar
        )

        db.collection("Reservas")
            .add(reserva)
            .addOnSuccessListener {
                Toast.makeText(this, "Reserva enviada com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { erro ->
                btnReservarLivros.isEnabled = true
                Toast.makeText(this, "Erro ao enviar solicitação de reserva: ${erro.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
