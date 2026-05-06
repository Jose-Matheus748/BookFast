package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.content.Intent

class AdminEditBookActivity: AppCompatActivity() {

    lateinit var btnEnviar : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit_book)

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)

        btnEnviar = findViewById(R.id.btnEnviar)

        btnEnviar.setOnClickListener {
            val intent = Intent(this, AdminBookpageActivity::class.java)
            startActivity(intent)
        }
    }
}