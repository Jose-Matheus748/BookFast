package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

class AdminGestaoPedidos : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var painelReservas: ScrollView
    private lateinit var painelDevolucoes: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gestao_pedidos)

        tabLayout = findViewById(R.id.tabLayout)
        painelReservas = findViewById(R.id.painelReservas)
        painelDevolucoes = findViewById(R.id.painelDevolucoes)

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)

        mostrarReservas()

        // Tela de reservas: primeira aba selecionada
        tabLayout.getTabAt(0)?.select()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> mostrarReservas()
                    1 -> mostrarDevolucoes()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun mostrarReservas() {
        painelReservas.visibility = ScrollView.VISIBLE
        painelDevolucoes.visibility = ScrollView.GONE
    }

    private fun mostrarDevolucoes() {
        painelReservas.visibility = ScrollView.GONE
        painelDevolucoes.visibility = ScrollView.VISIBLE
    }
}