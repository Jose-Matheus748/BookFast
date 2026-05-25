package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PaginaPerfilActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    lateinit var config: ImageView
    lateinit var imgFortaleza300 : ImageView

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

        val textUserName = findViewById<TextView>(R.id.userName)
        val userName = intent.getStringExtra("userName")
        textUserName.text = "$userName"

        val textUserEmail = findViewById<TextView>(R.id.userEmail)
        val userEmail = intent.getStringExtra("userEmail")
        textUserEmail.text = userEmail

        val intentName = intent.getStringExtra("userName")
        val intentEmail = intent.getStringExtra("userEmail")

        if (!intentName.isNullOrEmpty() && !intentEmail.isNullOrEmpty()) {
            textUserName.text = intentName
            textUserEmail.text = intentEmail
        } else {
            // Se não veio pelo Intent, busca os dados diretamente no Firestore
            carregarDadosDoUsuario(textUserName, textUserEmail)
        }

        config = findViewById(R.id.btnConfig)
        imgFortaleza300 = findViewById(R.id.capaFortaleza)

        config.setOnClickListener {
            abrirMenuConfig()
        }

        imgFortaleza300.setOnClickListener {
            val intent = Intent(this, BookpageActivity::class.java)
            startActivity(intent)
        }

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)
    }
    private fun carregarDadosDoUsuario(
        textUserName: TextView,
        textUserEmail: TextView
    ) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    textUserName.text = documento.getString("nome") ?: "Usuário"
                    textUserEmail.text = documento.getString("email")
                        ?: auth.currentUser?.email ?: ""
                } else {
                    // Documento não encontrado, usa os dados do Firebase Auth como fallback
                    textUserName.text = "Usuário"
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
            val editIntent = Intent(this, EditProfileActivity::class.java)

            editIntent.putExtra("userName", intent.getStringExtra("userName"))
            editIntent.putExtra("userEmail", intent.getStringExtra("userEmail"))
            editIntent.putExtra("userType", intent.getStringExtra("userType"))

            startActivity(editIntent)
        }

        view.findViewById<LinearLayout>(R.id.textViewSobreApp).setOnClickListener {
            bottomSheet.dismiss()
            val intent = Intent(this, AboutActivity::class.java)
            intent.putExtra("userName", intent.getStringExtra("userName"))
            intent.putExtra("userEmail", intent.getStringExtra("userEmail"))
            intent.putExtra("userType", intent.getStringExtra("userType"))

            startActivity(intent)
        }

        view.findViewById<LinearLayout>(R.id.textViewSobreApp).setOnClickListener {
            bottomSheet.dismiss()

            val aboutIntent = Intent(this, AboutActivity::class.java)

            aboutIntent.putExtra("userName", intent.getStringExtra("userName"))
            aboutIntent.putExtra("userEmail", intent.getStringExtra("userEmail"))
            aboutIntent.putExtra("userType", intent.getStringExtra("userType"))

            startActivity(aboutIntent)
        }

        bottomSheet.show()
    }
}
