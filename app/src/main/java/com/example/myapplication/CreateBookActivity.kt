package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import android.graphics.BitmapFactory

class CreateBookActivity : AppCompatActivity() {

    // Variáveis que só serão iniciadas no onCreate
    private lateinit var arrowBack: ImageView
    private lateinit var arrowExpand: ImageView
    private lateinit var layoutDetalhes: View
    private var detalhesAbertos = false

    private lateinit var btnAddAutor: ImageView
    private lateinit var autorBox2: View
    private lateinit var autorBox3: View
    private lateinit var autorBox4: View
    private lateinit var editAutor1: TextInputEditText
    private lateinit var editAutor2: TextInputEditText
    private lateinit var editAutor3: TextInputEditText
    private lateinit var editAutor4: TextInputEditText
    private lateinit var btnRemoverAutor2: View
    private lateinit var btnRemoverAutor3: View
    private lateinit var btnRemoverAutor4: View
    lateinit var btnEnviar: Button

    // Campos de input do formulário
    private lateinit var editTitulo: TextInputEditText
    private lateinit var editMaterial: TextInputEditText
    private lateinit var editIdioma: TextInputEditText
    private lateinit var editPublicacao: TextInputEditText
    private lateinit var editEdicao: TextInputEditText
    private lateinit var editSerie: TextInputEditText
    private lateinit var editAssuntos: TextInputEditText
    private lateinit var editReferencia: TextInputEditText

    // Capa do livro
    private lateinit var ctnDescricao: View
    private lateinit var imgCapaPreview: ImageView
    private lateinit var textViewCapa: android.widget.TextView
    private var imagemSelecionadaUri: Uri? = null

    // Conexão com o Firestore
    private val db = Firebase.firestore

    // Launcher para abrir a galeria do celular
    private val selecionarImagem = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagemSelecionadaUri = uri
            imgCapaPreview.visibility = View.VISIBLE
            textViewCapa.visibility = View.GONE
            // Converte para Base64 e exibe como Bitmap
            val base64 = converterImagemParaBase64(uri)
            val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imgCapaPreview.setImageBitmap(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_book)

        arrowBack = findViewById(R.id.arrowBackId)
        arrowExpand = findViewById(R.id.arrowExpandId)
        layoutDetalhes = findViewById(R.id.layoutDetalhes)

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)

        btnAddAutor = findViewById(R.id.btnAddAutor)
        autorBox2 = findViewById(R.id.autorBox2)
        autorBox3 = findViewById(R.id.autorBox3)
        autorBox4 = findViewById(R.id.autorBox4)
        editAutor1 = findViewById(R.id.editAutor1)
        editAutor2 = findViewById(R.id.editAutor2)
        editAutor3 = findViewById(R.id.editAutor3)
        editAutor4 = findViewById(R.id.editAutor4)
        btnRemoverAutor2 = findViewById(R.id.btnRemoveAutor2)
        btnRemoverAutor3 = findViewById(R.id.btnRemoveAutor3)
        btnRemoverAutor4 = findViewById(R.id.btnRemoveAutor4)
        btnEnviar = findViewById(R.id.btnEnviar)

        editTitulo     = findViewById(R.id.editTitulo)
        editMaterial   = findViewById(R.id.editMaterialInput)
        editIdioma     = findViewById(R.id.editIdiomaInput)
        editPublicacao = findViewById(R.id.editPublicacaoInput)
        editEdicao     = findViewById(R.id.editEdicaoInput)
        editSerie      = findViewById(R.id.editSerieInput)
        editAssuntos   = findViewById(R.id.editAssuntosInput)
        editReferencia = findViewById(R.id.editReferenciaInput)

        ctnDescricao   = findViewById(R.id.ctnDescricao)
        imgCapaPreview = findViewById(R.id.imgCapaPreview)
        textViewCapa   = findViewById(R.id.textViewCapa)

        arrowBack.setOnClickListener { voltarParaTelaAnterior() }
        arrowExpand.setOnClickListener { alternarDetalhes() }

        ctnDescricao.setOnClickListener {
            selecionarImagem.launch("image/*")
        }

        btnAddAutor.setOnClickListener { adicionarAutor() }
        btnRemoverAutor2.setOnClickListener { removerAutor(autorBox2, editAutor2) }
        btnRemoverAutor3.setOnClickListener { removerAutor(autorBox3, editAutor3) }
        btnRemoverAutor4.setOnClickListener { removerAutor(autorBox4, editAutor4) }
        btnEnviar.setOnClickListener { salvarLivro() }
    }

    private fun salvarLivro() {
        val titulo = editTitulo.text.toString().trim()

        if (titulo.isEmpty()) {
            Toast.makeText(this, "Por favor, informe o título do livro", Toast.LENGTH_SHORT).show()
            return
        }

        btnEnviar.isEnabled = false

        val capaBase64 = if (imagemSelecionadaUri != null) {
            converterImagemParaBase64(imagemSelecionadaUri!!)
        } else {
            ""
        }

        salvarNoFireStore(titulo, capaUrl = capaBase64)
    }

    private fun converterImagemParaBase64(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val imagemOriginal = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Redimensiona a imagem para no máximo 500x500 pixels
        val imagemRedimensionada = redimensionarBitmap(imagemOriginal, 500)

        // Comprime para JPEG com qualidade 70% (0-100)
        val outputStream = java.io.ByteArrayOutputStream()
        imagemRedimensionada.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
        val bytes = outputStream.toByteArray()

        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
    }

    private fun redimensionarBitmap(bitmap: android.graphics.Bitmap, tamanhoMaximo: Int): android.graphics.Bitmap {
        val largura = bitmap.width
        val altura = bitmap.height

        if (largura <= tamanhoMaximo && altura <= tamanhoMaximo) return bitmap

        // Calcula a proporção para não distorcer a imagem
        val proporcao = if (largura > altura) {
            tamanhoMaximo.toFloat() / largura
        } else {
            tamanhoMaximo.toFloat() / altura
        }

        val novaLargura = (largura * proporcao).toInt()
        val novaAltura = (altura * proporcao).toInt()

        return android.graphics.Bitmap.createScaledBitmap(bitmap, novaLargura, novaAltura, true)
    }

    private fun salvarNoFireStore(titulo: String, capaUrl: String) {
        val livro = hashMapOf(
            "titulo"     to titulo,
            "material"   to editMaterial.text.toString().trim(),
            "idioma"     to editIdioma.text.toString().trim(),
            "publicacao" to editPublicacao.text.toString().trim(),
            "edicao"     to editEdicao.text.toString().trim(),
            "serie"      to editSerie.text.toString().trim(),
            "assuntos"   to editAssuntos.text.toString().trim(),
            "referencia" to editReferencia.text.toString().trim(),
            "autores"    to obterAutores(),
            "capaUrl"    to capaUrl
        )

        db.collection("Livros")
            .add(livro)
            .addOnSuccessListener { _: com.google.firebase.firestore.DocumentReference ->
                Toast.makeText(this, "Livro cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AdminBookpageActivity::class.java))
                finish()
            }
            .addOnFailureListener { e: Exception ->
                Log.e("BookFast", "Erro ao salvar livro", e)
                Toast.makeText(this, "Erro ao cadastrar livro. Tente novamente.", Toast.LENGTH_SHORT).show()
                btnEnviar.isEnabled = true
            }
    }

    private fun voltarParaTelaAnterior() {
        startActivity(Intent(this, HomePageAdmin::class.java))
    }

    private fun alternarDetalhes() {
        detalhesAbertos = !detalhesAbertos
        if (detalhesAbertos) {
            layoutDetalhes.visibility = View.VISIBLE
            arrowExpand.rotation = 270f
        } else {
            layoutDetalhes.visibility = View.GONE
            arrowExpand.rotation = 90f
        }
    }

    private fun adicionarAutor() {
        when {
            autorBox2.visibility != View.VISIBLE -> autorBox2.visibility = View.VISIBLE
            autorBox3.visibility != View.VISIBLE -> autorBox3.visibility = View.VISIBLE
            autorBox4.visibility != View.VISIBLE -> autorBox4.visibility = View.VISIBLE
            else -> Toast.makeText(this, "Você pode adicionar no máximo 4 autores", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removerAutor(autorBox: View, editAutor: TextInputEditText) {
        editAutor.setText("")
        autorBox.visibility = View.GONE
    }

    private fun obterAutores(): List<String> {
        return listOf(
            editAutor1.text.toString().trim(),
            editAutor2.text.toString().trim(),
            editAutor3.text.toString().trim(),
            editAutor4.text.toString().trim()
        ).filter { it.isNotEmpty() }
    }
}