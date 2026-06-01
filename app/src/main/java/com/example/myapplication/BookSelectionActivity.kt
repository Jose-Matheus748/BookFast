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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.SelectedBookAdapter
import com.example.myapplication.model.Book
import com.example.myapplication.utils.MultaUtils
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

    private lateinit var containerEmprestimos: LinearLayout
    private lateinit var containerFilaEspera: LinearLayout
    private lateinit var containerLivrosAtrasados: LinearLayout
    private lateinit var txtTotalEmprestimos: TextView
    private lateinit var txtTotalFilaEspera: TextView
    private lateinit var txtValorTotalMultas: TextView

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
        textNenhumLivroSelecionado      =  findViewById(R.id.textNenhumLivroSelecionado)
        btnReservarLivrosSelecionados   = findViewById(R.id.btnReservarLivrosSelecionados)
        btnReservarTodosOsLivros        = findViewById(R.id.btnReservarTodosOsLivros)
        btnEntrarNaFila                 = findViewById(R.id.btnEntrarNaFila)
        imgLivroIndisponivel            = findViewById(R.id.imgLivroIndisponivel)
        tituloLivroIndisponivel         = findViewById(R.id.tituloLivroIndisponivel)
        autoresLivroIndisponivel        = findViewById(R.id.autoresLivroIndisponivel)
        checkLivroIndisponivel          = findViewById(R.id.checkLivroIndisponivel)
        cardLivroIndisponivel           = findViewById(R.id.cardLivroIndisponivel)
        dataDisponibilidade             = findViewById(R.id.dataDisponibilidade)
        containerEmprestimos            = findViewById(R.id.containerEmprestimos)
        containerFilaEspera             = findViewById(R.id.containerFilaEspera)
        containerLivrosAtrasados        = findViewById(R.id.containerLivrosAtrasados)
        txtTotalEmprestimos             = findViewById(R.id.txtTotalEmprestimos)
        txtTotalFilaEspera              = findViewById(R.id.txtTotalFilaEspera)
        txtValorTotalMultas             = findViewById(R.id.txtValorTotalMultas)

        cardLivros                          = findViewById(R.id.cardLivros)
        cardLivros.visibility               = View.GONE
        cardLivroIndisponivel.visibility    = View.GONE
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

                        carregarEmprestimos()
                        carregarFilaDeEspera()
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
                    "dataReserva" to Timestamp.now(),
                    "usuarioId" to usuarioId,
                    "nomeUsuario" to "Nome do Usuário 1",
                    "livroId" to livroId,
                    "tituloLivro" to (docLivro.getString("titulo") ?: "Sem título"),
                    "autoresLivro" to autores,
                    "capaUrl" to (docLivro.getString("capaUrl") ?: ""),
                    "status" to "selecionado"
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
                        id = docLivroSelecionado.getString("livroId") ?: "",
                        title = docLivroSelecionado.getString("tituloLivro") ?: "Sem título",
                        author = docLivroSelecionado.getString("autoresLivro") ?: "Sem autores",
                        capaBase64 = docLivroSelecionado.getString("capaUrl"),
                        imageUrl = R.drawable.bg_book_cover_placeholder
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
            "dataPedido" to Timestamp.now(),
            "usuarioId" to usuarioId,
            "nomeUsuario" to "Nome do Usuário 1",
            "livroId" to livro.id,
            "tituloLivro" to livro.title,
            "autoresLivro" to livro.author,
            "capaUrl" to (livro.capaBase64 ?: ""),
            "status" to "pendente"
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
                "dataPedido" to Timestamp.now(),
                "usuarioId" to usuarioId,
                "nomeUsuario" to "Nome do Usuário 1",
                "livroId" to livro.id,
                "tituloLivro" to livro.title,
                "autoresLivro" to livro.author,
                "capaUrl" to (livro.capaBase64 ?: ""),
                "status" to "pendente"
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

    private fun carregarEmprestimos() {
        containerEmprestimos.removeAllViews()
        containerLivrosAtrasados.removeAllViews()

        db.collection("Pedidos")
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("status", "retirado")
            .get()
            .addOnSuccessListener { resultado ->
                txtTotalEmprestimos.text = "${resultado.size()}/10"
                var valorTotalMultas = 0

                for (doc in resultado) {
                    val titulo = doc.getString("tituloLivro") ?: "Sem título"
                    val autores = doc.getString("autoresLivro") ?: "Sem autores"
                    val capaUrl = doc.getString("capaUrl")
                    val livroId = doc.getString("livroId") ?: ""
                    val pedidoId = doc.id
                    val dataVencimento = doc.getTimestamp("dataVencimento")
                    val valorMultaLivro = MultaUtils.calcularValorMulta(dataVencimento)

                    valorTotalMultas += valorMultaLivro

                    val card = criarCardEmprestimo(
                        pedidoId = pedidoId,
                        livroId = livroId,
                        titulo = titulo,
                        autores = autores,
                        capaUrl = capaUrl,
                        dataVencimento = dataVencimento
                    )

                    containerEmprestimos.addView(card)

                    if (valorMultaLivro > 0) {
                        val cardAtrasado = criarCardLivroAtrasado(
                            titulo = titulo,
                            capaUrl = capaUrl,
                            dataVencimento = dataVencimento
                        )

                        containerLivrosAtrasados.addView(cardAtrasado)
                    }
                }

                txtValorTotalMultas.text = MultaUtils.formatarValorMulta(valorTotalMultas)
            }
    }

    private fun carregarFilaDeEspera() {
        containerFilaEspera.removeAllViews()

        db.collection("Pedidos")
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { resultado ->
                txtTotalFilaEspera.text = "${resultado.size()}/3"

                for (doc in resultado) {
                    val titulo = doc.getString("tituloLivro") ?: "Sem título"
                    val autores = doc.getString("autoresLivro") ?: "Sem autores"
                    val capaUrl = doc.getString("capaUrl")

                    val card = criarCardFilaEspera(
                        titulo = titulo,
                        autores = autores,
                        capaUrl = capaUrl,
                        textoStatus = "Aguardando aprovação"
                    )

                    containerFilaEspera.addView(card)
                }
            }
    }

    private fun criarCardEmprestimo(
        pedidoId: String,
        livroId: String,
        titulo: String,
        autores: String,
        capaUrl: String?,
        dataVencimento: Timestamp?
    ): View {
        val card = CardView(this).apply {
            setCardBackgroundColor(0xFF2E2E2E.toInt())
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(8))
            }
        }

        val conteudoCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val linhaLivro = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
        }

        val imagemLivro = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP

            layoutParams = LinearLayout.LayoutParams(
                dpToPx(72),
                dpToPx(100)
            )

            carregarCapaNoImageView(this, capaUrl)
        }

        val areaTextos = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(dpToPx(12), 0, 0, 0)
            }
        }

        val txtTitulo = TextView(this).apply {
            text = titulo
            textSize = 15f
            setTextColor(0xFFF0F0F0.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val txtAutores = TextView(this).apply {
            text = autores
            textSize = 13f
            setTextColor(0xFFAAAAAA.toInt())
        }

        val txtVencimento = TextView(this).apply {
            text = "Vencimento: ${MultaUtils.formatarData(dataVencimento)}"
            textSize = 12f
            setTextColor(0xFFF0C040.toInt())
        }

        areaTextos.addView(txtTitulo)
        areaTextos.addView(txtAutores)
        areaTextos.addView(txtVencimento)

        linhaLivro.addView(imagemLivro)
        linhaLivro.addView(areaTextos)

        val divisor = View(this).apply {
            setBackgroundColor(0xFF3A3A3A.toInt())

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(1)
            ).apply {
                setMargins(dpToPx(12), 0, dpToPx(12), 0)
            }
        }

        val btnRenovar = Button(this).apply {
            text = "Renovar"
            textSize = 14f
            setTextColor(0xFFF0F0F0.toInt())
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFF19A1E4.toInt())

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(40)
            ).apply {
                setMargins(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))
            }

            setOnClickListener {
                mostrarDialogoRenovacao(pedidoId, livroId, dataVencimento)
            }
        }

        conteudoCard.addView(linhaLivro)
        conteudoCard.addView(divisor)
        conteudoCard.addView(btnRenovar)

        card.addView(conteudoCard)

        return card
    }

    private fun criarCardFilaEspera(
        titulo: String,
        autores: String,
        capaUrl: String?,
        textoStatus: String
    ): View {
        val card = CardView(this).apply {
            setCardBackgroundColor(0xFF2E2E2E.toInt())
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(8))
            }
        }

        val conteudoCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val linhaLivro = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
        }

        val imagemLivro = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP

            layoutParams = LinearLayout.LayoutParams(
                dpToPx(72),
                dpToPx(100)
            )

            carregarCapaNoImageView(this, capaUrl)
        }

        val areaTextos = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(dpToPx(12), 0, 0, 0)
            }
        }

        val txtTitulo = TextView(this).apply {
            text = titulo
            textSize = 15f
            setTextColor(0xFFAAAAAA.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val txtAutores = TextView(this).apply {
            text = autores
            textSize = 13f
            setTextColor(0xFF777777.toInt())
        }

        val txtStatus = TextView(this).apply {
            text = textoStatus
            textSize = 12f
            setTextColor(0xFF19A1E4.toInt())
        }

        areaTextos.addView(txtTitulo)
        areaTextos.addView(txtAutores)
        areaTextos.addView(txtStatus)

        linhaLivro.addView(imagemLivro)
        linhaLivro.addView(areaTextos)

        conteudoCard.addView(linhaLivro)
        card.addView(conteudoCard)

        return card
    }

    private fun criarCardLivroAtrasado(
        titulo: String,
        capaUrl: String?,
        dataVencimento: Timestamp?
    ): View {
        val card = CardView(this).apply {
            setCardBackgroundColor(0xFF2E2E2E.toInt())
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(8))
            }
        }

        val linhaLivro = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
        }

        val imagemLivro = ImageView(this).apply {
            alpha = 0.4f
            scaleType = ImageView.ScaleType.CENTER_CROP

            layoutParams = LinearLayout.LayoutParams(
                dpToPx(72),
                dpToPx(100)
            )

            carregarCapaNoImageView(this, capaUrl)
        }

        val areaTextos = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(dpToPx(12), 0, 0, 0)
            }
        }

        val txtTitulo = TextView(this).apply {
            text = titulo
            textSize = 15f
            setTextColor(0xFFFF6B6B.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val txtVencido = TextView(this).apply {
            text = "VENCIDO"
            textSize = 10f
            setTextColor(0xFFFF6B6B.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            setBackgroundColor(0xFF5C1C1C.toInt())
            setPadding(dpToPx(8), dpToPx(2), dpToPx(8), dpToPx(2))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(4), 0, 0)
            }
        }

        val txtDesde = TextView(this).apply {
            text = "Desde: ${MultaUtils.formatarData(dataVencimento)}"
            textSize = 12f
            setTextColor(0xFF888888.toInt())

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(6), 0, 0)
            }
        }

        areaTextos.addView(txtTitulo)
        areaTextos.addView(txtVencido)
        areaTextos.addView(txtDesde)

        linhaLivro.addView(imagemLivro)
        linhaLivro.addView(areaTextos)
        card.addView(linhaLivro)

        return card
    }

    private fun mostrarDialogoRenovacao(
        pedidoId: String,
        livroId: String,
        dataVencimentoAtual: Timestamp?
    ) {
        AlertDialog.Builder(this)
            .setTitle("Renovar empréstimo")
            .setMessage("Deseja renovar o livro por mais 10 dias?")
            .setPositiveButton("Sim") { _, _ ->
                renovarEmprestimo(pedidoId, livroId, dataVencimentoAtual)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun renovarEmprestimo(
        pedidoId: String,
        livroId: String,
        dataVencimentoAtual: Timestamp?
    ) {
        db.collection("Filas")
            .whereEqualTo("livroId", livroId)
            .get()
            .addOnSuccessListener { resultadoFilas ->
                val existeUsuarioNaFila = resultadoFilas.documents.any { doc ->
                    doc.getString("usuarioId") != usuarioId
                }

                if (existeUsuarioNaFila) {
                    Toast.makeText(
                        this,
                        "Não é possível renovar. Existe usuário na fila.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }

                verificarPedidosPendentesAntesDeRenovar(
                    pedidoId,
                    livroId,
                    dataVencimentoAtual
                )
            }
    }

    private fun verificarPedidosPendentesAntesDeRenovar(
        pedidoId: String,
        livroId: String,
        dataVencimentoAtual: Timestamp?
    ) {
        db.collection("Pedidos")
            .whereEqualTo("livroId", livroId)
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { resultadoPedidos ->
                val existePedidoDeOutroUsuario = resultadoPedidos.documents.any { doc ->
                    doc.getString("usuarioId") != usuarioId
                }

                if (existePedidoDeOutroUsuario) {
                    Toast.makeText(
                        this,
                        "Não é possível renovar. Existe pedido pendente para este livro.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }

                val dataBaseRenovacao = dataVencimentoAtual?.toDate() ?: Timestamp.now().toDate()
                val novaDataVencimento = MultaUtils.gerarDataVencimento(dataBaseRenovacao)

                db.collection("Pedidos")
                    .document(pedidoId)
                    .update(
                        mapOf(
                            "dataRenovacao" to Timestamp.now(),
                            "dataVencimento" to novaDataVencimento
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Empréstimo renovado!",
                            Toast.LENGTH_SHORT
                        ).show()

                        carregarEmprestimos()
                    }
                    .addOnFailureListener { erro ->
                        Toast.makeText(
                            this,
                            "Erro ao renovar: ${erro.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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

    private fun carregarCapaNoImageView(imageView: ImageView, capaBase64: String?) {
        if (!capaBase64.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(capaBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageView.setImageBitmap(bitmap)
                return
            } catch (erro: Exception) {
                Log.e("BookSelection", "Erro ao carregar capa", erro)
            }
        }

        imageView.setImageResource(R.drawable.bg_book_cover_placeholder)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
