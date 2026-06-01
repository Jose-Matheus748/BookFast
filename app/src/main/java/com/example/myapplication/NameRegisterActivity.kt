package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class NameRegisterActivity : AppCompatActivity() {

    private lateinit var nameInputText: TextInputEditText
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_register)

        nameInputText = findViewById(R.id.textName)
        btnNext = findViewById(R.id.btnSelecionar)

        btnNext.setOnClickListener {
            validateAndNext()
        }
    }

    private fun validateAndNext() {
        val name = nameInputText.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Digite seu nome", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("name", name)
        startActivity(intent)
    }
}