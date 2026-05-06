package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.widget.ImageView

object FooterAdminNavigation {

    fun setup(activity: Activity) {
        val imageHome     = activity.findViewById<ImageView>(R.id.imageHome)
        val imageBooks    = activity.findViewById<ImageView>(R.id.imageBooks)
        val imageMessages = activity.findViewById<ImageView>(R.id.imageMessages)
        val imageAccount  = activity.findViewById<ImageView>(R.id.imageAccount)

        imageHome.setOnClickListener {
            if (activity !is HomePageAdmin) {
                activity.startActivity(Intent(activity, HomePageAdmin::class.java))
            }
        }

        imageBooks.setOnClickListener {
            if (activity !is AdminGestaoPedidos) {
                activity.startActivity(Intent(activity, AdminGestaoPedidos::class.java))
            }
        }

        imageMessages.setOnClickListener {
            if (activity !is ChatbotAdminActivity) {
                activity.startActivity(Intent(activity, ChatbotAdminActivity::class.java))
            }
        }

        imageAccount.setOnClickListener {
            if (activity !is PaginaPerfilAdmin) {
                val intent = Intent(activity, PaginaPerfilAdmin::class.java)
                intent.putExtra("userName", activity.intent.getStringExtra("userName"))
                intent.putExtra("userEmail", activity.intent.getStringExtra("userEmail"))
                activity.startActivity(intent)
            }
        }
    }
}