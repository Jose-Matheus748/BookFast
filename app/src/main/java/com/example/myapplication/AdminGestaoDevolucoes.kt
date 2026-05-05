package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

class AdminGestaoDevolucoes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_gestao_devolucoes)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)

        // Tela de devoluções: segunda aba selecionada
        tabLayout.getTabAt(1)?.select()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        val intent =
                            Intent(this@AdminGestaoDevolucoes, AdminGestaoPedidos::class.java)
                        startActivity(intent)
                        finish()
                    }

                    1 -> {
                        // Já está na tela de Devoluções
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}