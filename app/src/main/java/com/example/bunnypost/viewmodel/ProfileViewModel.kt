//app/src/main/java/com/example/bunnypost/viewmodel/ProfileViewModel.kt

package com.example.bunnypost.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bunnypost.data.helper.Result
import com.example.bunnypost.data.remote.model.UserData
import com.example.bunnypost.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<Result<UserData>>(Result.Loading)
    val profileState: StateFlow<Result<UserData>> = _profileState

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _profileState.value = Result.Loading
            try {
                val profile = userRepository.getProfile()
                _profileState.value = Result.Success(profile)
            } catch (e: Exception) {
                _profileState.value = Result.Error(e.message ?: "An unknown error occurred")
            }
}
    }
}