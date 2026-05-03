
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

        val valor = intent.getStringExtra("name")

        tvBoasvindas = findViewById(R.id.titulo2)
        etEmail = findViewById(R.id.editTextTextEmailAddress3)
        etSenha = findViewById(R.id.editTextTextPassword2)
        etConfirmarSenha = findViewById(R.id.editTextTextPassword4)
        btnCriarConta = findViewById(R.id.btnNext4)

        tvBoasvindas.text = "Bem vindo, $valor"

        btnCriarConta.setOnClickListener {

            val email = etEmail.text.toString()
            val senha = etSenha.text.toString()
            val confirmarSenha = etConfirmarSenha.text.toString()

            if (email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else if (senha != confirmarSenha) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, HomePageActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}