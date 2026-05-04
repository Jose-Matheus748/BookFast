package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AdminBookpageActivity : AppCompatActivity() {

    private lateinit var imgPencil: ImageView
    private lateinit var imgTrash: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookpage_admin)

        imgPencil = findViewById(R.id.imgPencil)
        imgTrash = findViewById(R.id.imgTrash)

        imgPencil.setOnClickListener {
            navegarParaEdicaoDoLivro()
        }

        imgTrash.setOnClickListener {
            mostrarModalExcluirLivro()
        }
    }

    private fun navegarParaEdicaoDoLivro() {
        startActivity(Intent(this, AdminEditBookActivity::class.java))
    }

    private fun mostrarModalExcluirLivro() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Livro")
            .setMessage("Você tem certeza que deseja excluir esse livro?")
            .setPositiveButton("Sim") { dialog, _ ->
                dialog.dismiss()
                mostrarModalLivroExcluido()
            }
            .setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun mostrarModalLivroExcluido() {
        AlertDialog.Builder(this)
            .setTitle("Sucesso")
            .setMessage("Livro excluído com sucesso!")
            .setPositiveButton("Voltar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}