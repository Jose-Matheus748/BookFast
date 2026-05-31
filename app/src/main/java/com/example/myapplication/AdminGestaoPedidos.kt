package com.example.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class AdminGestaoPedidos : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var painelReservas: ScrollView
    private lateinit var painelDevolucoes: ScrollView
    private lateinit var containerPedidos: LinearLayout
    private lateinit var containerDevolucoes: LinearLayout
    private lateinit var txtSemPedidos: TextView
    private lateinit var txtSemDevolucoes: TextView

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gestao_pedidos)

        iniciarViews()

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)

        tabLayout.getTabAt(0)?.select()
        carregarPedidos()
        configurarAbas()
    }

    private fun iniciarViews() {
        tabLayout = findViewById(R.id.tabLayout)
        painelReservas = findViewById(R.id.painelReservas)
        painelDevolucoes = findViewById(R.id.painelDevolucoes)
        containerPedidos = findViewById(R.id.containerPedidos)
        containerDevolucoes = findViewById(R.id.containerDevolucoes)
        txtSemPedidos = findViewById(R.id.txtSemPedidos)
        txtSemDevolucoes = findViewById(R.id.txtSemDevolucoes)
    }

    private fun configurarAbas() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        painelReservas.visibility = View.VISIBLE
                        painelDevolucoes.visibility = View.GONE
                        carregarPedidos()
                    }

                    1 -> {
                        painelReservas.visibility = View.GONE
                        painelDevolucoes.visibility = View.VISIBLE
                        carregarDevolucoes()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun carregarPedidos() {
        containerPedidos.removeAllViews()
        txtSemPedidos.visibility = View.GONE

        db.collection("Pedidos")
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    txtSemPedidos.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                val pedidosPorUsuario = LinkedHashMap<String, MutableList<Map<String, Any?>>>()

                for (doc in resultado) {
                    val usuarioId = doc.getString("usuarioId") ?: continue

                    val pedido = mapOf(
                        "pedidoId" to doc.id,
                        "usuarioId" to usuarioId,
                        "nomeUsuario" to doc.getString("nomeUsuario"),
                        "livroId" to doc.getString("livroId"),
                        "tituloLivro" to doc.getString("tituloLivro"),
                        "autoresLivro" to doc.getString("autoresLivro"),
                        "capaUrl" to doc.getString("capaUrl")
                    )

                    pedidosPorUsuario
                        .getOrPut(usuarioId) { mutableListOf() }
                        .add(pedido)
                }

                for ((_, pedidos) in pedidosPorUsuario) {
                    val nomeUsuario = pedidos.first()["nomeUsuario"] as? String ?: "Usuário"
                    adicionarGrupoPedido(nomeUsuario, pedidos)
                }
            }
            .addOnFailureListener { erro ->
                Toast.makeText(
                    this,
                    "Erro ao carregar pedidos: ${erro.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun adicionarGrupoPedido(
        nomeUsuario: String,
        pedidos: List<Map<String, Any?>>
    ) {
        val ctx = this

        val tvNome = TextView(ctx).apply {
            text = nomeUsuario
            textSize = 18f
            setTextColor(0xFFF1F1F1.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(14), 0, dpToPx(6))
            }
        }

        containerPedidos.addView(tvNome)

        val card = CardView(ctx).apply {
            setCardBackgroundColor(0xFF2E2E2E.toInt())
            radius = dpToPx(8).toFloat()
            cardElevation = dpToPx(2).toFloat()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(8))
            }
        }

        val cardContent = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            val padding = dpToPx(12)
            setPadding(padding, padding, padding, padding)
        }

        for ((index, pedido) in pedidos.withIndex()) {
            if (index > 0) {
                val divisor = View(ctx).apply {
                    setBackgroundColor(0xFF3A3A3A.toInt())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(1)
                    ).apply {
                        setMargins(0, dpToPx(14), 0, dpToPx(14))
                    }
                }

                cardContent.addView(divisor)
            }

            cardContent.addView(criarLinhaLivro(pedido))
        }

        val tvPergunta = TextView(ctx).apply {
            text = "Retirada realizada?"
            textSize = 15f
            setTextColor(0xFFF1F1F1.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(14), 0, dpToPx(8))
            }
        }

        cardContent.addView(tvPergunta)

        val rowBotoes = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(42)
            )
        }

        val btnConfirmar = Button(ctx).apply {
            text = "Confirmar"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 13f
            isAllCaps = false
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFF19A1E4.toInt())

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            ).apply {
                setMargins(0, 0, dpToPx(8), 0)
            }

            setOnClickListener {
                confirmarRetirada(pedidos)
            }
        }

        val btnNegar = Button(ctx).apply {
            text = "Negar"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 13f
            isAllCaps = false
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFF434343.toInt())

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )

            setOnClickListener {
                negarRetirada(pedidos)
            }
        }

        rowBotoes.addView(btnConfirmar)
        rowBotoes.addView(btnNegar)

        cardContent.addView(rowBotoes)
        card.addView(cardContent)

        containerPedidos.addView(card)
    }

    private fun confirmarRetirada(pedidos: List<Map<String, Any?>>) {
        for (pedido in pedidos) {
            val pedidoId = pedido["pedidoId"] as? String ?: continue

            db.collection("Pedidos")
                .document(pedidoId)
                .update("status", "retirado")
        }

        Toast.makeText(this, "Retirada confirmada!", Toast.LENGTH_SHORT).show()
        carregarPedidos()
    }

    private fun negarRetirada(pedidos: List<Map<String, Any?>>) {
        for (pedido in pedidos) {
            val pedidoId = pedido["pedidoId"] as? String ?: continue

            db.collection("Pedidos")
                .document(pedidoId)
                .update("status", "nao_retirado")
        }

        Toast.makeText(this, "Pedido negado.", Toast.LENGTH_SHORT).show()
        carregarPedidos()
    }

    private fun carregarDevolucoes() {
        containerDevolucoes.removeAllViews()
        txtSemDevolucoes.visibility = View.GONE

        db.collection("Pedidos")
            .whereEqualTo("status", "retirado")
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    txtSemDevolucoes.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                val pedidosPorUsuario = LinkedHashMap<String, MutableList<Map<String, Any?>>>()

                for (doc in resultado) {
                    val usuarioId = doc.getString("usuarioId") ?: continue

                    val pedido = mapOf(
                        "pedidoId" to doc.id,
                        "usuarioId" to usuarioId,
                        "nomeUsuario" to doc.getString("nomeUsuario"),
                        "livroId" to doc.getString("livroId"),
                        "tituloLivro" to doc.getString("tituloLivro"),
                        "autoresLivro" to doc.getString("autoresLivro"),
                        "capaUrl" to doc.getString("capaUrl")
                    )

                    pedidosPorUsuario
                        .getOrPut(usuarioId) { mutableListOf() }
                        .add(pedido)
                }

                for ((_, pedidos) in pedidosPorUsuario) {
                    val nomeUsuario = pedidos.first()["nomeUsuario"] as? String ?: "Usuário"
                    adicionarGrupoDevolucao(nomeUsuario, pedidos)
                }
            }
            .addOnFailureListener { erro ->
                Toast.makeText(
                    this,
                    "Erro ao carregar devoluções: ${erro.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun adicionarGrupoDevolucao(
        nomeUsuario: String,
        pedidos: List<Map<String, Any?>>
    ) {
        val ctx = this

        val tvNome = TextView(ctx).apply {
            text = nomeUsuario
            textSize = 18f
            setTextColor(0xFFF1F1F1.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(14), 0, dpToPx(6))
            }
        }

        containerDevolucoes.addView(tvNome)

        val card = CardView(ctx).apply {
            setCardBackgroundColor(0xFF2E2E2E.toInt())
            radius = dpToPx(8).toFloat()
            cardElevation = dpToPx(2).toFloat()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(8))
            }
        }

        val cardContent = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            val padding = dpToPx(12)
            setPadding(padding, padding, padding, padding)
        }

        for ((index, pedido) in pedidos.withIndex()) {
            if (index > 0) {
                val divisor = View(ctx).apply {
                    setBackgroundColor(0xFF3A3A3A.toInt())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(1)
                    ).apply {
                        setMargins(0, dpToPx(14), 0, dpToPx(14))
                    }
                }

                cardContent.addView(divisor)
            }

            cardContent.addView(criarLinhaLivro(pedido))
        }

        val tvPergunta = TextView(ctx).apply {
            text = "Devolução realizada?"
            textSize = 15f
            setTextColor(0xFFF1F1F1.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(14), 0, dpToPx(8))
            }
        }

        cardContent.addView(tvPergunta)

        val btnConfirmar = Button(ctx).apply {
            text = "Confirmar"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 13f
            isAllCaps = false
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFF19A1E4.toInt())

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(42)
            )

            setOnClickListener {
                confirmarDevolucao(pedidos)
            }
        }

        cardContent.addView(btnConfirmar)

        card.addView(cardContent)
        containerDevolucoes.addView(card)
    }

    private fun confirmarDevolucao(pedidos: List<Map<String, Any?>>) {
        for (pedido in pedidos) {
            val pedidoId = pedido["pedidoId"] as? String ?: continue

            db.collection("Pedidos")
                .document(pedidoId)
                .update("status", "devolvido")
        }

        Toast.makeText(this, "Devolução confirmada!", Toast.LENGTH_SHORT).show()
        carregarDevolucoes()
    }

    private fun criarLinhaLivro(pedido: Map<String, Any?>): LinearLayout {
        val ctx = this

        val titulo = pedido["tituloLivro"] as? String ?: "Sem título"
        val autores = pedido["autoresLivro"] as? String ?: ""
        val capaBase64 = pedido["capaUrl"] as? String

        val linha = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val cardCapa = CardView(ctx).apply {
            setCardBackgroundColor(0xFF1C1C1C.toInt())
            radius = dpToPx(8).toFloat()
            cardElevation = 0f

            layoutParams = LinearLayout.LayoutParams(
                dpToPx(78),
                dpToPx(108)
            )
        }

        val imgCapa = ImageView(ctx).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            carregarCapaNesteImageView(this, capaBase64)
        }

        cardCapa.addView(imgCapa)
        linha.addView(cardCapa)

        val areaTextos = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(dpToPx(18), 0, 0, 0)
            }
        }

        val tvTitulo = TextView(ctx).apply {
            text = titulo
            textSize = 18f
            setTextColor(0xFFF1F1F1.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val tvAutor = TextView(ctx).apply {
            text = if (autores.isNotEmpty()) {
                "Autor:\n$autores"
            } else {
                "Autor: -"
            }

            textSize = 14f
            setTextColor(0xFFF1F1F1.toInt())
        }

        areaTextos.addView(tvTitulo)
        areaTextos.addView(tvAutor)

        linha.addView(areaTextos)

        return linha
    }

    private fun carregarCapaNesteImageView(imageView: ImageView, capaBase64: String?) {
        if (!capaBase64.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(capaBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageView.setImageBitmap(bitmap)
                return
            } catch (erro: Exception) {
                imageView.setImageResource(R.drawable.bg_book_cover_placeholder)
            }
        } else {
            imageView.setImageResource(R.drawable.bg_book_cover_placeholder)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}