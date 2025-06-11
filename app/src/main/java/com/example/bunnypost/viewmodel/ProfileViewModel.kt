package com.example.bunnypost.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bunnypost.data.helper.Result
import com.example.bunnypost.data.local.entity.PostEntity
import com.example.bunnypost.data.local.entity.UserEntity
import com.example.bunnypost.data.repository.AuthRepository
import com.example.bunnypost.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<Result<UserEntity>?>(null)
    val profileState: StateFlow<Result<UserEntity>?> = _profileState.asStateFlow()

    private val _userPostsState = MutableStateFlow<Result<List<PostEntity>>>(Result.Loading)
    val userPostsState: StateFlow<Result<List<PostEntity>>> = _userPostsState.asStateFlow()

    private val _likedPostsState = MutableStateFlow<Result<List<PostEntity>>>(Result.Loading)
    val likedPostsState: StateFlow<Result<List<PostEntity>>> = _likedPostsState.asStateFlow()


    init {
        fetchMyProfile()
    }

    /**
     * Memperbarui profileState secara manual dengan data baru dari operasi lain,
     * seperti setelah berhasil mengedit profil, untuk menghindari fetch ulang.
     */
    fun updateProfileStateWithNewData(updatedUser: UserEntity) {
        _profileState.value = Result.Success(updatedUser)
    }

    fun fetchMyProfile() {
        viewModelScope.launch {
            authRepository.getMyProfile().collect { result ->
                _profileState.value = result
                if (result is Result.Success) {
                    fetchUserPosts(result.data.id)
                    fetchLikedPosts(result.data.id)
                }
            }
        }
    }

    private fun fetchUserPosts(userId: String) {
        viewModelScope.launch {
            _userPostsState.value = Result.Loading
            try {
                val posts = postRepository.getPostsByAuthorId(userId)
                _userPostsState.value = Result.Success(posts)
            } catch (e: Exception) {
                _userPostsState.value = Result.Error(e.message ?: "Failed to load posts")
            }
        }
    }

    private fun fetchLikedPosts(userId: String) {
        viewModelScope.launch {
            _likedPostsState.value = Result.Loading
            try {
                val posts = postRepository.getLikedPostsByUser(userId)
                _likedPostsState.value = Result.Success(posts)
            } catch (e: Exception) {
                _likedPostsState.value = Result.Error(e.message ?: "Failed to load liked posts")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}