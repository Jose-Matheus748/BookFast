package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class ForgottenPasswordActivity: AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgoten_password)

        val inputEmail = findViewById<EditText>(R.id.editTextTextEmailAddress2)
        val btnRecuperar = findViewById<Button>(R.id.btnNext2)
        val linkLogin = findViewById<TextView>(R.id.linkLogin)

        btnRecuperar.setOnClickListener {
            val email = inputEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Informe o email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.recuperarSenha(email) // ← envia email pelo Firebase
        }

        viewModel.recuperacaoResult.observe(this) { resultado ->
            resultado.onSuccess {
                Toast.makeText(this, "Email de recuperação enviado!", Toast.LENGTH_LONG).show()
                finish()
            }.onFailure { erro ->
                Toast.makeText(this, "Erro: ${erro.message}", Toast.LENGTH_SHORT).show()
            }
        }

        linkLogin.setOnClickListener { finish() }
    }
}