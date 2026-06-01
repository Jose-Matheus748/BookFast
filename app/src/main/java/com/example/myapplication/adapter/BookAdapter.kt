package com.example.myapplication.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Book

class BookAdapter(
    private var books: List<Book>,
    private val onClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgLivro: ImageView = itemView.findViewById(R.id.imgBook)
        val tituloLivro: TextView = itemView.findViewById(R.id.tvTitle)
        val autorLivro: TextView = itemView.findViewById(R.id.tvAuthor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val livro = books[position]
        holder.tituloLivro.text = livro.title
        holder.autorLivro.text = livro.author
        carregarCapa(holder, livro)
        holder.itemView.setOnClickListener { onClick(livro) }
    }

    private fun carregarCapa(holder: BookViewHolder, livro: Book) {
        if (!livro.capaBase64.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(livro.capaBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.imgLivro.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.imgLivro.setImageResource(livro.imageUrl)
            }
        } else {
            holder.imgLivro.setImageResource(livro.imageUrl)
        }
    }

    override fun getItemCount(): Int = books.size

    fun atualizarLista(novaLista: List<Book>) {
        books = novaLista
        notifyDataSetChanged()
    }
}