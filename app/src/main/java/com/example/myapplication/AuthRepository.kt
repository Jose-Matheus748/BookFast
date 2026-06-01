package com.example.myapplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
    class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // LOGIN
    suspend fun login(email: String, senha: String): Result<User> {
        return try {
            val resultado = auth.signInWithEmailAndPassword(email, senha).await()
            val uid = resultado.user!!.uid

            // Busca o perfil do usuário no Firestore
            val documento = db.collection("Usuarios").document(uid).get().await()
            val user = documento.toObject(User::class.java) ?: User()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun recuperarSenha(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


        suspend fun cadastrar(nome: String, email: String, senha: String, perfil: String = "usuario"): Result<Unit> {
            return try {
                val resultado = auth.createUserWithEmailAndPassword(email, senha).await()
                val uid = resultado.user!!.uid

                val novoUsuario = hashMapOf(
                    "uid"   to uid,
                    "nome"  to nome,
                    "email" to email,
                    "tipo"  to perfil   // ← campo agora é "tipo", igual ao que você lê
                )

                db.collection("Usuarios")   // ← maiúsculo, igual ao resto do app
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

    fun usuarioAtual() = auth.currentUser
}