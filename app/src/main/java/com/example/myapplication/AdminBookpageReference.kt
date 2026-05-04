package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AdminBookpageReference : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_bookpage_reference)

        HeaderAdminNavigation.setup(this)
    }
}