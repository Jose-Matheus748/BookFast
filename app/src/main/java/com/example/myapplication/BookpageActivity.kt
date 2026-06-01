package com.example.myapplication

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class BookpageActivity : AppCompatActivity() {

    private val db   = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private lateinit var containerExemplares: LinearLayout
    private lateinit var lbNExemplares: TextView
    private lateinit var btnFavoritar: Button

    private var capaBase64Atual: String? = null
    private var livroId      = ""
    private var isFavoritado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookpage)

        containerExemplares = findViewById(R.id.containerExemplares)
        lbNExemplares       = findViewById(R.id.lbNExemplaresId)
        btnFavoritar        = findViewById(R.id.btnFavoritar)

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)

        val btnDetalhes      = findViewById<Button>(R.id.btnDetalhes)
        val btnReferencia    = findViewById<Button>(R.id.btnReferencia)
        val painelDetalhes   = findViewById<LinearLayout>(R.id.painelDetalhes)
        val painelReferencia = findViewById<LinearLayout>(R.id.painelReferencia)
        val btnCopiar        = findViewById<Button>(R.id.btnCopiarReferencia)
        val btnSelecionar    = findViewById<Button>(R.id.btnSelecionar)

        val corAtiva   = 0xFF19A1E4.toInt()
        val corInativa = 0xFF434343.toInt()

        fun mostrarDetalhes() {
            painelDetalhes.visibility   = View.VISIBLE
            painelReferencia.visibility = View.GONE
            btnDetalhes.backgroundTintList   = android.content.res.ColorStateList.valueOf(corAtiva)
            btnReferencia.backgroundTintList = android.content.res.ColorStateList.valueOf(corInativa)
        }

        fun mostrarReferencia() {
            painelDetalhes.visibility   = View.GONE
            painelReferencia.visibility = View.VISIBLE
            btnDetalhes.backgroundTintList   = android.content.res.ColorStateList.valueOf(corInativa)
            btnReferencia.backgroundTintList = android.content.res.ColorStateList.valueOf(corAtiva)
        }

        btnSelecionar.setOnClickListener {
            if (livroId.isEmpty()) {
                Toast.makeText(this, "Livro não carregado ainda.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, BookSelectionActivity::class.java)
            intent.putExtra("LIVRO_ID", livroId)
            startActivity(intent)
        }

        btnFavoritar.setOnClickListener {
            alternarFavorito()
        }

        btnDetalhes.setOnClickListener   { mostrarDetalhes() }
        btnReferencia.setOnClickListener { mostrarReferencia() }

        btnCopiar.setOnClickListener {
            val texto = findViewById<TextView>(R.id.lbReferenciaId).text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("referencia", texto))
            Toast.makeText(this, "Referência copiada!", Toast.LENGTH_SHORT).show()
        }

        mostrarDetalhes()

        livroId = intent.getStringExtra("LIVRO_ID") ?: ""
        if (livroId.isEmpty()) {
            Toast.makeText(this, "Livro não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db.collection("Livros").document(livroId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Livro não encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val capaBase64 = doc.getString("capaUrl") ?: ""
                capaBase64Atual = capaBase64
                val imgCapa = findViewById<ImageView>(R.id.capaFortaleza)
                if (capaBase64.isNotEmpty()) {
                    try {
                        val bytes  = Base64.decode(capaBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imgCapa.setImageBitmap(bitmap)
                    } catch (e: Exception) { }
                }

                val titulo     = doc.getString("titulo") ?: ""
                val autores    = (doc.get("autores") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val autoresStr = autores.joinToString(", ")

                findViewById<TextView>(R.id.tituloLivroId).text = titulo
                findViewById<TextView>(R.id.autorId).text       = autoresStr

                val material   = doc.getString("material")   ?: ""
                val idioma     = doc.getString("idioma")     ?: ""
                val publicacao = doc.getString("publicacao") ?: ""
                val edicao     = doc.getString("edicao")     ?: ""
                val serie      = doc.getString("serie")      ?: ""
                val assuntos   = doc.getString("assuntos")   ?: ""
                val referencia = doc.getString("referencia") ?: ""

                findViewById<TextView>(R.id.lbmaterialId).text   = if (material.isNotEmpty())   "Material: $material"     else ""
                findViewById<TextView>(R.id.ldIdiomaId).text     = if (idioma.isNotEmpty())     "Idioma: $idioma"         else ""
                findViewById<TextView>(R.id.lbPublicacaoId).text = if (publicacao.isNotEmpty()) "Publicação: $publicacao" else ""
                findViewById<TextView>(R.id.lbEdicaoId).text     = if (edicao.isNotEmpty())     "Edição: $edicao"         else ""
                findViewById<TextView>(R.id.lbSerieId).text      = if (serie.isNotEmpty())      "Série: $serie"           else ""
                findViewById<TextView>(R.id.lbAssuntosId).text   = if (assuntos.isNotEmpty())   "Assuntos: $assuntos"     else ""
                findViewById<TextView>(R.id.LbAutoresId).text    = autoresStr
                findViewById<TextView>(R.id.lbReferenciaId).text = referencia

                // Verifica favorito só após carregar o livro
                verificarFavoritoAtual()
                carregarExemplares()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar livro", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    // ── Favoritos ────────────────────────────────────────────────────────────

    private fun verificarFavoritoAtual() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("Usuarios").document(uid)
            .collection("Favoritos").document(livroId)
            .get()
            .addOnSuccessListener { doc ->
                isFavoritado = doc.exists()
                atualizarBotaoFavorito()
            }
    }

    private fun alternarFavorito() {
        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Faça login para favoritar", Toast.LENGTH_SHORT).show()
            return
        }

        val refFavorito = db.collection("Usuarios").document(uid)
            .collection("Favoritos").document(livroId)

        if (isFavoritado) {
            refFavorito.delete()
                .addOnSuccessListener {
                    isFavoritado = false
                    atualizarBotaoFavorito()
                    Toast.makeText(this, "Removido dos favoritos", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao remover favorito", Toast.LENGTH_SHORT).show()
                }
        } else {
            val dados = hashMapOf(
                "livroId"    to livroId,
                "titulo"     to findViewById<TextView>(R.id.tituloLivroId).text.toString(),
                "autor"      to findViewById<TextView>(R.id.autorId).text.toString(),
                "capaBase64" to (capaBase64Atual ?: "")  // ← usar o valor real
            )
            refFavorito.set(dados)
                .addOnSuccessListener {
                    isFavoritado = true
                    atualizarBotaoFavorito()
                    Toast.makeText(this, "Adicionado aos favoritos!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao favoritar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun atualizarBotaoFavorito() {
        if (isFavoritado) {
            btnFavoritar.text = "★ Favoritado"
            btnFavoritar.backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFF19A1E4.toInt())
        } else {
            btnFavoritar.text = "☆ Favoritar"
            btnFavoritar.backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFF434343.toInt())
        }
    }

    // ── Exemplares ───────────────────────────────────────────────────────────

    private fun carregarExemplares() {
        db.collection("Livros")
            .document(livroId)
            .collection("Exemplares")
            .get()
            .addOnSuccessListener { result ->
                containerExemplares.removeAllViews()
                val total = result.size()
                lbNExemplares.text = "Número de exemplares: $total"

                if (total == 0) {
                    val tv = TextView(this).apply {
                        text = "Nenhum exemplar disponível."
                        textSize = 14f
                        setTextColor(0xFFAAAAAA.toInt())
                        setPadding(16, 16, 16, 8)
                    }
                    containerExemplares.addView(tv)
                    return@addOnSuccessListener
                }

                for (doc in result) {
                    val card = criarCardExemplar(
                        registro    = doc.getString("registro")    ?: "",
                        edicao      = doc.getString("edicao")      ?: "",
                        ano         = doc.getString("ano")         ?: "",
                        suporte     = doc.getString("suporte")     ?: "",
                        localizacao = doc.getString("localizacao") ?: "",
                        situacao    = doc.getString("situacao")    ?: ""
                    )
                    containerExemplares.addView(card)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar exemplares", Toast.LENGTH_SHORT).show()
            }
    }

    private fun criarCardExemplar(
        registro: String, edicao: String, ano: String,
        suporte: String, localizacao: String, situacao: String
    ): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(dpToPx(16), dpToPx(8), dpToPx(16), 0) }
            setPadding(0, 0, 0, dpToPx(8))
            setBackgroundResource(R.drawable.bg_container_exemplares)
        }

        fun linhaTexto(texto: String, topPadding: Int = 4) = TextView(this).apply {
            text = texto
            textSize = 14f
            setTextColor(0xFFF0F0F0.toInt())
            setPadding(dpToPx(8), dpToPx(topPadding), dpToPx(8), 0)
        }

        card.addView(linhaTexto("Registro: $registro", 8))
        if (edicao.isNotEmpty())      card.addView(linhaTexto("Edição: $edicao"))
        if (ano.isNotEmpty())         card.addView(linhaTexto("Ano: $ano"))
        if (suporte.isNotEmpty())     card.addView(linhaTexto("Suporte: $suporte"))
        if (localizacao.isNotEmpty()) card.addView(linhaTexto("Localização: $localizacao"))
        if (situacao.isNotEmpty())    card.addView(linhaTexto("Situação: $situacao"))

        return card
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()
}