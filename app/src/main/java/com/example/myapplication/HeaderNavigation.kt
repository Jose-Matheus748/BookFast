package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton

object HeaderNavigation {
    fun setup(activity: Activity, rootView: View? = null) {
        val etSearch = activity.findViewById<EditText>(R.id.etSearch)

        if (activity !is SearchListActivity) {
            val btnSearch = activity.findViewById<ImageButton>(R.id.btnSearch)
            btnSearch.setOnClickListener {
                val intent = Intent(activity, SearchListActivity::class.java)
                activity.startActivity(intent)
            }
        }

        etSearch.setOnClickListener {
            // Por enquanto não precisa fazer nada aqui
        }

    }

    private fun fecharBusca(btnSearch: ImageButton, etSearch: EditText) {
        etSearch.setText("")
        etSearch.visibility = View.GONE
        btnSearch.visibility = View.VISIBLE
    }
}