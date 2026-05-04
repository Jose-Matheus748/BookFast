package com.example.myapplication

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.model.Book
import com.google.android.material.bottomsheet.BottomSheetDialog

class PaginaPerfilActivity : AppCompatActivity() {

    lateinit var config: ImageView
    lateinit var imgFortaleza300 : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paginaperfil)

        val textUserName = findViewById<TextView>(R.id.userName)
        val userName = intent.getStringExtra("userName")
        textUserName.text = "$userName"

        val textUserEmail = findViewById<TextView>(R.id.userEmail)
        val userEmail = intent.getStringExtra("userEmail")
        textUserEmail.text = userEmail

        config = findViewById(R.id.btnConfig)
        imgFortaleza300 = findViewById(R.id.capaFortaleza)

        config.setOnClickListener {
            abrirMenuConfig()
        }

        imgFortaleza300.setOnClickListener {
            val intent = Intent(this, BookpageActivity::class.java)
            startActivity(intent)
        }

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)
    }

    private fun abrirMenuConfig() {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.component_user_options, null)
        bottomSheet.setContentView(view)

        view.findViewById<LinearLayout>(R.id.textViewEditarPerfil).setOnClickListener {
            bottomSheet.dismiss()
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.textViewSobreApp).setOnClickListener {
            bottomSheet.dismiss()
            startActivity(Intent(this, AboutActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.textViewSair).setOnClickListener {
            bottomSheet.dismiss()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        bottomSheet.show()
    }
}