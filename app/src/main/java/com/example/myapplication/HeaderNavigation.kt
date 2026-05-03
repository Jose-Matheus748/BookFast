package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton

object HeaderNavigation {

    fun setup(activity: Activity, rootView: View? = null) {
        val btnSearch = activity.findViewById<ImageButton>(R.id.btnSearch)
        val etSearch  = activity.findViewById<EditText>(R.id.etSearch)

        btnSearch.setOnClickListener {
            val intent = Intent(activity, SearchListActivity::class.java)
            activity.startActivity(intent)
        }

        etSearch.setOnClickListener {
        }

        rootView?.setOnClickListener {
            if (etSearch.visibility == View.VISIBLE) {
                fecharBusca(btnSearch, etSearch)
            }
        }
    }

    private fun fecharBusca(btnSearch: ImageButton, etSearch: EditText) {
        etSearch.setText("")
        etSearch.visibility = View.GONE
        btnSearch.visibility = View.VISIBLE
    }
}