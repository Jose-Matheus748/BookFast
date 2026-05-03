package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.tabs.TabLayout

class EmptyBookSelectionActivity : AppCompatActivity() {

    lateinit var btnBuscar : Button
    lateinit var btnBuscarReserva : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paginaselecionado)

        btnBuscar = findViewById(R.id.btnBuscarLivros)
        btnBuscarReserva = findViewById(R.id.btnBuscarLivrosReserva)

        btnBuscar.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }

        btnBuscarReserva.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val painelSelecionado = findViewById<LinearLayout>(R.id.painelSelecionado)
        val painelReservas = findViewById<LinearLayout>(R.id.painelReservas)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        painelSelecionado.visibility = View.VISIBLE
                        painelReservas.visibility    = View.GONE
                    }
                    1 -> {
                        painelSelecionado.visibility = View.GONE
                        painelReservas.visibility    = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}