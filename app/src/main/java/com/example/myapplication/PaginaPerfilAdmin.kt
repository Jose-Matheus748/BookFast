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

    lateinit var config: ImageView
    lateinit var imgFortaleza300: ImageView

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagina_perfil_admin)

        config = findViewById(R.id.btnConfig)
        imgFortaleza300 = findViewById(R.id.capaFortaleza)

        val textUserName = findViewById<TextView>(R.id.userName)
        val textUserEmail = findViewById<TextView>(R.id.userEmail)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("Usuarios").document(uid).get()
                .addOnSuccessListener { doc ->
                    val nome = doc.getString("nome") ?: auth.currentUser?.displayName ?: "Usuário"
                    val email = auth.currentUser?.email ?: ""
                    textUserName.text = nome
                    textUserEmail.text = email
                }
                .addOnFailureListener {
                    textUserName.text = auth.currentUser?.displayName ?: "Usuário"
                    textUserEmail.text = auth.currentUser?.email ?: ""
                }
        }

        config.setOnClickListener {
            abrirMenuConfig()
        }

        imgFortaleza300.setOnClickListener {
            startActivity(Intent(this, BookpageActivity::class.java))
        }

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)
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
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        bottomSheet.show()
    }
}