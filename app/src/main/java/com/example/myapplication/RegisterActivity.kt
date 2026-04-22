
package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    lateinit var tvBoasvindas:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_acess)
        var valor = intent.getStringExtra("name")
        tvBoasvindas = findViewById(R.id.titulo2)
        tvBoasvindas.text = "Bem vindo, "+valor
    }
}