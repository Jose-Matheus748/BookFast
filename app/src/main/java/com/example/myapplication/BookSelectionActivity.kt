package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

class BookSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_selection)

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val painelSelecionados = findViewById<ScrollView>(R.id.painelSelecionados)
        val painelReservas = findViewById<ScrollView>(R.id.painelReservas)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        painelSelecionados.visibility = View.VISIBLE
                        painelReservas.visibility     = View.GONE
                    }
                    1 -> {
                        painelSelecionados.visibility = View.GONE
                        painelReservas.visibility     = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}