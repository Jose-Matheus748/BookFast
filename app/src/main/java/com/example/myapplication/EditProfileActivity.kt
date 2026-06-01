package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var btnSalvar: Button
    private lateinit var btnVoltar: Button
    private lateinit var imgPencil: ImageView
    private lateinit var imgAvatar: ImageView
    private lateinit var editNome: EditText

    private lateinit var containerFavoritosEdit: LinearLayout

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db   by lazy { FirebaseFirestore.getInstance() }

    private var novaFotoUri: Uri? = null
    private val favoritosVisiveis = mutableSetOf(1, 2, 3, 4)

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                novaFotoUri = it
                Glide.with(this).load(it).circleCrop().into(imgAvatar)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        bindViews()
        preencherDadosAtuais()
        carregarFavoritosEdit()
        configurarBotoes()
        carregarTipoEConfigurar()
    }

    private fun carregarTipoEConfigurar() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("Usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val tipo = doc.getString("tipo") ?: "usuario"
                if (tipo == "admin") {
                    HeaderAdminNavigation.setup(this)
                    FooterAdminNavigation.setup(this)
                } else {
                    HeaderNavigation.setup(this)
                    FooterNavigation.setup(this)
                }
            }
            .addOnFailureListener {
                HeaderNavigation.setup(this)
                FooterNavigation.setup(this)
            }
    }

    private fun bindViews() {
        btnSalvar = findViewById(R.id.btnNext)
        btnVoltar = findViewById(R.id.btnVoltar)
        imgPencil = findViewById(R.id.imgPencil)
        editNome  = findViewById(R.id.editTextTextEmailAddress4)
        imgAvatar = findViewById(R.id.imgAvatar)
        containerFavoritosEdit = findViewById(R.id.containerFavoritosEdit)
    }

    private fun preencherDadosAtuais() {
        val user = auth.currentUser ?: return
        val uid  = user.uid

        db.collection("Usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                editNome.setText(doc.getString("nome") ?: user.displayName ?: "")

                val fotoBase64 = doc.getString("fotoBase64")
                if (!fotoBase64.isNullOrEmpty()) {
                    try {
                        val bytes  = Base64.decode(fotoBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        Glide.with(this).load(bitmap).circleCrop().into(imgAvatar)
                    } catch (e: Exception) { /* mantém placeholder */ }
                }
            }
            .addOnFailureListener {
                editNome.setText(user.displayName ?: "")
            }
    }

    private fun configurarBotoes() {
        imgPencil.setOnClickListener { pickImageLauncher.launch("image/*") }
        btnSalvar.setOnClickListener { salvarAlteracoes() }
        btnVoltar.setOnClickListener { finish() }
    }

    private fun salvarAlteracoes() {
        val user = auth.currentUser ?: run {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        val novoNome = editNome.text.toString().trim()
        if (novoNome.isEmpty()) {
            editNome.error = "O nome não pode estar vazio"
            editNome.requestFocus()
            return
        }

        btnSalvar.isEnabled = false
        btnSalvar.text      = "Salvando…"

        val fotoBase64 = novaFotoUri?.let { converterParaBase64(it) }
        atualizarPerfil(user.uid, novoNome, fotoBase64)
    }

    private fun atualizarPerfil(uid: String, nome: String, fotoBase64: String?) {
        val user = auth.currentUser ?: return

        // Atualiza displayName no FirebaseAuth
        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(nome)
            .build()

        user.updateProfile(profileUpdate)
            .addOnSuccessListener {
                val dados = mutableMapOf<String, Any>("nome" to nome)
                fotoBase64?.let { dados["fotoBase64"] = it }

                db.collection("Usuarios").document(uid)
                    .update(dados as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Alterações salvas!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, PaginaPerfilActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Perfil salvo, mas erro no banco: ${e.message}", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, PaginaPerfilActivity::class.java))
                        finish()
                    }
            }
            .addOnFailureListener { e ->
                resetarBotaoSalvar()
                Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Converte URI da galeria para base64 (igual ao EditBookActivity)
    private fun converterParaBase64(uri: Uri): String {
        val inputStream    = contentResolver.openInputStream(uri)
        val imagemOriginal = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        val imagemRedimensionada = redimensionarBitmap(imagemOriginal, 300) // menor que livro pois é avatar
        val outputStream = java.io.ByteArrayOutputStream()
        imagemRedimensionada.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun redimensionarBitmap(bitmap: Bitmap, max: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= max && h <= max) return bitmap
        val scale = if (w > h) max.toFloat() / w else max.toFloat() / h
        return Bitmap.createScaledBitmap(bitmap, (w * scale).toInt(), (h * scale).toInt(), true)
    }

    private fun resetarBotaoSalvar() {
        btnSalvar.isEnabled = true
        btnSalvar.text      = getString(R.string.salvar_altera_es)
    }

    private fun carregarFavoritosEdit() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("Usuarios").document(uid)
            .collection("Favoritos")
            .get()
            .addOnSuccessListener { result ->
                containerFavoritosEdit.removeAllViews()

                if (result.isEmpty) {
                    val tv = TextView(this).apply {
                        text = "Nenhum favorito ainda."
                        textSize = 14f
                        setTextColor(0xFFAAAAAA.toInt())
                    }
                    containerFavoritosEdit.addView(tv)
                    return@addOnSuccessListener
                }

                val livros = result.documents
                for (i in livros.indices step 2) {
                    val row = android.widget.LinearLayout(this).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 0, 0, dpToPxEdit(12)) }
                        weightSum = 2f
                    }

                    val card1 = criarCardFavoritoEdit(
                        livroId    = livros[i].getString("livroId") ?: "",
                        titulo     = livros[i].getString("titulo") ?: "",
                        autor      = livros[i].getString("autor") ?: "",
                        capaBase64 = livros[i].getString("capaBase64") ?: ""
                    )
                    row.addView(card1)

                    if (i + 1 < livros.size) {
                        val card2 = criarCardFavoritoEdit(
                            livroId    = livros[i + 1].getString("livroId") ?: "",
                            titulo     = livros[i + 1].getString("titulo") ?: "",
                            autor      = livros[i + 1].getString("autor") ?: "",
                            capaBase64 = livros[i + 1].getString("capaBase64") ?: ""
                        )
                        row.addView(card2)
                    } else {
                        val espacador = android.view.View(this).apply {
                            layoutParams = android.widget.LinearLayout.LayoutParams(
                                0, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1f
                            )
                        }
                        row.addView(espacador)
                    }

                    containerFavoritosEdit.addView(row)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar favoritos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun criarCardFavoritoEdit(
        livroId: String,
        titulo: String,
        autor: String,
        capaBase64: String
    ): android.widget.LinearLayout {
        val card = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply { setMargins(dpToPxEdit(4), 0, dpToPxEdit(4), 0) }
            setBackgroundColor(0xFF2E2E2E.toInt())
            setPadding(dpToPxEdit(8), dpToPxEdit(8), dpToPxEdit(8), dpToPxEdit(10))
        }

        val imgCapa = ImageView(this).apply {
            val widthPx = (resources.displayMetrics.widthPixels / 2) - dpToPxEdit(56)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                (widthPx * 1.35f).toInt()
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(0xFF1A1A1A.toInt())
        }

        if (capaBase64.isNotEmpty()) {
            try {
                val bytes  = Base64.decode(capaBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imgCapa.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imgCapa.setImageResource(R.drawable.bg_book_cover_placeholder)
            }
        } else {
            imgCapa.setImageResource(R.drawable.bg_book_cover_placeholder)
        }

        val tvTitulo = android.widget.TextView(this).apply {
            text = titulo
            textSize = 12f
            setTextColor(0xFFF0F0F0.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dpToPxEdit(6), 0, 0) }
        }

        val tvAutor = android.widget.TextView(this).apply {
            text = autor
            textSize = 10f
            setTextColor(0xFF888888.toInt())
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dpToPxEdit(2), 0, 0) }
        }

        val btnRemover = Button(this).apply {
            text = "Remover"
            textSize = 12f
            setTextColor(0xFFFF6B6B.toInt())
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFF3A1C1C.toInt())
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPxEdit(34)
            ).apply { setMargins(0, dpToPxEdit(8), 0, 0) }
        }

        btnRemover.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("Usuarios").document(uid)
                .collection("Favoritos").document(livroId)
                .delete()
                .addOnSuccessListener {
                    card.animate()
                        .alpha(0f).scaleX(0.8f).scaleY(0.8f)
                        .setDuration(200)
                        .withEndAction { carregarFavoritosEdit() }
                        .start()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao remover favorito", Toast.LENGTH_SHORT).show()
                }
        }

        card.addView(imgCapa)
        card.addView(tvTitulo)
        card.addView(tvAutor)
        card.addView(btnRemover)

        return card
    }

    private fun dpToPxEdit(dp: Int) = (dp * resources.displayMetrics.density).toInt()
}