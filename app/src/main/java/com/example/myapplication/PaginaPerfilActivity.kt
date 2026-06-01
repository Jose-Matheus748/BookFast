package com.example.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PaginaPerfilActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private lateinit var config: ImageView
    private lateinit var imgAvatarPerfil: ImageView
    private lateinit var containerFavoritos: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagina_perfil)

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        val textUserName  = findViewById<TextView>(R.id.userName)
        val textUserEmail = findViewById<TextView>(R.id.userEmail)
        imgAvatarPerfil   = findViewById(R.id.imgAvatarPerfil)
        config            = findViewById(R.id.btnConfig)
        containerFavoritos = findViewById(R.id.containerFavoritos)

        carregarDadosDoUsuario(textUserName, textUserEmail)
        carregarFavoritos()

        config.setOnClickListener { abrirMenuConfig() }

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)
    }

    private fun carregarDadosDoUsuario(textUserName: TextView, textUserEmail: TextView) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("Usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    textUserName.text  = doc.getString("nome") ?: "Usuário"
                    textUserEmail.text = doc.getString("email") ?: auth.currentUser?.email ?: ""

                    val fotoBase64 = doc.getString("fotoBase64")
                    if (!fotoBase64.isNullOrEmpty()) {
                        try {
                            val bytes  = Base64.decode(fotoBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            Glide.with(this).load(bitmap).circleCrop().into(imgAvatarPerfil)
                        } catch (e: Exception) { /* mantém placeholder */ }
                    }
                } else {
                    textUserName.text  = "Usuário"
                    textUserEmail.text = auth.currentUser?.email ?: ""
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun carregarFavoritos() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("Usuarios").document(uid)
            .collection("Favoritos")
            .get()
            .addOnSuccessListener { result ->
                containerFavoritos.removeAllViews()

                if (result.isEmpty) {
                    val tv = TextView(this).apply {
                        text = "Nenhum favorito ainda."
                        textSize = 14f
                        setTextColor(0xFFAAAAAA.toInt())
                    }
                    containerFavoritos.addView(tv)
                    return@addOnSuccessListener
                }

                // Exibe em grid de 2 colunas via LinearLayouts horizontais
                val livros = result.documents
                for (i in livros.indices step 2) {
                    val row = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 0, 0, dpToPx(10)) }  // só margem inferior entre linhas
                        weightSum = 2f
                    }

                    val card1 = criarCardFavorito(
                        livroId     = livros[i].getString("livroId") ?: "",
                        titulo      = livros[i].getString("titulo") ?: "",
                        autor       = livros[i].getString("autor") ?: "",
                        capaBase64  = livros[i].getString("capaBase64") ?: ""
                    )
                    row.addView(card1)

                    if (i + 1 < livros.size) {
                        val card2 = criarCardFavorito(
                            livroId    = livros[i + 1].getString("livroId") ?: "",
                            titulo     = livros[i + 1].getString("titulo") ?: "",
                            autor      = livros[i + 1].getString("autor") ?: "",
                            capaBase64 = livros[i + 1].getString("capaBase64") ?: ""
                        )
                        row.addView(card2)
                    } else {
                        // Célula vazia para manter o grid alinhado
                        val espacador = android.view.View(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                            )
                        }
                        row.addView(espacador)
                    }

                    containerFavoritos.addView(row)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar favoritos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun criarCardFavorito(
        livroId: String,
        titulo: String,
        autor: String,
        capaBase64: String
    ): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f  // altura dinâmica
            ).apply { setMargins(dpToPx(4), 0, dpToPx(4), 0) } // margem simétrica
            setBackgroundColor(0xFF2E2E2E.toInt())
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(10))
        }

        // Capa com proporção de livro (3:4)
        val imgCapa = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0
            ).also {
                it.weight = 1f   // placeholder; altura real vem do AspectRatio abaixo
            }
            // Usa dimensão calculada em runtime via post
            val widthPx = (resources.displayMetrics.widthPixels / 2) - dpToPx(32)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (widthPx * 1.35f).toInt()   // proporção 1 : 1.35  ≈ capa de livro
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(0xFF1A1A1A.toInt())  // fallback enquanto carrega
        }

        if (capaBase64.isNotEmpty()) {
            try {
                val bytes  = Base64.decode(capaBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imgCapa.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imgCapa.setImageResource(R.drawable.bg_book_cover_placeholder)
            }
        } else {
            imgCapa.setImageResource(R.drawable.bg_book_cover_placeholder)
        }

        val tvTitulo = TextView(this).apply {
            text = titulo
            textSize = 12f
            setTextColor(0xFFF0F0F0.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dpToPx(8), 0, 0) }
        }

        val tvAutor = TextView(this).apply {
            text = autor
            textSize = 10f
            setTextColor(0xFF888888.toInt())
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dpToPx(2), 0, 0) }
        }

        card.addView(imgCapa)
        card.addView(tvTitulo)
        card.addView(tvAutor)

        card.setOnClickListener {
            startActivity(Intent(this, BookpageActivity::class.java).apply {
                putExtra("LIVRO_ID", livroId)
            })
        }

        return card
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

    private fun abrirMenuConfig() {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.component_user_options, null)
        bottomSheet.setContentView(view)

        view.findViewById<LinearLayout>(R.id.textViewEditarPerfil).setOnClickListener {
            bottomSheet.dismiss()
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.textViewSobreApp).setOnClickListener {
            bottomSheet.dismiss()
            startActivity(Intent(this, AboutActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.textViewSair).setOnClickListener {
            bottomSheet.dismiss()
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        bottomSheet.show()
    }
}