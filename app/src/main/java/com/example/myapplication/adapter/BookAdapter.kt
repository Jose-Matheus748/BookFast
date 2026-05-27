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

    private val onClick: (Book) -> Unit  //  adicionar este parâmetro

    private val onItemClick: (Book) -> Unit  //  callback de clique

): RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    // ViewHolder: guarda as referências dos elementos visuais de UM item
    class BookViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imgLivro: ImageView = itemView.findViewById(R.id.imgBook)
        val tituloLivro: TextView = itemView.findViewById(R.id.tvTitle)
        val autorLivro: TextView = itemView.findViewById(R.id.tvAuthor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val visualizacao = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)


        return BookViewHolder(visualizacao)
    }

    // Preenche um item da lista com dados.
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val livro = books[position]

        holder.tituloLivro.text = livro.title
        holder.autorLivro.text = livro.author

        carregarCapaDoLivro(holder, livro)
        holder.itemView.setOnClickListener { onClick(livro) }
    }

    private fun carregarCapaDoLivro(holder: BookViewHolder, livro: Book) {
        if(!livro.capaBase64.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(livro.capaBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.imgLivro.setImageBitmap(bitmap)
            } catch (error: Exception) {
                holder.imgLivro.setImageResource(livro.imageUrl)
            }
        } else {
            holder.imgLivro.setImageResource(livro.imageUrl)
        }
        return BookViewHolder(visualizacao)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val livro = books[position]
        holder.tituloLivro.text = livro.title
        holder.autorLivro.text = livro.author

        if (!livro.capaBase64.isNullOrEmpty()) {
            // Carrega a imagem da URL do Firestore com Glide
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(livro.capaBase64)
                .placeholder(R.drawable.bg_book_cover_placeholder)
                .error(R.drawable.bg_book_cover_placeholder)
                .into(holder.imgLivro)
        } else {
            // Fallback pro drawable local
            holder.imgLivro.setImageResource(livro.imageUrl)
        }

        holder.itemView.setOnClickListener { onItemClick(livro) }
    }

    override fun getItemCount(): Int = books.size

    fun atualizarLista(novaLista: List<Book>) {
        books = novaLista
        notifyDataSetChanged() // Avisa ao RecyclerView que os dados mudaram
        notifyDataSetChanged()
    }
}
