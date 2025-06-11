package com.example.bunnypost.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bunnypost.data.helper.Result
import com.example.bunnypost.data.local.entity.UserEntity
import com.example.bunnypost.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<Result<UserEntity>?>(null)
    val profileState: StateFlow<Result<UserEntity>?> = _profileState.asStateFlow()

    init {
        fetchMyProfile()
    }

    fun fetchMyProfile() {
        viewModelScope.launch {
            authRepository.getMyProfile().collect { result ->
                _profileState.value = result
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            // You might want to navigate back to login screen after logout
        }
    }
}