
package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    lateinit var tvBoasvindas: TextView
    lateinit var etEmail: EditText
    lateinit var etSenha: EditText
    lateinit var etConfirmarSenha: EditText
    lateinit var btnCriarConta: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_acess)

        val name = intent.getStringExtra("name") ?: "Usuario 1"

        tvBoasvindas = findViewById(R.id.titulo2)
        etEmail = findViewById(R.id.editTextTextEmailAddress3)
        etSenha = findViewById(R.id.editTextTextPassword2)
        etConfirmarSenha = findViewById(R.id.editTextTextPassword4)
        btnCriarConta = findViewById(R.id.btnNext4)

        tvBoasvindas.text = "Bem vindo, $name"

        btnCriarConta.setOnClickListener {

            val nome = intent.getStringExtra("name")
                ?.trim()
                ?.ifBlank { "Usuario 1" }
                ?: "Usuario 1"

            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()
            val confirmarSenha = etConfirmarSenha.text.toString().trim()

            val userEmail = "usuario1@bookfast.com"
            val userPassword = "123456"
            val userName = nome

            val adminEmail = "admin@bookfast.com"
            val adminPassword = "admin123"
            val adminName = "Admin"

            if (email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()

            } else if (senha != confirmarSenha) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()

            } else if (email == adminEmail && senha == adminPassword) {
                val intent = Intent(this, HomePageAdmin::class.java)
                intent.putExtra("userName", nome)
                intent.putExtra("userEmail", adminEmail)
                startActivity(intent)
                finish()

            } else if (email == userEmail && senha == userPassword) {
                val intent = Intent(this, HomePageActivity::class.java)
                intent.putExtra("userName", userName)
                intent.putExtra("userEmail", userEmail)
                startActivity(intent)
                finish()

            } else {
                // --- CORREÇÃO DA FALHA AQUI ---
                // Desabilita o botão para evitar cliques duplos enquanto o Firebase responde
                btnCriarConta.isEnabled = false

                // 1. Cria o usuário no Firebase Authentication
                auth.createUserWithEmailAndPassword(email, senha)
                    .addOnSuccessListener { authResult ->
                        val userId = authResult.user?.uid

                        if (userId != null) {
                            // Mapeia os dados usando as variáveis existentes para persistir no banco
                            val userMap = hashMapOf(
                                "nome" to nome,
                                "email" to email
                            )

                            // 2. Salva no Firestore vinculando ao UID gerado
                            db.collection("usuarios")
                                .document(userId)
                                .set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()

                                    // 3. Redireciona para a HomePage apenas após salvar no banco
                                    val intent = Intent(this, HomePageActivity::class.java)
                                    intent.putExtra("userName", nome)
                                    intent.putExtra("userEmail", email)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    btnCriarConta.isEnabled = true
                                    Log.e("FirestoreError", "Erro ao salvar dados do usuário", e)
                                    Toast.makeText(this, "Erro ao salvar no banco de dados.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        btnCriarConta.isEnabled = true
                        Log.e("AuthError", "Erro ao criar autenticação", e)
                        Toast.makeText(this, "Erro ao criar conta: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}