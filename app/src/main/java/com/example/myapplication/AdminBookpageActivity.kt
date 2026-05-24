package com.example.myapplication

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class AdminBookpageActivity : AppCompatActivity() {

    private lateinit var imgPencil: ImageView
    private lateinit var imgTrash: ImageView
    private val db = Firebase.firestore
    private var livroId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookpage_admin)

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)

        imgPencil = findViewById(R.id.imgPencil)
        imgTrash  = findViewById(R.id.imgTrash)

        val btnDetalhes      = findViewById<Button>(R.id.btnDetalhes)
        val btnReferencia    = findViewById<Button>(R.id.btnReferencia)
        val painelDetalhes   = findViewById<LinearLayout>(R.id.painelDetalhes)
        val painelReferencia = findViewById<LinearLayout>(R.id.painelReferencia)
        val btnCopiar        = findViewById<Button>(R.id.btnCopiarReferencia)

        val corAtiva   = 0xFF19A1E4.toInt()
        val corInativa = 0xFF434343.toInt()

        fun mostrarDetalhes() {
            painelDetalhes.visibility   = View.VISIBLE
            painelReferencia.visibility = View.GONE
            btnDetalhes.backgroundTintList   = ColorStateList.valueOf(corAtiva)
            btnReferencia.backgroundTintList = ColorStateList.valueOf(corInativa)
        }

        fun mostrarReferencia() {
            painelDetalhes.visibility   = View.GONE
            painelReferencia.visibility = View.VISIBLE
            btnDetalhes.backgroundTintList   = ColorStateList.valueOf(corInativa)
            btnReferencia.backgroundTintList = ColorStateList.valueOf(corAtiva)
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

        imgPencil.setOnClickListener { navegarParaEdicao() }
        imgTrash.setOnClickListener  { mostrarModalExcluir() }

        // ── Carrega dados do Firestore ──────────────────────────────────────
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

                // Capa
                val capaBase64 = doc.getString("capaUrl") ?: ""
                val imgCapa = findViewById<ImageView>(R.id.capaFortaleza)
                if (capaBase64.isNotEmpty()) {
                    try {
                        val bytes = Base64.decode(capaBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imgCapa.setImageBitmap(bitmap)
                    } catch (e: Exception) { /* mantém padrão */ }
                }

                // Título e autores
                val titulo = doc.getString("titulo") ?: ""
                val autores = (doc.get("autores") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.joinToString(", ") ?: ""

                findViewById<TextView>(R.id.tituloLivroId).text = titulo
                findViewById<TextView>(R.id.autorId).text       = autores

                // Detalhes
                val material   = doc.getString("material")   ?: ""
                val idioma     = doc.getString("idioma")     ?: ""
                val publicacao = doc.getString("publicacao") ?: ""
                val edicao     = doc.getString("edicao")     ?: ""
                val serie      = doc.getString("serie")      ?: ""
                val assuntos   = doc.getString("assuntos")   ?: ""
                val referencia = doc.getString("referencia") ?: ""

                findViewById<TextView>(R.id.lbmaterialId).text   =
                    if (material.isNotEmpty())   "Material: $material"     else ""
                findViewById<TextView>(R.id.ldIdiomaId).text     =
                    if (idioma.isNotEmpty())     "Idioma: $idioma"         else ""
                findViewById<TextView>(R.id.lbPublicacaoId).text =
                    if (publicacao.isNotEmpty()) "Publicação: $publicacao" else ""
                findViewById<TextView>(R.id.lbEdicaoId).text     =
                    if (edicao.isNotEmpty())     "Edição: $edicao"         else ""
                findViewById<TextView>(R.id.lbSerieId).text      =
                    if (serie.isNotEmpty())      "Série: $serie"           else ""
                findViewById<TextView>(R.id.lbAssuntosId).text   =
                    if (assuntos.isNotEmpty())   "Assuntos: $assuntos"     else ""
                findViewById<TextView>(R.id.LbAutoresId).text    = autores
                findViewById<TextView>(R.id.lbReferenciaId).text = referencia
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar livro", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun navegarParaEdicao() {
        // Passa o ID para a tela de edição também
        val intent = Intent(this, AdminEditBookActivity::class.java)
        intent.putExtra("LIVRO_ID", livroId)
        startActivity(intent)
    }

    private fun mostrarModalExcluir() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Livro")
            .setMessage("Você tem certeza que deseja excluir esse livro?")
            .setPositiveButton("Sim") { dialog, _ ->
                dialog.dismiss()
                excluirLivro()
            }
            .setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun excluirLivro() {
        db.collection("Livros").document(livroId)
            .delete()
            .addOnSuccessListener {
                AlertDialog.Builder(this)
                    .setTitle("Sucesso")
                    .setMessage("Livro excluído com sucesso!")
                    .setPositiveButton("Voltar") { dialog, _ ->
                        dialog.dismiss()
                        startActivity(Intent(this, HomePageAdmin::class.java))
                        finish()
                    }
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao excluir: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}