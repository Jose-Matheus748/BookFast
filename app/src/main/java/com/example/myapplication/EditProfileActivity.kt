package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditProfileActivity : AppCompatActivity() {

    lateinit var btnSalvar : Button
    lateinit var btnVoltar : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        btnSalvar = findViewById(R.id.btnNext)
        btnVoltar = findViewById(R.id.btnVoltar)

        btnSalvar.setOnClickListener {
            val intent = Intent(this, PaginaPerfilActivity::class.java)
            startActivity(intent)
        }

        btnVoltar.setOnClickListener {
            val intent = Intent(this, PaginaPerfilActivity::class.java)
            startActivity(intent)
        }

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)
    }
}