package com.example.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.myapplication.model.Book
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.FirebaseAuth


class HomePageActivity : AppCompatActivity() {

    // livros do carrossel em destaque
    private lateinit var img1: ImageView
    private lateinit var img2: ImageView
    private lateinit var img3: ImageView
    private lateinit var tituloDestaque1: TextView
    private lateinit var tituloDestaque2: TextView
    private lateinit var tituloDestaque3: TextView
    private lateinit var btnAnterior: FloatingActionButton
    private lateinit var btnProximo: FloatingActionButton

    // Novas aquisições
    private lateinit var imgNovas1: ImageView
    private lateinit var imgNovas2: ImageView
    private lateinit var imgNovas3: ImageView
    private lateinit var imgNovas4: ImageView
    private lateinit var tituloNovas1: TextView
    private lateinit var tituloNovas2: TextView
    private lateinit var tituloNovas3: TextView
    private lateinit var tituloNovas4: TextView

    // Pesquisa Científica
    private lateinit var imgCiencias1: ImageView
    private lateinit var imgCiencias2: ImageView
    private lateinit var imgCiencias3: ImageView
    private lateinit var imgCiencias4: ImageView
    private lateinit var tituloCiencias1: TextView
    private lateinit var tituloCiencias2: TextView
    private lateinit var tituloCiencias3: TextView
    private lateinit var tituloCiencias4: TextView

    // Busca e layout
    private lateinit var btnSearch: ImageButton
    private lateinit var etSearch: EditText
    private lateinit var mainLayout: View

    private val db = Firebase.firestore
    private val todosOsLivros = mutableListOf<Book>()

    // Carrossel
    private var grupoAtual = 0
    private val tamanhoGrupo = 3
    private val imagemParaLivro = mutableMapOf<ImageView, Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        HeaderNavigation.setup(this)
        FooterNavigation.setup(this)

        mainLayout = findViewById(R.id.home_page)

        img1 = findViewById(R.id.capaFortaleza)
        img2 = findViewById(R.id.como_elaborar)
        img3 = findViewById(R.id.arvores)
        tituloDestaque1 = findViewById(R.id.tituloDestaque1)
        tituloDestaque2 = findViewById(R.id.tituloDestaque2)
        tituloDestaque3 = findViewById(R.id.tituloDestaque3)
        btnAnterior = findViewById(R.id.btnAnterior)
        btnProximo  = findViewById(R.id.btnProximo)

        //aq sao os livros da aba de novas aquisicoes
        imgNovas1 = findViewById(R.id.estudo_vermelho)
        imgNovas2 = findViewById(R.id.witcher_last_wish)
        imgNovas3 = findViewById(R.id.metamorfose)
        imgNovas4 = findViewById(R.id.memorias_postumas)
        tituloNovas1 = findViewById(R.id.tituloNovas1)
        tituloNovas2 = findViewById(R.id.tituloNovas2)
        tituloNovas3 = findViewById(R.id.tituloNovas3)
        tituloNovas4 = findViewById(R.id.tituloNovas4)

        //aq tao os livros da aba de pesquisa cientifica
        imgCiencias1 = findViewById(R.id.discurso_ciencias)
        imgCiencias2 = findViewById(R.id.metodologias_cien_era_digital)
        imgCiencias3 = findViewById(R.id.mito_neutralidade)
        imgCiencias4 = findViewById(R.id.livro14)
        tituloCiencias1 = findViewById(R.id.tituloCiencias1)
        tituloCiencias2 = findViewById(R.id.tituloCiencias2)
        tituloCiencias3 = findViewById(R.id.tituloCiencias3)
        tituloCiencias4 = findViewById(R.id.tituloCiencias4)

        btnSearch = findViewById(R.id.btnSearch)
        etSearch  = findViewById(R.id.etSearch)

        btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchListActivity::class.java))
        }

        mainLayout.setOnClickListener {
            if (etSearch.isVisible) fecharBusca()
        }

        btnProximo.setOnClickListener {
            val totalGrupos = calcularTotalGrupos()
            if (totalGrupos == 0) return@setOnClickListener
            grupoAtual = (grupoAtual + 1) % totalGrupos
            mostrarGrupoDestaque()
        }

        btnAnterior.setOnClickListener {
            val totalGrupos = calcularTotalGrupos()
            if (totalGrupos == 0) return@setOnClickListener
            grupoAtual = if (grupoAtual - 1 < 0) totalGrupos - 1 else grupoAtual - 1
            mostrarGrupoDestaque()
        }

        listOf(img1, img2, img3).forEach { imgView ->
            imgView.setOnClickListener {
                val livro = imagemParaLivro[imgView] ?: return@setOnClickListener
                abrirBookpage(livro.id)
            }
        }

        listOf(imgNovas1, imgNovas2, imgNovas3, imgNovas4,
            imgCiencias1, imgCiencias2, imgCiencias3, imgCiencias4
        ).forEach { imgView ->
            imgView.setOnClickListener {
                val livro = imagemParaLivro[imgView] ?: return@setOnClickListener
                abrirBookpage(livro.id)
            }
        }

        carregarLivros()
    }

    private fun abrirBookpage(livroId: String) {
        val intent = Intent(this, BookpageActivity::class.java)
        intent.putExtra("LIVRO_ID", livroId)
        startActivity(intent)
    }

    private fun carregarLivros() {
        db.collection("Livros")
            .get()
            .addOnSuccessListener { result ->
                todosOsLivros.clear()
                for (doc in result) {
                    val autores = (doc.get("autores") as? List<*>)
                        ?.filterIsInstance<String>()
                        ?.joinToString(", ") ?: ""
                    todosOsLivros.add(
                        Book(
                            id         = doc.id,
                            title      = doc.getString("titulo") ?: "Sem título",
                            author     = autores,
                            capaBase64 = doc.getString("capaUrl"),
                            imageUrl   = R.drawable.bg_book_cover_placeholder
                        )
                    )
                }
                popularTodasAsSecoes()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar livros", Toast.LENGTH_SHORT).show()
            }
    }

    private fun popularTodasAsSecoes() {
        mostrarGrupoDestaque()
        popularSecao(
            livros      = todosOsLivros.take(4),        // primeiros 4
            imgViews    = listOf(imgNovas1, imgNovas2, imgNovas3, imgNovas4),
            txtViews    = listOf(tituloNovas1, tituloNovas2, tituloNovas3, tituloNovas4)
        )
        popularSecao(
            livros      = todosOsLivros.takeLast(4),    // últimos 4 (ou menos)
            imgViews    = listOf(imgCiencias1, imgCiencias2, imgCiencias3, imgCiencias4),
            txtViews    = listOf(tituloCiencias1, tituloCiencias2, tituloCiencias3, tituloCiencias4)
        )
    }

    private fun popularSecao(livros: List<Book>, imgViews: List<ImageView>, txtViews: List<TextView>) {
        imgViews.forEachIndexed { i, imgView ->
            val txtView = txtViews[i]
            if (i < livros.size) {
                val livro = livros[i]
                imagemParaLivro[imgView] = livro
                imgView.visibility = View.VISIBLE
                txtView.visibility = View.VISIBLE
                txtView.text = livro.title
                exibirCapa(imgView, livro)

                (imgView.parent?.parent as? View)?.visibility = View.VISIBLE
            } else {
                imgView.visibility = View.GONE
                txtView.visibility = View.GONE
                (imgView.parent?.parent as? View)?.visibility = View.GONE
            }
        }
    }


    private fun calcularTotalGrupos(): Int {
        // Usa todos os livros para o carrossel; mínimo 1 grupo se houver livros
        return if (todosOsLivros.isEmpty()) 0
        else Math.ceil(todosOsLivros.size.toDouble() / tamanhoGrupo).toInt()
    }

    private fun mostrarGrupoDestaque() {
        val imgViews = listOf(img1, img2, img3)
        val txtViews = listOf(tituloDestaque1, tituloDestaque2, tituloDestaque3)
        val inicio   = grupoAtual * tamanhoGrupo

        imgViews.forEachIndexed { i, imgView ->
            val txtView = txtViews[i]
            val idx     = inicio + i
            if (idx < todosOsLivros.size) {
                val livro = todosOsLivros[idx]
                imagemParaLivro[imgView] = livro
                imgView.visibility = View.VISIBLE
                txtView.visibility = View.VISIBLE
                txtView.text       = livro.title
                exibirCapa(imgView, livro)
            } else {
                imgView.visibility = View.INVISIBLE
                txtView.visibility = View.INVISIBLE
                imagemParaLivro.remove(imgView)
            }
        }   // ← este } fecha o forEachIndexed

        val totalGrupos = calcularTotalGrupos()
        btnAnterior.visibility = if (totalGrupos > 1) View.VISIBLE else View.GONE
        btnProximo.visibility  = if (totalGrupos > 1) View.VISIBLE else View.GONE
    }   // ← este } fecha o mostrarGrupoDestaque


    override fun onStart() {
        super.onStart()

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun exibirCapa(imgView: ImageView, livro: Book) {
        if (!livro.capaBase64.isNullOrEmpty()) {
            try {
                val bytes  = Base64.decode(livro.capaBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imgView.setImageBitmap(bitmap)
                return
            } catch (e: Exception) { /* fallback */ }
        }
        imgView.setImageResource(livro.imageUrl)
    }

    override fun onBackPressed() {
        if (etSearch.isVisible) fecharBusca() else super.onBackPressed()
    }

    private fun fecharBusca() {
        etSearch.setText("")
        etSearch.visibility = View.GONE
        btnSearch.visibility = View.VISIBLE
    }
}