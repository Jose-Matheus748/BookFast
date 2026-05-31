package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class NameRegisterActivity : AppCompatActivity() {

    private lateinit var nameInputText: TextInputEditText
    private lateinit var btnNext: Button

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_register)

        // Se o usuário já está autenticado, redireciona sem passar pelo cadastro
        if (auth.currentUser != null) {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
            return
        }

        nameInputText = findViewById(R.id.textName)
        btnNext = findViewById(R.id.btnSelecionar)

        btnNext.setOnClickListener {
            showUserName()
        }
    }

    private fun showUserName() {
        val name = nameInputText.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(
                this,
                "Você não digitou seu nome",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        validateData()
    }

    private fun validateData() {
        val name = nameInputText.text.toString().trim().ifBlank {
            "Usuario 1"
        }

        val intention = Intent(this, RegisterActivity::class.java)
        intention.putExtra("name", name)
        startActivity(intention)
    }
}