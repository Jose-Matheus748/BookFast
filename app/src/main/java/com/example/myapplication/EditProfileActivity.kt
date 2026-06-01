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
        configurarRemocaoFavoritos()
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
    }

    private fun preencherDadosAtuais() {
        val user = auth.currentUser ?: return
        val uid  = user.uid

        db.collection("Usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                editNome.setText(doc.getString("nome") ?: user.displayName ?: "")

                // Carrega foto salva em base64
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

    private fun configurarRemocaoFavoritos() {
        val itens = mapOf(
            R.id.btnRemoverFav1 to Pair(R.id.itemFav1, 1),
            R.id.btnRemoverFav2 to Pair(R.id.itemFav2, 2),
            R.id.btnRemoverFav3 to Pair(R.id.itemFav3, 3),
            R.id.btnRemoverFav4 to Pair(R.id.itemFav4, 4)
        )
        itens.forEach { (btnId, par) ->
            val (itemId, numero) = par
            val btn  = findViewById<Button>(btnId)
            val item = findViewById<FrameLayout>(itemId)
            btn.setOnClickListener {
                item.animate().alpha(0f).scaleX(0.8f).scaleY(0.8f).setDuration(200)
                    .withEndAction {
                        item.visibility = View.GONE
                        favoritosVisiveis.remove(numero)
                        salvarRemocaoFavoritoNoFirestore(numero)
                        if (favoritosVisiveis.isEmpty()) {
                            Toast.makeText(this, "Nenhum favorito restante.", Toast.LENGTH_SHORT).show()
                        }
                    }.start()
            }
        }
    }

    private fun salvarRemocaoFavoritoNoFirestore(numeroFav: Int) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("Usuarios").document(uid)
            .collection("Favoritos").document("fav$numeroFav")
            .delete()
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao sincronizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetarBotaoSalvar() {
        btnSalvar.isEnabled = true
        btnSalvar.text      = getString(R.string.salvar_altera_es)
    }
}