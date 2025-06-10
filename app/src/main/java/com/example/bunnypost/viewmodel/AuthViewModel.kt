package com.example.bunnypost.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bunnypost.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.bunnypost.data.helper.Result

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Result<String>?>(null)
    val loginState: StateFlow<Result<String>?> = _loginState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    // Tambahkan StateFlow untuk status pendaftaran
    private val _signUpState = MutableStateFlow<Result<String>?>(null)
    val signUpState: StateFlow<Result<String>?> = _signUpState.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = authRepository.isLoggedIn()
        }
    }

    fun login(email: String, password: String) {
        Log.d("AuthViewModel", "Login called with email: $email, password: $password")
        viewModelScope.launch {
            authRepository.login(email, password).collect { result ->
                _loginState.value = result
                if (result is Result.Success) {
                    _isLoggedIn.value = true
                }
            }
        }
    }

    fun logout(onLogoutFinished: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _isLoggedIn.value = false
            _loginState.value = null
            _signUpState.value = null // Clear sign up state on logout
            onLogoutFinished()
        }
    }

    // Fungsi signup yang diperbaiki
    fun signup(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String
    ) {
        Log.d("AuthViewModel", "Signup called for email: $email, username: $username")
        viewModelScope.launch {
            // Atur status loading
            _signUpState.value = Result.Loading
            // Kumpulkan (collect) hasil dari flow authRepository.signup
            authRepository.signup(email, password, username, firstName, lastName).collect { result ->
                _signUpState.value = result // Tetapkan hasil yang dikumpulkan
                if (result is Result.Success) { // Sekarang ini adalah Result.Success<String>
                    // Opsional: lakukan sesuatu setelah signup berhasil, misalnya otomatis login
                    // atau clear form.
                }
            }
        }
    }
}