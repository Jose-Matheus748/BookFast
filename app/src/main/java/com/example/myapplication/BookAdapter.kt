package com.example.myapplication

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.model.Book

class BookAdapter( // Adapter: ele pega a lista de livros e mostra na tela
    private var books: List<Book>
): RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    // ViewHolder: guarda as referências dos elementos visuais de UM item
    class BookViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imgLivro: ImageView = itemView.findViewById(R.id.imgBook)
        val tituloLivro: TextView = itemView.findViewById(R.id.tvTitle)
        val autorLivro: TextView = itemView.findViewById(R.id.tvAuthor)
    }

    // Cria o layout visual de cada item da lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {

        // "Infla" o XML item_book.xml, ou seja, transforma ele em View
        val visualizacao = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)

        // Retorna o ViewHolder com esse layout
        return BookViewHolder(visualizacao)
    }

    // Coloca os dados do livro dentro do item visual
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val livro = books[position]
        holder.imgLivro.setImageResource(livro.imageUrl)
        holder.tituloLivro.text = livro.title
        holder.autorLivro.text = livro.author
    }

    // Informa quantos itens existem na lista
    override fun getItemCount(): Int = books.size

    fun atualizarLista(novaLista: List<Book>) {
        books = novaLista

        // Avisa ao RecyclerView que os dados mudaram
        notifyDataSetChanged()
    }
}