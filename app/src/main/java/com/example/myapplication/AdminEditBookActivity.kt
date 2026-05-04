package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AdminEditBookActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit_book)

        HeaderAdminNavigation.setup(this)
    }
}