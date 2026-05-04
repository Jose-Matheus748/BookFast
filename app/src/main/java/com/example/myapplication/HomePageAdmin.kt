package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class HomePageAdmin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page_admin)

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)

        val imgPlus = findViewById<ImageView>(R.id.imgPlus)

        imgPlus.setOnClickListener {
            val intent = Intent(this, CreateBookActivity::class.java)
            startActivity(intent)
        }
    }
}