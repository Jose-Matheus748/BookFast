package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels

class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var inputEmailAddress: EditText
    private lateinit var inputPassword: EditText
    private lateinit var linkForgotPassword: TextView
    private lateinit var btnLogin: Button
    private lateinit var textLinkRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {

            val uid = auth.currentUser!!.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->

                    val tipo = document.getString("tipo")

                    if (tipo == "admin") {
                        startActivity(Intent(this, HomePageAdmin::class.java))
                    } else {
                        startActivity(Intent(this, HomePageActivity::class.java))
                    }

                    finish()
                }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        inputEmailAddress = findViewById(R.id.inputEmailAddress)
        inputPassword = findViewById(R.id.inputPassword)
        linkForgotPassword = findViewById(R.id.linkForgotPassword)
        btnLogin = findViewById(R.id.btnLogin)
        textLinkRegister = findViewById(R.id.textLinkRegister)

        viewModel.loginResult.observe(this) { resultado ->
            resultado.onSuccess { user ->
                val destino = when (user.perfil) {
                    "admin" -> Intent(this, HomePageAdmin::class.java)
                    else -> Intent(this, HomePageActivity::class.java)
                }
                destino.putExtra("userName", user.nome)
                destino.putExtra("userEmail", user.email)
                destino.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(destino)
            }.onFailure {
                Toast.makeText(this, "Credenciais inválidas", Toast.LENGTH_SHORT).show()
            }
        }

        linkForgotPassword.setOnClickListener {
            navegarParaRecuperacaoDeSenha()
        }

        btnLogin.setOnClickListener {
            validarLogin()
        }

        textLinkRegister.setOnClickListener {
            navegarParaRegistroDoUsuario()
        }
    }

    private fun navegarParaRecuperacaoDeSenha() {
        val intent = Intent(this, ForgottenPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun validarLogin() {
        val email = inputEmailAddress.text.toString().trim()
        val password = inputPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.login(email, password)
    }

    private fun navegarParaRegistroDoUsuario() {
        val intent = Intent(this, NameRegisterActivity::class.java)
        startActivity(intent)
    }
}