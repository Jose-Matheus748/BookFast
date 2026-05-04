package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CreateBookActivity : AppCompatActivity() {

    private var detalhesAbertos = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_book)

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)

        val arrowExpand = findViewById<ImageView>(R.id.arrowExpandId)
        val layoutDetalhes = findViewById<View>(R.id.layoutDetalhes)

        arrowExpand.setOnClickListener {
            detalhesAbertos = !detalhesAbertos

            if (detalhesAbertos) {
                layoutDetalhes.visibility = View.VISIBLE
                arrowExpand.rotation = 270f
            } else {
                layoutDetalhes.visibility = View.GONE
                arrowExpand.rotation = 90f
            }
        }
    }
}