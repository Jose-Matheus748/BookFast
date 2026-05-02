package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var inputEmailAddress: EditText
    private lateinit var inputPassword: EditText
    private lateinit var linkForgotPassword: TextView
    private lateinit var btnLogin: Button
    private lateinit var textLinkRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        inputEmailAddress = findViewById(R.id.inputEmailAddress)
        inputPassword = findViewById(R.id.inputPassword)
        linkForgotPassword = findViewById(R.id.linkForgotPassword)
        btnLogin = findViewById(R.id.btnLogin)
        textLinkRegister = findViewById(R.id.textLinkRegister)

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
        val intent = Intent(this, ForgotenPasswordActivity::class.java)
        startActivity(intent)
    }

    /**   validarLogin():
     *      pega o email e a senha digitados pelo usuário, converte em texto, e corta espaços
     *      se qualquer um dos campos estiver vazio, e eu clicar em login, o app mostra um Toaster de erro
     *      caso os dois campos estejam preenchidos e eu clicar em login, navega para a HomePageActivity
     */
    private fun validarLogin() {
        val email = inputEmailAddress.text.toString().trim()
        val password = inputPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this,
                "Por favor, preencha todos os campos",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navegarParaRegistroDoUsuario() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}