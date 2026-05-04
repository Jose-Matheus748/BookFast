package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {

    lateinit var btnSalvar: Button
    lateinit var btnVoltar: Button

    private val favoritosVisiveis = mutableSetOf(1, 2, 3, 4)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        btnSalvar = findViewById(R.id.btnNext)
        btnVoltar = findViewById(R.id.btnVoltar)

        configurarRemocaoFavoritos()

        btnSalvar.setOnClickListener {
            Toast.makeText(this, "Alterações salvas!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, PaginaPerfilActivity::class.java))
        }

        btnVoltar.setOnClickListener {
            startActivity(Intent(this, PaginaPerfilActivity::class.java))
        }

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)
    }

    private fun configurarRemocaoFavoritos() {
        val itens = mapOf(
            R.id.btnRemoverFav1 to R.id.itemFav1,
            R.id.btnRemoverFav2 to R.id.itemFav2,
            R.id.btnRemoverFav3 to R.id.itemFav3,
            R.id.btnRemoverFav4 to R.id.itemFav4
        )

        itens.forEach { (btnId, itemId) ->
            val btn   = findViewById<Button>(btnId)
            val item  = findViewById<FrameLayout>(itemId)
            val numero = itens.keys.indexOf(btnId) + 1

            btn.setOnClickListener {
                item.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .withEndAction {
                        item.visibility = View.GONE
                        favoritosVisiveis.remove(numero)
                        reorganizarGrid()
                    }
                    .start()
            }
        }
    }

    private fun reorganizarGrid() {
        if (favoritosVisiveis.isEmpty()) {
            Toast.makeText(this, "Nenhum favorito restante.", Toast.LENGTH_SHORT).show()
        }
    }
}