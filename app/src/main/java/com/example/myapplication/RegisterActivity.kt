
package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class RegisterActivity : AppCompatActivity() {

    lateinit var tvBoasvindas: TextView
    lateinit var etEmail: EditText
    lateinit var etSenha: EditText
    lateinit var etConfirmarSenha: EditText
    lateinit var btnCriarConta: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_acess)

        val name = intent.getStringExtra("name") ?: "Usuario 1"

        tvBoasvindas = findViewById(R.id.titulo2)
        etEmail = findViewById(R.id.editTextTextEmailAddress3)
        etSenha = findViewById(R.id.editTextTextPassword2)
        etConfirmarSenha = findViewById(R.id.editTextTextPassword4)
        btnCriarConta = findViewById(R.id.btnNext4)

        tvBoasvindas.text = "Bem vindo, $name"

        btnCriarConta.setOnClickListener {

            val nome = intent.getStringExtra("name")
                ?.trim()
                ?.ifBlank { "Usuario 1" }
                ?: "Usuario 1"

            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()
            val confirmarSenha = etConfirmarSenha.text.toString().trim()

            val userEmail = "usuario1@bookfast.com"
            val userPassword = "123456"
            val userName = nome

            val adminEmail = "admin@bookfast.com"
            val adminPassword = "admin123"
            val adminName = "Admin"

            if (email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()

            } else if (senha != confirmarSenha) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()

            } else if (email == adminEmail && senha == adminPassword) {
                val intent = Intent(this, HomePageAdmin::class.java)
                intent.putExtra("userName", nome)
                intent.putExtra("userEmail", adminEmail)
                startActivity(intent)
                finish()

            } else if (email == userEmail && senha == userPassword) {
                val intent = Intent(this, HomePageActivity::class.java)
                intent.putExtra("userName", userName)
                intent.putExtra("userEmail", userEmail)
                startActivity(intent)
                finish()

            } else {
                val intent = Intent(this, HomePageActivity::class.java)
                intent.putExtra("userName", nome)
                intent.putExtra("userEmail", email)
                startActivity(intent)
                finish()
            }
        }
    }
}