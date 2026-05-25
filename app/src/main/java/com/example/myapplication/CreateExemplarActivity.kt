package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class CreateExemplarActivity : AppCompatActivity() {

    private lateinit var inputRegistro: TextInputLayout
    private lateinit var inputEdicao: TextInputLayout
    private lateinit var inputAno: TextInputLayout
    private lateinit var inputSuporte: TextInputLayout
    private lateinit var inputLocalizacao: TextInputLayout
    private lateinit var inputSituacao: TextInputLayout

    private lateinit var arrowBack: ImageView
    private lateinit var btnSalvar: Button

    private val db = Firebase.firestore
    private var livroId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createexemplar)

        HeaderAdminNavigation.setup(this)
        FooterAdminNavigation.setup(this)

        livroId = intent.getStringExtra("LIVRO_ID") ?: ""
        if (livroId.isEmpty()) {
            Toast.makeText(this, "Livro não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        vincularViews()
        configurarBotoes()
    }

    private fun vincularViews() {
        arrowBack        = findViewById(R.id.arrowBackId)
        inputRegistro    = findViewById(R.id.inputRegistro)
        inputEdicao      = findViewById(R.id.inputEdicao)
        inputAno         = findViewById(R.id.inputAno)
        inputSuporte     = findViewById(R.id.inputSuporte)
        inputLocalizacao = findViewById(R.id.inputLocalizacao)
        inputSituacao    = findViewById(R.id.inputSituacao)
        btnSalvar        = findViewById(R.id.btnSalvar)
    }

    private fun configurarBotoes() {
        arrowBack.setOnClickListener { finish() }
        btnSalvar.setOnClickListener { salvarExemplar() }
    }

    private fun salvarExemplar() {
        val registro    = inputRegistro.editText?.text.toString().trim()
        val edicao      = inputEdicao.editText?.text.toString().trim()
        val ano         = inputAno.editText?.text.toString().trim()
        val suporte     = inputSuporte.editText?.text.toString().trim()
        val localizacao = inputLocalizacao.editText?.text.toString().trim()
        val situacao    = inputSituacao.editText?.text.toString().trim()

        if (registro.isEmpty()) {
            Toast.makeText(this, "Informe o número de registro", Toast.LENGTH_SHORT).show()
            return
        }
        if (suporte.isEmpty()) {
            Toast.makeText(this, "Informe o suporte", Toast.LENGTH_SHORT).show()
            return
        }
        if (situacao.isEmpty()) {
            Toast.makeText(this, "Informe a situação", Toast.LENGTH_SHORT).show()
            return
        }

        btnSalvar.isEnabled = false

        val exemplar = hashMapOf<String, Any>(
            "registro"    to registro,
            "edicao"      to edicao,
            "ano"         to ano,
            "suporte"     to suporte,
            "localizacao" to localizacao,
            "situacao"    to situacao,
            "livroId"     to livroId
        )

        // Exemplares ficam numa subcoleção "Exemplares" dentro do documento do livro
        db.collection("Livros")
            .document(livroId)
            .collection("Exemplares")
            .add(exemplar)
            .addOnSuccessListener {
                Toast.makeText(this, "Exemplar criado com sucesso!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AdminBookpageActivity::class.java)
                intent.putExtra("LIVRO_ID", livroId)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao criar exemplar: ${e.message}", Toast.LENGTH_SHORT).show()
                btnSalvar.isEnabled = true
            }
    }
}