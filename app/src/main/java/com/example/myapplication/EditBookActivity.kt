package com.example.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class EditBookActivity : AppCompatActivity() {

    // Campos do formulário
    private lateinit var inputTitulo: TextInputLayout
    private lateinit var inputMaterial: TextInputLayout
    private lateinit var inputIdioma: TextInputLayout
    private lateinit var inputPublicacao: TextInputLayout
    private lateinit var inputEdicao: TextInputLayout
    private lateinit var inputSerie: TextInputLayout
    private lateinit var inputAssuntos: TextInputLayout
    private lateinit var inputReferencia: TextInputLayout

    // Autores (até 4)
    private lateinit var inputAutor1: TextInputLayout
    private lateinit var inputAutor2: TextInputLayout
    private lateinit var inputAutor3: TextInputLayout
    private lateinit var inputAutor4: TextInputLayout
    private lateinit var autorBox2: View
    private lateinit var autorBox3: View
    private lateinit var autorBox4: View

    // Capa e navegação
    private lateinit var ctnDescricao: View
    private lateinit var imgCapaPreview: ImageView
    private lateinit var arrowBack: ImageView
    private lateinit var btnEnviar: Button

    private val db = Firebase.firestore
    private var livroId: String = ""
    private var capaBase64Atual: String = ""
    private var imagemSelecionadaUri: Uri? = null

    private val selecionarImagem = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagemSelecionadaUri = uri
            val base64 = converterImagemParaBase64(uri)
            val bytes  = Base64.decode(base64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imgCapaPreview.setImageBitmap(bitmap)
            imgCapaPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_book)

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)

        vincularViews()
        configurarBotoes()

        livroId = intent.getStringExtra("LIVRO_ID") ?: ""
        if (livroId.isEmpty()) {
            Toast.makeText(this, "Livro não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        carregarDadosAtuais()
    }

    private fun vincularViews() {
        arrowBack       = findViewById(R.id.arrowBackId)
        inputTitulo     = findViewById(R.id.inputTItle)
        inputMaterial   = findViewById(R.id.inputMaterial)
        inputIdioma     = findViewById(R.id.inputIdioma)
        inputPublicacao = findViewById(R.id.inputPublicacao)
        inputEdicao     = findViewById(R.id.inputEdicao)
        inputSerie      = findViewById(R.id.inputSerie)
        inputAssuntos   = findViewById(R.id.inputAssuntos)
        inputReferencia = findViewById(R.id.inputReferencia)
        ctnDescricao    = findViewById(R.id.ctnDescricao)
        imgCapaPreview  = findViewById(R.id.imgCapaPreview)
        btnEnviar       = findViewById(R.id.btnEnviar)

        inputAutor1 = findViewById(R.id.inputAutor1)
        inputAutor2 = findViewById(R.id.inputAutor2)
        inputAutor3 = findViewById(R.id.inputAutor3)
        inputAutor4 = findViewById(R.id.inputAutor4)
        autorBox2   = findViewById(R.id.autorBox2)
        autorBox3   = findViewById(R.id.autorBox3)
        autorBox4   = findViewById(R.id.autorBox4)
    }

    private fun configurarBotoes() {
        arrowBack.setOnClickListener { finish() }

        // Clique na capa abre o seletor de imagem
        ctnDescricao.setOnClickListener {
            selecionarImagem.launch("image/*")
        }

        // Expandir / recolher seção de detalhes
        val arrowExpand    = findViewById<ImageView>(R.id.arrowExpandId)
        val layoutDetalhes = findViewById<View>(R.id.layoutDetalhes)
        arrowExpand.setOnClickListener {
            if (layoutDetalhes.visibility == View.VISIBLE) {
                layoutDetalhes.visibility = View.GONE
                arrowExpand.rotation = 0f
            } else {
                layoutDetalhes.visibility = View.VISIBLE
                arrowExpand.rotation = 90f
            }
        }

        // Adicionar autor (abre o próximo box disponível)
        val imgPlus = findViewById<ImageView>(R.id.imgPlus)
        imgPlus.setOnClickListener {
            when {
                autorBox2.visibility == View.GONE -> autorBox2.visibility = View.VISIBLE
                autorBox3.visibility == View.GONE -> autorBox3.visibility = View.VISIBLE
                autorBox4.visibility == View.GONE -> autorBox4.visibility = View.VISIBLE
                else -> Toast.makeText(this, "Limite de 4 autores atingido", Toast.LENGTH_SHORT).show()
            }
        }

        // Remover autores individuais
        findViewById<TextView>(R.id.btnRemoveAutor2).setOnClickListener {
            autorBox2.visibility = View.GONE
            inputAutor2.editText?.setText("")
        }
        findViewById<TextView>(R.id.btnRemoveAutor3).setOnClickListener {
            autorBox3.visibility = View.GONE
            inputAutor3.editText?.setText("")
        }
        findViewById<TextView>(R.id.btnRemoveAutor4).setOnClickListener {
            autorBox4.visibility = View.GONE
            inputAutor4.editText?.setText("")
        }

        btnEnviar.setOnClickListener { salvarEdicao() }
    }

    private fun carregarDadosAtuais() {
        db.collection("Livros").document(livroId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Livro não encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                // Campos simples
                inputTitulo.editText?.setText(doc.getString("titulo")     ?: "")
                inputMaterial.editText?.setText(doc.getString("material") ?: "")
                inputIdioma.editText?.setText(doc.getString("idioma")     ?: "")
                inputPublicacao.editText?.setText(doc.getString("publicacao") ?: "")
                inputEdicao.editText?.setText(doc.getString("edicao")     ?: "")
                inputSerie.editText?.setText(doc.getString("serie")       ?: "")
                inputAssuntos.editText?.setText(doc.getString("assuntos") ?: "")
                inputReferencia.editText?.setText(doc.getString("referencia") ?: "")

                // Autores — exibe apenas os boxes necessários
                val autores = (doc.get("autores") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()

                inputAutor1.editText?.setText(autores.getOrNull(0) ?: "")

                if (autores.size >= 2) {
                    autorBox2.visibility = View.VISIBLE
                    inputAutor2.editText?.setText(autores[1])
                } else {
                    autorBox2.visibility = View.GONE
                }

                if (autores.size >= 3) {
                    autorBox3.visibility = View.VISIBLE
                    inputAutor3.editText?.setText(autores[2])
                } else {
                    autorBox3.visibility = View.GONE
                }

                if (autores.size >= 4) {
                    autorBox4.visibility = View.VISIBLE
                    inputAutor4.editText?.setText(autores[3])
                } else {
                    autorBox4.visibility = View.GONE
                }

                // Capa
                capaBase64Atual = doc.getString("capaUrl") ?: ""
                if (capaBase64Atual.isNotEmpty()) {
                    try {
                        val bytes  = Base64.decode(capaBase64Atual, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imgCapaPreview.setImageBitmap(bitmap)
                        imgCapaPreview.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        // mantém placeholder caso a base64 esteja corrompida
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun salvarEdicao() {
        val titulo = inputTitulo.editText?.text.toString().trim()
        if (titulo.isEmpty()) {
            Toast.makeText(this, "Por favor, informe o título", Toast.LENGTH_SHORT).show()
            return
        }

        btnEnviar.isEnabled = false

        val capaSalvar = if (imagemSelecionadaUri != null) {
            converterImagemParaBase64(imagemSelecionadaUri!!)
        } else {
            capaBase64Atual
        }

        // Coleta apenas autores de boxes visíveis e com texto
        val autoresSalvos = listOfNotNull(
            inputAutor1.editText?.text.toString().trim().takeIf { it.isNotEmpty() },
            inputAutor2.editText?.text.toString().trim()
                .takeIf { it.isNotEmpty() && autorBox2.visibility == View.VISIBLE },
            inputAutor3.editText?.text.toString().trim()
                .takeIf { it.isNotEmpty() && autorBox3.visibility == View.VISIBLE },
            inputAutor4.editText?.text.toString().trim()
                .takeIf { it.isNotEmpty() && autorBox4.visibility == View.VISIBLE }
        )

        val dadosAtualizados = hashMapOf<String, Any>(
            "titulo"     to titulo,
            "material"   to (inputMaterial.editText?.text.toString().trim()),
            "idioma"     to (inputIdioma.editText?.text.toString().trim()),
            "publicacao" to (inputPublicacao.editText?.text.toString().trim()),
            "edicao"     to (inputEdicao.editText?.text.toString().trim()),
            "serie"      to (inputSerie.editText?.text.toString().trim()),
            "assuntos"   to (inputAssuntos.editText?.text.toString().trim()),
            "referencia" to (inputReferencia.editText?.text.toString().trim()),
            "autores"    to autoresSalvos,
            "capaUrl"    to capaSalvar
        )

        db.collection("Livros").document(livroId)
            .update(dadosAtualizados)
            .addOnSuccessListener {
                Toast.makeText(this, "Livro atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AdminBookpageActivity::class.java)
                intent.putExtra("LIVRO_ID", livroId)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao atualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                btnEnviar.isEnabled = true
            }
    }

    private fun converterImagemParaBase64(uri: Uri): String {
        val inputStream    = contentResolver.openInputStream(uri)
        val imagemOriginal = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        val imagemRedimensionada = redimensionarBitmap(imagemOriginal, 500)
        val outputStream = java.io.ByteArrayOutputStream()
        imagemRedimensionada.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun redimensionarBitmap(
        bitmap: android.graphics.Bitmap,
        max: Int
    ): android.graphics.Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= max && h <= max) return bitmap
        val scale = if (w > h) max.toFloat() / w else max.toFloat() / h
        return android.graphics.Bitmap.createScaledBitmap(
            bitmap, (w * scale).toInt(), (h * scale).toInt(), true
        )
    }
}