package com.example.myapplication.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Book

class SelectedBookAdapter(
    private var livros: List<Book>,
    private val onReservar: (Book) -> Unit,
    private val onRemover: (Book) -> Unit
) : RecyclerView.Adapter<SelectedBookAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val capa: ImageView = itemView.findViewById(R.id.imgLivroSelecionado)
        val titulo: TextView = itemView.findViewById(R.id.tituloLivroSelecionadoItem)
        val autores: TextView = itemView.findViewById(R.id.autoresLivroSelecionadoItem)
        val check: CheckBox = itemView.findViewById(R.id.checkLivroSelecionadoItem)
        val btnReservar: Button = itemView.findViewById(R.id.btnReservarLivroSelecionadoItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book_selected, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val livro = livros[position]

        holder.titulo.text = livro.title
        holder.autores.text = livro.author

        carregarCapa(holder.capa, livro)

        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = true

        holder.check.setOnCheckedChangeListener { _, marcado ->
            if (!marcado) {
                onRemover(livro)
            }
        }

        holder.btnReservar.setOnClickListener {
            onReservar(livro)
        }
    }

    override fun getItemCount(): Int = livros.size

    fun atualizarLista(novaLista: List<Book>) {
        livros = novaLista
        notifyDataSetChanged()
    }

    private fun carregarCapa(imageView: ImageView, livro: Book) {
        if (!livro.capaBase64.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(livro.capaBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageView.setImageBitmap(bitmap)
                return
            } catch (error: Exception) {
                Log.e("SelectedBookAdapter", "Erro ao decodificar a imagem base64: ${error.message}")
            }
        }

        imageView.setImageResource(livro.imageUrl)
    }
}