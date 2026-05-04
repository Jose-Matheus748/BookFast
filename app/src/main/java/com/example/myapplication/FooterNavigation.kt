package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.widget.ImageView

object FooterNavigation {

    fun setup(activity: Activity) {
        val imageHome     = activity.findViewById<ImageView>(R.id.imageHome)
        val imageBooks    = activity.findViewById<ImageView>(R.id.imageBooks)
        val imageMessages = activity.findViewById<ImageView>(R.id.imageMessages)
        val imageAccount  = activity.findViewById<ImageView>(R.id.imageAccount)

        imageHome.setOnClickListener {
            if (activity !is HomePageActivity) {
                activity.startActivity(Intent(activity, HomePageActivity::class.java))
            }
        }

        imageBooks.setOnClickListener {
            if (activity !is EmptyBookSelectionActivity) {
                activity.startActivity(Intent(activity, EmptyBookSelectionActivity::class.java))
            }
        }

        imageMessages.setOnClickListener {
            if (activity !is ChatbotActivity) {
                activity.startActivity(Intent(activity, ChatbotActivity::class.java))
            }
        }

        imageAccount.setOnClickListener {
            if (activity !is PaginaPerfilActivity) {
                val intent = Intent(activity, PaginaPerfilActivity::class.java)
                intent.putExtra("userName", activity.intent.getStringExtra("userName"))
                intent.putExtra("userEmail", activity.intent.getStringExtra("userEmail"))
                activity.startActivity(intent)
            }
        }
    }
}