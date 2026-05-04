package com.example.myapplication

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class BookpageActivity : AppCompatActivity() {

    lateinit var btnSelecionar : Button
    lateinit var btnFavoritar : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookpage)

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)

        val btnDetalhes = findViewById<Button>(R.id.btnDetalhes)
        val btnReferencia = findViewById<Button>(R.id.btnReferencia)
        val painelDetalhes = findViewById<LinearLayout>(R.id.painelDetalhes)
        val painelReferencia = findViewById<LinearLayout>(R.id.painelReferencia)
        val btnCopiar = findViewById<Button>(R.id.btnCopiarReferencia)
        btnSelecionar = findViewById(R.id.btnSelecionar)
        btnFavoritar = findViewById(R.id.btnFavoritar)


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
            val intent = Intent(this, BookSelectionActivity::class.java)
            startActivity(intent)
        }

        btnFavoritar.setOnClickListener {
            val intent = Intent(this, PaginaPerfilActivity::class.java)
            startActivity(intent)
        }

        btnDetalhes.setOnClickListener   { mostrarDetalhes() }
        btnReferencia.setOnClickListener { mostrarReferencia() }

        // Copiar referência para a área de transferência
        btnCopiar.setOnClickListener {
            val textoReferencia = findViewById<android.widget.TextView>(R.id.lbReferenciaId).text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("referencia", textoReferencia))
            Toast.makeText(this, "Referência copiada!", Toast.LENGTH_SHORT).show()
        }

        // Começa na aba Detalhes
        mostrarDetalhes()
    }
}