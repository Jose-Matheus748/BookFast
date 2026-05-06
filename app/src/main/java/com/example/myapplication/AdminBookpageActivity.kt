package com.example.myapplication

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AdminBookpageActivity : AppCompatActivity() {

    private lateinit var imgPencil: ImageView
    private lateinit var imgTrash: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookpage_admin)

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)

        imgPencil = findViewById(R.id.imgPencil)
        imgTrash = findViewById(R.id.imgTrash)

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
            val textoReferencia = findViewById<TextView>(R.id.lbReferenciaId).text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("referencia", textoReferencia))
            Toast.makeText(this, "Referência copiada!", Toast.LENGTH_SHORT).show()
        }

        // Começa na aba Detalhes
        mostrarDetalhes()

        // --- Lógica de admin ---
        imgPencil.setOnClickListener { navegarParaEdicaoDoLivro() }
        imgTrash.setOnClickListener  { mostrarModalExcluirLivro() }
    }

    private fun navegarParaEdicaoDoLivro() {
        startActivity(Intent(this, AdminEditBookActivity::class.java))
    }

    private fun mostrarModalExcluirLivro() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Livro")
            .setMessage("Você tem certeza que deseja excluir esse livro?")
            .setPositiveButton("Sim") { dialog, _ ->
                dialog.dismiss()
                mostrarModalLivroExcluido()
            }
            .setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun mostrarModalLivroExcluido() {
        AlertDialog.Builder(this)
            .setTitle("Sucesso")
            .setMessage("Livro excluído com sucesso!")
            .setPositiveButton("Voltar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}