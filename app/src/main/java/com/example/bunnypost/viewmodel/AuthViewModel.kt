package com.example.bunnypost.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bunnypost.data.repository.AuthRepository
import com.example.bunnypost.di.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.bunnypost.data.helper.Result

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    val sessionManager: SessionManager
) : ViewModel() {



    private val _loginState = MutableStateFlow<Result<String>?>(null)
    val loginState: StateFlow<Result<String>?> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow<Result<String>?>(null)
    val signUpState: StateFlow<Result<String>?> = _signUpState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = authRepository.isLoggedIn()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            authRepository.login(email, password).collect { result ->
                _loginState.value = result
                if (result is Result.Success) {
                    _isLoggedIn.value = true
                }
            }
        }
    }

    fun signup(email: String, password: String, username: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            authRepository.signup(email, password, username, firstName, lastName).collect { result ->
                _signUpState.value = result
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
            _signUpState.value = null
            onLogoutFinished()
        }
    }
}