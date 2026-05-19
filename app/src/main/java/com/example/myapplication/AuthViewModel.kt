package com.example.myapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch


class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()
    val loginResult = MutableLiveData<Result<FirebaseUser>>()

    fun login(email: String, senha: String) {
        viewModelScope.launch {
            loginResult.postValue(repository.login(email, senha))
        }
    }
}