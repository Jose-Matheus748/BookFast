package com.example.myapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch


class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()
    val loginResult = MutableLiveData<Result<User>>()
    val cadastroResult = MutableLiveData<Result<Unit>>()

    fun login(email: String, senha: String) {
        viewModelScope.launch {
            loginResult.postValue(repository.login(email, senha))
        }
    }

    val recuperacaoResult = MutableLiveData<Result<Unit>>()

    fun recuperarSenha(email: String) {
        viewModelScope.launch {
            recuperacaoResult.postValue(repository.recuperarSenha(email))
        }
    }

    fun cadastrar(nome: String, email: String, senha: String, perfil: String = "usuario") {
        viewModelScope.launch {
            cadastroResult.postValue(repository.cadastrar(nome, email, senha, perfil))
        }
    }

    fun logout() = repository.logout()

    fun usuarioJaLogado() = repository.usuarioAtual() != null
}