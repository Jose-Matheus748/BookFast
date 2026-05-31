package com.example.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
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
    private lateinit var imgLivroIndisponivel: ImageView
    private lateinit var tituloLivroIndisponivel: TextView
    private lateinit var autoresLivroIndisponivel: TextView
    private lateinit var checkLivroIndisponivel: CheckBox
    private lateinit var cardLivroIndisponivel: View
    private lateinit var dataDisponibilidade: TextView
    private var livroIndisponivelId: String = ""

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
        carregarLivroIndisponivel()

        btnReservarTodosOsLivros.setOnClickListener {
            reservarTodosOsLivrosSelecionados()
        }

        btnEntrarNaFila.setOnClickListener {
            if (livroIndisponivelId.isEmpty()) {
                Toast.makeText(this, "Livro indisponível não carregado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!checkLivroIndisponivel.isChecked) {
                Toast.makeText(this, "Marque o livro antes de entrar na fila.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            entrarNaFila(livroIndisponivelId)
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
        imgLivroIndisponivel            = findViewById(R.id.imgLivroIndisponivel)
        tituloLivroIndisponivel         = findViewById(R.id.tituloLivroIndisponivel)
        autoresLivroIndisponivel        = findViewById(R.id.autoresLivroIndisponivel)
        checkLivroIndisponivel          = findViewById(R.id.checkLivroIndisponivel)
        cardLivroIndisponivel           = findViewById(R.id.cardLivroIndisponivel)
        dataDisponibilidade             = findViewById(R.id.dataDisponibilidade)

        cardLivros = findViewById(R.id.cardLivros)
        cardLivros.visibility = View.GONE
        cardLivroIndisponivel.visibility = View.GONE
    }

    private fun prepararRecyclerView() {
        adapter = SelectedBookAdapter(
            livros = livrosSelecionados,
            onReservar = { livro -> reservarLivro(livro) },
            onRemover = { livro -> removerLivroSelecionado(livro) }
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
                    "nomeUsuario"   to "Nome do Usuário 1",
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
        if (livro.id.isEmpty()) {
            Toast.makeText(this, "Livro não encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        val pedidoId = "$usuarioId-${livro.id}"

        val pedido = hashMapOf(
            "dataPedido"    to Timestamp.now(),
            "usuarioId"     to usuarioId,
            "nomeUsuario"   to "Nome do Usuário 1",
            "livroId"       to livro.id,
            "tituloLivro"   to livro.title,
            "autoresLivro"  to livro.author,
            "capaUrl"       to (livro.capaBase64 ?: ""),
            "status"        to "pendente"
        )

        db.collection("Pedidos")
            .document(pedidoId)
            .set(pedido)
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
                    "Erro ao enviar pedido: ${erro.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun reservarTodosOsLivrosSelecionados() {
        if (livrosSelecionados.isEmpty()) {
            Toast.makeText(this, "Você não selecionou nenhum livro.", Toast.LENGTH_SHORT).show()
            return
        }

        for (livro in livrosSelecionados) {
            val pedidoId = "$usuarioId-${livro.id}"

            val pedido = hashMapOf(
                "dataPedido"    to Timestamp.now(),
                "usuarioId"     to usuarioId,
                "nomeUsuario"   to "Nome do Usuário 1",
                "livroId"       to livro.id,
                "tituloLivro"   to livro.title,
                "autoresLivro"  to livro.author,
                "capaUrl"       to (livro.capaBase64 ?: ""),
                "status"        to "pendente"
            )

            db.collection("Pedidos")
                .document(pedidoId)
                .set(pedido)
        }

        Toast.makeText(
            this,
            "Todos os pedidos foram enviados para aprovação!",
            Toast.LENGTH_SHORT
        ).show()

        finish()
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

    private fun entrarNaFila(livroId: String) {

        Log.e("Bookfast:", "livroId: $livroId")

        if (livroId.isEmpty()) {
            Toast.makeText(this, "Livro não encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        val filaId = "$usuarioId-$livroId"

        val fila = hashMapOf(
            "usuarioId" to usuarioId,
            "livroId" to livroId,
            "dataEntradaFila" to Timestamp.now()
        )

        db.collection("Filas")
            .document(filaId)
            .set(fila)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Você entrou na fila de espera!",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
            .addOnFailureListener { erro ->
                Toast.makeText(
                    this,
                    "Erro ao entrar na fila: ${erro.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun carregarLivroIndisponivel() {
        livroIndisponivelId = ""
        cardLivroIndisponivel.visibility = View.GONE

        db.collection("Livros")
            .get()
            .addOnSuccessListener { resultadoLivros ->
                for (docLivro in resultadoLivros) {
                    val livroId = docLivro.id

                    db.collection("Livros")
                        .document(livroId)
                        .collection("Exemplares")
                        .get()
                        .addOnSuccessListener { exemplares ->
                            if (livroIndisponivelId.isNotEmpty()) {
                                return@addOnSuccessListener
                            }

                            val temExemplarDisponivel = exemplares.any { exemplar ->
                                val situacao = exemplar.getString("situacao")?.trim() ?: ""

                                situacao.equals("Disponível", ignoreCase = true) ||
                                        situacao.equals("Disponivel", ignoreCase = true)
                            }

                            if (!temExemplarDisponivel) {
                                livroIndisponivelId = livroId

                                val titulo = docLivro.getString("titulo") ?: "Sem título"

                                val autores = (docLivro.get("autores") as? List<*>)
                                    ?.filterIsInstance<String>()
                                    ?.joinToString(", ")
                                    ?: "Autor não informado"

                                val capaBase64 = docLivro.getString("capaUrl")

                                tituloLivroIndisponivel.text = titulo
                                autoresLivroIndisponivel.text = autores

                                carregarCapaLivroIndisponivel(capaBase64)

                                cardLivroIndisponivel.visibility = View.VISIBLE
                            }
                        }
                }
            }
            .addOnFailureListener { erro ->
                Toast.makeText(
                    this,
                    "Erro ao carregar livro indisponível: ${erro.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun carregarCapaLivroIndisponivel(capaBase64: String?) {
        if (!capaBase64.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(capaBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imgLivroIndisponivel.setImageBitmap(bitmap)
                return
            } catch (erro: Exception) {
                Log.e("BookSelection", "Erro ao carregar capa do livro indisponível", erro)
            }
        }

        imgLivroIndisponivel.setImageResource(R.drawable.bg_book_cover_placeholder)
    }
}