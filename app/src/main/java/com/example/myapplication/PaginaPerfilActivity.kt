package com.example.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
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
    private lateinit var imgFortaleza300: ImageView
    private lateinit var imgAvatarPerfil: ImageView  // ← adicione esse ImageView no seu layout

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
        imgAvatarPerfil   = findViewById(R.id.imgAvatarPerfil) // ID do ImageView no layout
        config            = findViewById(R.id.btnConfig)
        imgFortaleza300   = findViewById(R.id.capaFortaleza)

        val intentName  = intent.getStringExtra("userName")
        val intentEmail = intent.getStringExtra("userEmail")

        if (!intentName.isNullOrEmpty() && !intentEmail.isNullOrEmpty()) {
            textUserName.text  = intentName
            textUserEmail.text = intentEmail
        }

        // Sempre busca os dados atualizados do Firestore (incluindo foto)
        carregarDadosDoUsuario(textUserName, textUserEmail)

        config.setOnClickListener { abrirMenuConfig() }

        imgFortaleza300.setOnClickListener {
            startActivity(Intent(this, BookpageActivity::class.java))
        }

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

                    // Carrega foto em base64
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

        // ← Adicione este bloco
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