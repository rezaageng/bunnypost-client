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
import com.example.bunnypost.data.local.entity.UserEntity

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Result<String>?>(null)
    val loginState: StateFlow<Result<String>?> = _loginState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    private val _signUpState = MutableStateFlow<Result<String>?>(null)
    val signUpState: StateFlow<Result<String>?> = _signUpState.asStateFlow()

    private val _editProfileState = MutableStateFlow<Result<UserEntity>?>(null)
    val editProfileState: StateFlow<Result<UserEntity>?> = _editProfileState.asStateFlow()

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
            _signUpState.value = null
            _editProfileState.value = null
            onLogoutFinished()
        }
    }

    fun signup(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String
    ) {
        Log.d("AuthViewModel", "Signup called for email: $email, username: $username")
        viewModelScope.launch {
            _signUpState.value = Result.Loading
            authRepository.signup(email, password, username, firstName, lastName).collect { result ->
                _signUpState.value = result
            }
        }
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        username: String,
        bio: String?
    ) {
        Log.d("AuthViewModel", "Update profile called for username: $username")
        viewModelScope.launch {
            _editProfileState.value = Result.Loading
            authRepository.updateProfile(firstName, lastName, username, bio).collect { result ->
                _editProfileState.value = result
            }
        }
    }

    fun resetEditProfileState() {
        _editProfileState.value = null
    }
}