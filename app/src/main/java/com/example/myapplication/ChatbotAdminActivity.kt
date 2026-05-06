package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
class ChatbotAdminActivity : AppCompatActivity() {

    lateinit var btnSend : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagina_chatbot_admin)

        btnSend = findViewById(R.id.btnSendAdmin)

        btnSend.setOnClickListener {
            val intent = Intent(this, ChatbotMessageAdminActivity::class.java)
            startActivity(intent)
        }

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)
    }
}