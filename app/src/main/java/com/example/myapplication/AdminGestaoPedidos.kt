package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

class AdminGestaoPedidos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gestao_pedidos)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)

        // Tela de reservas: primeira aba selecionada
        tabLayout.getTabAt(0)?.select()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // Já está na tela de Reservas
                    }

                    1 -> {
                        val intent =
                            Intent(this@AdminGestaoPedidos, AdminGestaoDevolucoes::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}