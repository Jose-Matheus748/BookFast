package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    lateinit var tvBoasvindas: TextView
    lateinit var etEmail: EditText
    lateinit var etSenha: EditText
    lateinit var etConfirmarSenha: EditText
    lateinit var btnCriarConta: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_acess)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val name = intent.getStringExtra("name") ?: "Usuário"

        tvBoasvindas = findViewById(R.id.titulo2)
        etEmail = findViewById(R.id.editTextTextEmailAddress3)
        etSenha = findViewById(R.id.editTextTextPassword2)
        etConfirmarSenha = findViewById(R.id.editTextTextPassword4)
        btnCriarConta = findViewById(R.id.btnNext4)

        tvBoasvindas.text = "Boa, $name"

        btnCriarConta.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()
            val confirmarSenha = etConfirmarSenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else if (senha != confirmarSenha) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            } else {
                btnCriarConta.isEnabled = false
                criarUsuario(name, email, senha)
            }
        }
    }

    private fun criarUsuario(nome: String, email: String, senha: String) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser!!.uid
                    val usuario = hashMapOf(
                        "nome" to nome,
                        "email" to email,
                        "tipo" to "user"
                    )
                    db.collection("Usuarios")
                        .document(uid)
                        .set(usuario)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, HomePageActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            btnCriarConta.isEnabled = true
                            Log.e("FirestoreError", "Erro ao salvar dados do usuário", e)
                            Toast.makeText(this, "Erro ao salvar no banco de dados.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    btnCriarConta.isEnabled = true
                    Log.e("AuthError", "Erro ao criar autenticação", task.exception)
                    Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}