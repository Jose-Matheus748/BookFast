package com.example.myapplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // LOGIN
    suspend fun login(email: String, senha: String): Result<FirebaseUser> {
        return try {
            val resultado = auth.signInWithEmailAndPassword(email, senha).await()
            Result.success(resultado.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // CADASTRO + salva dados no Firestore
    suspend fun cadastrar(nome: String, email: String, senha: String): Result<Unit> {
        return try {
            val resultado = auth.createUserWithEmailAndPassword(email, senha).await()
            val uid = resultado.user!!.uid

            val novoUsuario = User(uid = uid, nome = nome, email = email)

            db.collection("usuarios")
                .document(uid)
                .set(novoUsuario)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // LOGOUT
    fun logout() = auth.signOut()

    // Verifica se já está logado
    fun usuarioAtual(): FirebaseUser? = auth.currentUser
}