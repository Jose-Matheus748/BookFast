package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AboutActivity : AppCompatActivity() {

    lateinit var imgBack : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        imgBack = findViewById(R.id.arrowBackId)

        imgBack.setOnClickListener {
            val userType = intent.getStringExtra("userType")

            if (userType == "admin") {
                val adminIntent = Intent(this, PaginaPerfilAdmin::class.java)

                adminIntent.putExtra("userName", intent.getStringExtra("userName"))
                adminIntent.putExtra("userEmail", intent.getStringExtra("userEmail"))
                adminIntent.putExtra("userType", userType)

                startActivity(adminIntent)

            } else {
                val userIntent = Intent(this, PaginaPerfilActivity::class.java)

                userIntent.putExtra("userName", intent.getStringExtra("userName"))
                userIntent.putExtra("userEmail", intent.getStringExtra("userEmail"))
                userIntent.putExtra("userType", userType)

                startActivity(userIntent)
            }

            finish()
        }

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)
    }
}
