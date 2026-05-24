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
import com.google.firebase.firestore.firestore

class BookpageActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookpage)

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)

        val btnDetalhes      = findViewById<Button>(R.id.btnDetalhes)
        val btnReferencia    = findViewById<Button>(R.id.btnReferencia)
        val painelDetalhes   = findViewById<LinearLayout>(R.id.painelDetalhes)
        val painelReferencia = findViewById<LinearLayout>(R.id.painelReferencia)
        val btnCopiar        = findViewById<Button>(R.id.btnCopiarReferencia)
        val btnSelecionar    = findViewById<Button>(R.id.btnSelecionar)
        val btnFavoritar     = findViewById<Button>(R.id.btnFavoritar)

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
            startActivity(Intent(this, BookSelectionActivity::class.java))
        }

        btnFavoritar.setOnClickListener {
            startActivity(Intent(this, PaginaPerfilActivity::class.java))
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

        // ── Carrega dados do Firestore ──────────────────────────────────────
        val livroId = intent.getStringExtra("LIVRO_ID")
        if (livroId.isNullOrEmpty()) {
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

                // Capa
                val capaBase64 = doc.getString("capaUrl") ?: ""
                val imgCapa = findViewById<ImageView>(R.id.capaFortaleza)
                if (capaBase64.isNotEmpty()) {
                    try {
                        val bytes = Base64.decode(capaBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imgCapa.setImageBitmap(bitmap)
                    } catch (e: Exception) { /* mantém imagem padrão */ }
                }

                // Título e autores (cabeçalho)
                val titulo  = doc.getString("titulo") ?: ""
                val autores = (doc.get("autores") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
                val autoresStr = autores.joinToString(", ")

                findViewById<TextView>(R.id.tituloLivroId).text = titulo
                findViewById<TextView>(R.id.autorId).text       = autoresStr

                // Campos de detalhe
                val material   = doc.getString("material")   ?: ""
                val idioma     = doc.getString("idioma")     ?: ""
                val publicacao = doc.getString("publicacao") ?: ""
                val edicao     = doc.getString("edicao")     ?: ""
                val serie      = doc.getString("serie")      ?: ""
                val assuntos   = doc.getString("assuntos")   ?: ""
                val referencia = doc.getString("referencia") ?: ""

                findViewById<TextView>(R.id.lbmaterialId).text  =
                    if (material.isNotEmpty())   "Material: $material"   else ""
                findViewById<TextView>(R.id.ldIdiomaId).text    =
                    if (idioma.isNotEmpty())     "Idioma: $idioma"       else ""
                findViewById<TextView>(R.id.lbPublicacaoId).text =
                    if (publicacao.isNotEmpty()) "Publicação: $publicacao" else ""
                findViewById<TextView>(R.id.lbEdicaoId).text    =
                    if (edicao.isNotEmpty())     "Edição: $edicao"       else ""
                findViewById<TextView>(R.id.lbSerieId).text     =
                    if (serie.isNotEmpty())      "Série: $serie"         else ""
                findViewById<TextView>(R.id.lbAssuntosId).text  =
                    if (assuntos.isNotEmpty())   "Assuntos: $assuntos"   else ""
                findViewById<TextView>(R.id.LbAutoresId).text   = autoresStr
                findViewById<TextView>(R.id.lbReferenciaId).text = referencia
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar livro", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}