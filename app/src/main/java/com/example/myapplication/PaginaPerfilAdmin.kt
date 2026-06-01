package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PaginaPerfilAdmin : AppCompatActivity() {

    private lateinit var config: ImageView
    private lateinit var imgAvatarPerfil: ImageView

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db   by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagina_perfil_admin)

        config          = findViewById(R.id.btnConfig)
        imgAvatarPerfil = findViewById(R.id.imgAvatarPerfil)

        val textUserName  = findViewById<TextView>(R.id.userName)
        val textUserEmail = findViewById<TextView>(R.id.userEmail)

        carregarDadosDoUsuario(textUserName, textUserEmail)

        config.setOnClickListener { abrirMenuConfig() }

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)
    }

    private fun carregarDadosDoUsuario(textUserName: TextView, textUserEmail: TextView) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("Usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                textUserName.text  = doc.getString("nome") ?: auth.currentUser?.displayName ?: "Usuário"
                textUserEmail.text = auth.currentUser?.email ?: ""

                val fotoBase64 = doc.getString("fotoBase64")
                if (!fotoBase64.isNullOrEmpty()) {
                    try {
                        val bytes  = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        com.bumptech.glide.Glide.with(this)
                            .load(bitmap)
                            .circleCrop()
                            .into(imgAvatarPerfil)
                    } catch (e: Exception) { /* mantém placeholder */ }
                }
            }
            .addOnFailureListener {
                textUserName.text  = auth.currentUser?.displayName ?: "Usuário"
                textUserEmail.text = auth.currentUser?.email ?: ""
            }
    }

    private fun abrirMenuConfig() {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.component_user_options, null)
        bottomSheet.setContentView(view)

        view.findViewById<LinearLayout>(R.id.textViewEditarPerfil).setOnClickListener {
            bottomSheet.dismiss()
            startActivity(Intent(this, EditProfileAdminActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.textViewSobreApp).setOnClickListener {
            bottomSheet.dismiss()
            startActivity(Intent(this, AboutActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.textViewSair).setOnClickListener {
            bottomSheet.dismiss()
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        bottomSheet.show()
    }
}