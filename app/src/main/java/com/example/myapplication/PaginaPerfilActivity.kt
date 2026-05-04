package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class PaginaPerfilActivity : AppCompatActivity(){

    lateinit var config : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paginaperfil)

        val textUserName = findViewById<TextView>(R.id.userName)
        val userName = intent.getStringExtra("userName")
        textUserName.text = "($userName)"

        val textUserEmail = findViewById<TextView>(R.id.userEmail)
        val userEmail = intent.getStringExtra("userEmail")
        textUserEmail.text = userEmail

        config = findViewById(R.id.btnConfig)

        config.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)
    }
}