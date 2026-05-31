package com.example.myapplication

import android.content.Intent
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

        tabLayout            = findViewById(R.id.tabLayout)
        painelReservas       = findViewById(R.id.painelReservas)
        painelDevolucoes     = findViewById(R.id.painelDevolucoes)
        containerPedidos     = findViewById(R.id.containerPedidos)
        containerDevolucoes  = findViewById(R.id.containerDevolucoes)
        txtSemPedidos        = findViewById(R.id.txtSemPedidos)
        txtSemDevolucoes     = findViewById(R.id.txtSemDevolucoes)

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)

        tabLayout.getTabAt(0)?.select()
        carregarPedidos()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        painelReservas.visibility   = View.VISIBLE
                        painelDevolucoes.visibility = View.GONE
                        carregarPedidos()
                    }
                    1 -> {
                        painelReservas.visibility   = View.GONE
                        painelDevolucoes.visibility = View.VISIBLE
                        carregarDevolucoes()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // ─────────────────────────────────────────────────────────────
    //  ABA RESERVAS — status "pendente"
    // ─────────────────────────────────────────────────────────────

    private fun carregarPedidos() {
        containerPedidos.removeAllViews()
        txtSemPedidos.visibility = View.GONE

        db.collection("Reservas")
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    txtSemPedidos.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                // Agrupa as reservas pelo usuarioId
                val porUsuario = LinkedHashMap<String, MutableList<Map<String, Any?>>>()
                for (doc in resultado) {
                    val uid = doc.getString("usuarioId") ?: continue
                    porUsuario.getOrPut(uid) { mutableListOf() }.add(
                        mapOf(
                            "reservaId"    to doc.id,
                            "livroId"      to doc.getString("livroId"),
                            "tituloLivro"  to doc.getString("tituloLivro"),
                            "autoresLivro" to doc.getString("autoresLivro"),
                            "capaUrl"      to doc.getString("capaUrl"),
                            "nomeUsuario"  to doc.getString("nomeUsuario"),
                            "usuarioId"    to uid
                        )
                    )
                }

                for ((_, reservas) in porUsuario) {
                    val nomeUsuario = reservas.first()["nomeUsuario"] as? String ?: "Usuário"
                    val usuarioId   = reservas.first()["usuarioId"]   as? String ?: ""
                    adicionarGrupoPedido(nomeUsuario, usuarioId, reservas)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar pedidos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun adicionarGrupoPedido(
        nomeUsuario: String,
        usuarioId: String,
        reservas: List<Map<String, Any?>>
    ) {
        val ctx = this

        // Nome do usuário
        val tvNome = TextView(ctx).apply {
            text      = nomeUsuario
            textSize  = 18f
            setTextColor(0xFFF1F1F1.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dpToPx(14), 0, dpToPx(6)) }
            layoutParams = lp
        }
        containerPedidos.addView(tvNome)

        // Card com todos os livros deste usuário
        val card = CardView(ctx).apply {
            setCardBackgroundColor(0xFF2E2E2E.toInt())
            radius          = dpToPx(8).toFloat()
            cardElevation   = dpToPx(2).toFloat()
            layoutParams    = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, dpToPx(8)) }
        }

        val cardContent = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            val p = dpToPx(12)
            setPadding(p, p, p, p)
        }

        reservas.forEachIndexed { index, reserva ->
            if (index > 0) {
                // Linha divisória
                val divider = View(ctx).apply {
                    setBackgroundColor(0xFF3A3A3A.toInt())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)
                    ).apply { setMargins(0, dpToPx(14), 0, dpToPx(14)) }
                }
                cardContent.addView(divider)
            }
            cardContent.addView(criarLinhaLivro(reserva))
        }

        // Pergunta + botões
        val tvPergunta = TextView(ctx).apply {
            text      = "Retirada realizada?"
            textSize  = 15f
            setTextColor(0xFFF1F1F1.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dpToPx(14), 0, dpToPx(8)) }
        }
        cardContent.addView(tvPergunta)

        val rowBotoes = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(42)
            )
        }

        val btnConfirmar = Button(ctx).apply {
            text = "Confirmar"
            setTextColor(0xFFFFFFFF.toInt())
            textSize      = 13f
            isAllCaps     = false
            backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF19A1E4.toInt())
            layoutParams  = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                .apply { setMargins(0, 0, dpToPx(8), 0) }
            setOnClickListener { confirmarRetirada(usuarioId, reservas) }
        }

        val btnNegar = Button(ctx).apply {
            text = "Negar"
            setTextColor(0xFFFFFFFF.toInt())
            textSize      = 13f
            isAllCaps     = false
            backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF434343.toInt())
            layoutParams  = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            setOnClickListener { negarRetirada(usuarioId, reservas) }
        }

        rowBotoes.addView(btnConfirmar)
        rowBotoes.addView(btnNegar)
        cardContent.addView(rowBotoes)

        card.addView(cardContent)
        containerPedidos.addView(card)
    }

    // Confirmar → status "retirado"
    private fun confirmarRetirada(usuarioId: String, reservas: List<Map<String, Any?>>) {
        val batch = db.batch()
        for (reserva in reservas) {
            val id  = reserva["reservaId"] as? String ?: continue
            val ref = db.collection("Reservas").document(id)
            batch.update(ref, "status", "retirado")
        }
        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Retirada confirmada!", Toast.LENGTH_SHORT).show()
                carregarPedidos()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao confirmar retirada", Toast.LENGTH_SHORT).show()
            }
    }

    // Negar → status "nao_retirado" (volta para o usuário ver)
    private fun negarRetirada(usuarioId: String, reservas: List<Map<String, Any?>>) {
        val batch = db.batch()
        for (reserva in reservas) {
            val id  = reserva["reservaId"] as? String ?: continue
            val ref = db.collection("Reservas").document(id)
            batch.update(ref, "status", "nao_retirado")
        }
        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Pedido marcado como não retirado.", Toast.LENGTH_SHORT).show()
                carregarPedidos()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao negar retirada", Toast.LENGTH_SHORT).show()
            }
    }

    // ─────────────────────────────────────────────────────────────
    //  ABA DEVOLUÇÕES — status "retirado"
    // ─────────────────────────────────────────────────────────────

    private fun carregarDevolucoes() {
        containerDevolucoes.removeAllViews()
        txtSemDevolucoes.visibility = View.GONE

        db.collection("Reservas")
            .whereEqualTo("status", "retirado")
            .get()
            .addOnSuccessListener { resultado ->
                if (resultado.isEmpty) {
                    txtSemDevolucoes.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                val porUsuario = LinkedHashMap<String, MutableList<Map<String, Any?>>>()
                for (doc in resultado) {
                    val uid = doc.getString("usuarioId") ?: continue
                    porUsuario.getOrPut(uid) { mutableListOf() }.add(
                        mapOf(
                            "reservaId"    to doc.id,
                            "livroId"      to doc.getString("livroId"),
                            "tituloLivro"  to doc.getString("tituloLivro"),
                            "autoresLivro" to doc.getString("autoresLivro"),
                            "capaUrl"      to doc.getString("capaUrl"),
                            "nomeUsuario"  to doc.getString("nomeUsuario"),
                            "usuarioId"    to uid
                        )
                    )
                }

                for ((_, reservas) in porUsuario) {
                    val nomeUsuario = reservas.first()["nomeUsuario"] as? String ?: "Usuário"
                    val usuarioId   = reservas.first()["usuarioId"]   as? String ?: ""
                    adicionarGrupoDevolucao(nomeUsuario, usuarioId, reservas)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar devoluções", Toast.LENGTH_SHORT).show()
            }
    }

    private fun adicionarGrupoDevolucao(
        nomeUsuario: String,
        usuarioId: String,
        reservas: List<Map<String, Any?>>
    ) {
        val ctx = this

        val tvNome = TextView(ctx).apply {
            text     = nomeUsuario
            textSize = 18f
            setTextColor(0xFFF1F1F1.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dpToPx(14), 0, dpToPx(6)) }
        }
        containerDevolucoes.addView(tvNome)

        val card = CardView(ctx).apply {
            setCardBackgroundColor(0xFF2E2E2E.toInt())
            radius        = dpToPx(8).toFloat()
            cardElevation = dpToPx(2).toFloat()
            layoutParams  = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, dpToPx(8)) }
        }

        val cardContent = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            val p = dpToPx(12)
            setPadding(p, p, p, p)
        }

        reservas.forEachIndexed { index, reserva ->
            if (index > 0) {
                val divider = View(ctx).apply {
                    setBackgroundColor(0xFF3A3A3A.toInt())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)
                    ).apply { setMargins(0, dpToPx(14), 0, dpToPx(14)) }
                }
                cardContent.addView(divider)
            }
            cardContent.addView(criarLinhaLivro(reserva))
        }

        val tvPergunta = TextView(ctx).apply {
            text     = "Devolução realizada?"
            textSize = 15f
            setTextColor(0xFFF1F1F1.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dpToPx(14), 0, dpToPx(8)) }
        }
        cardContent.addView(tvPergunta)

        val btnConfirmar = Button(ctx).apply {
            text = "Confirmar"
            setTextColor(0xFFFFFFFF.toInt())
            textSize  = 13f
            isAllCaps = false
            backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF19A1E4.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(42)
            )
            setOnClickListener { confirmarDevolucao(usuarioId, reservas) }
        }
        cardContent.addView(btnConfirmar)

        card.addView(cardContent)
        containerDevolucoes.addView(card)
    }

    // Devolução confirmada → status "devolvido"
    private fun confirmarDevolucao(usuarioId: String, reservas: List<Map<String, Any?>>) {
        val batch = db.batch()
        for (reserva in reservas) {
            val id  = reserva["reservaId"] as? String ?: continue
            val ref = db.collection("Reservas").document(id)
            batch.update(ref, "status", "devolvido")
        }
        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Devolução confirmada!", Toast.LENGTH_SHORT).show()
                carregarDevolucoes()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao confirmar devolução", Toast.LENGTH_SHORT).show()
            }
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────

    private fun criarLinhaLivro(reserva: Map<String, Any?>): LinearLayout {
        val ctx     = this
        val titulo  = reserva["tituloLivro"]  as? String ?: "Sem título"
        val autores = reserva["autoresLivro"] as? String ?: ""
        val capa    = reserva["capaUrl"]      as? String

        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Capa
        val capaCard = androidx.cardview.widget.CardView(ctx).apply {
            setCardBackgroundColor(0xFF1C1C1C.toInt())
            radius        = dpToPx(8).toFloat()
            cardElevation = 0f
            layoutParams  = LinearLayout.LayoutParams(dpToPx(78), dpToPx(108))
        }
        val imgCapa = ImageView(ctx).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            if (!capa.isNullOrEmpty()) {
                try {
                    val bytes  = Base64.decode(capa, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    setImageBitmap(bitmap)
                } catch (e: Exception) {
                    setImageResource(R.drawable.bg_book_cover_placeholder)
                }
            } else {
                setImageResource(R.drawable.bg_book_cover_placeholder)
            }
        }
        capaCard.addView(imgCapa)
        row.addView(capaCard)

        // Textos
        val info = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { setMargins(dpToPx(18), 0, 0, 0) }
        }
        val tvTitulo = TextView(ctx).apply {
            text     = titulo
            textSize = 18f
            setTextColor(0xFFF1F1F1.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val tvAutor = TextView(ctx).apply {
            text     = if (autores.isNotEmpty()) "Autor:\n$autores" else "Autor: —"
            textSize = 14f
            setTextColor(0xFFF1F1F1.toInt())
        }
        info.addView(tvTitulo)
        info.addView(tvAutor)
        row.addView(info)

        return row
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}