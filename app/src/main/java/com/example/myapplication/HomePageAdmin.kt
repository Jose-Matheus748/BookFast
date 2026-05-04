package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HomePageAdmin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page_admin)

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)
    }
}