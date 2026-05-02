package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var inputEmailAddress: EditText
    private lateinit var inputPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        inputEmailAddress = findViewById(R.id.inputEmailAddress)
        inputPassword = findViewById(R.id.inputPassword)

        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            validarLogin()
        }
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
}