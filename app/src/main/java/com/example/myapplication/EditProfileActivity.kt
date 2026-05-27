package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    // ── Views ──
    private lateinit var btnSalvar: Button
    private lateinit var btnVoltar: Button
    private lateinit var imgPencil: ImageView
    private lateinit var imgAvatar: ImageView
    private lateinit var editNome: EditText

    // ── Firebase ──
    private val auth    by lazy { FirebaseAuth.getInstance() }
    private val db      by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    // ── Estado ──
    private var novaFotoUri: Uri? = null
    private val favoritosVisiveis = mutableSetOf(1, 2, 3, 4)

    // ── Picker de imagem da galeria ──
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                novaFotoUri = it
                // Mostra a prévia imediatamente usando Glide
                Glide.with(this)
                    .load(it)
                    .circleCrop()
                    .into(imgAvatar)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        bindViews()
        preencherNomeAtual()
        configurarRemocaoFavoritos()
        configurarBotoes()

        carregarTipoEConfigurar() // ← substitui os dois setups diretos
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


    // ── Bind ──
    private fun bindViews() {
        btnSalvar  = findViewById(R.id.btnNext)
        btnVoltar  = findViewById(R.id.btnVoltar)
        imgPencil  = findViewById(R.id.imgPencil)
        editNome   = findViewById(R.id.editTextTextEmailAddress4)
        imgAvatar = findViewById(R.id.imgAvatar)
    }

    // ── Preenche o nome atual do usuário no campo ──
    private fun preencherNomeAtual() {
        val user = auth.currentUser ?: return
        val uid  = user.uid

        // Tenta primeiro o Firestore; fallback para displayName do Auth
        db.collection("Usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val nome = doc.getString("nome")
                    ?: user.displayName
                    ?: ""
                editNome.setText(nome)
            }
            .addOnFailureListener {
                editNome.setText(user.displayName ?: "")
            }

        // Carrega foto atual (se houver) com Glide
        user.photoUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .circleCrop()
                .into(imgAvatar)
        }
    }

    // ── Botões principais ──
    private fun configurarBotoes() {

        // Editar foto — abre a galeria
        imgPencil.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Salvar alterações
        btnSalvar.setOnClickListener {
            salvarAlteracoes()
        }

        // Voltar
        btnVoltar.setOnClickListener {
            finish() // volta para a activity anterior sem recriar a pilha
        }
    }

    // ── Lógica de salvar ──
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

        if (novaFotoUri != null) {
            // 1) Upload da foto → depois salva nome
            uploadFotoESalvar(user.uid, novoNome, novaFotoUri!!)
        } else {
            // 2) Só atualiza o nome
            atualizarNome(user.uid, novoNome, fotoUrl = null)
        }
    }

    // ── Upload da foto para o Firebase Storage ──
    private fun uploadFotoESalvar(uid: String, nome: String, uri: Uri) {
        val ref = storage.reference.child("avatars/$uid.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    atualizarNome(uid, nome, downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                resetarBotaoSalvar()
                Toast.makeText(this, "Erro ao enviar foto: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ── Atualiza nome no Auth + Firestore ──
    private fun atualizarNome(uid: String, nome: String, fotoUrl: String?) {
        val user = auth.currentUser ?: return

        // Monta a atualização do FirebaseAuth
        val profileBuilder = UserProfileChangeRequest.Builder().setDisplayName(nome)
        fotoUrl?.let { profileBuilder.setPhotoUri(Uri.parse(it)) }

        user.updateProfile(profileBuilder.build())
            .addOnSuccessListener {
                // Monta o mapa para o Firestore
                val dados = mutableMapOf<String, Any>("nome" to nome)
                fotoUrl?.let { dados["photoUrl"] = it }

                db.collection("Usuarios").document(uid)
                    .update(dados as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Alterações salvas!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, PaginaPerfilActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        // Auth foi salvo; avisa sobre o Firestore mas não bloqueia
                        Toast.makeText(
                            this,
                            "Perfil salvo, mas erro no banco: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(Intent(this, PaginaPerfilActivity::class.java))
                        finish()
                    }
            }
            .addOnFailureListener { e ->
                resetarBotaoSalvar()
                Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ── Remoção de favoritos (com animação) ──
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
                item.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .withEndAction {
                        item.visibility = View.GONE
                        favoritosVisiveis.remove(numero)
                        salvarRemocaoFavoritoNoFirestore(numero)
                        if (favoritosVisiveis.isEmpty()) {
                            Toast.makeText(this, "Nenhum favorito restante.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .start()
            }
        }
    }

    // ── Persiste a remoção no Firestore ──
    private fun salvarRemocaoFavoritoNoFirestore(numeroFav: Int) {
        val uid = auth.currentUser?.uid ?: return
        
        // Adapte o caminho conforme o seu modelo de dados
        db.collection("Usuarios").document(uid)
            .collection("Favoritos").document("fav$numeroFav")
            .delete()
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Livro removido da tela, mas erro ao sincronizar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // ── Helper ──
    private fun resetarBotaoSalvar() {
        btnSalvar.isEnabled = true
        btnSalvar.text      = getString(R.string.salvar_altera_es)
    }
}
