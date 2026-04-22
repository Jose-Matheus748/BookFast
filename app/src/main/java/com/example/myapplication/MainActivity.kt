package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    private lateinit var nameInputText: TextInputEditText
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        nameInputText = findViewById(R.id.textName)
        btnNext = findViewById(R.id.btnSelecionar)

        btnNext.setOnClickListener {
            showUserName()
        }
    }

    private fun showUserName() {
        val name = nameInputText.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Você não digitou seu nome", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Olá, $name!", Toast.LENGTH_SHORT).show()
        }
    }
}