package com.example.bunnypost.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bunnypost.data.local.entity.PostEntity
import com.example.bunnypost.data.local.entity.UserEntity
import com.example.bunnypost.data.repository.PostRepository
import com.example.bunnypost.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchResultsUsers: StateFlow<List<UserEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            flow {
                if (query.isBlank()) {
                    emit(emptyList())
                } else {
                    try {
                        emit(userRepository.searchUsers(query)) // Menangkap pengecualian di sini
                    } catch (e: Exception) {
                        // Mengeluarkan daftar kosong jika terjadi kesalahan untuk mencegah crash
                        emit(emptyList())
                        // Anda bisa menambahkan logika logging atau menampilkan pesan error di UI di sini jika diperlukan
                        // e.g., _errorState.value = "Error fetching users: ${e.message}"
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val searchResultsPosts: StateFlow<List<PostEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                postRepository.searchPosts(query)
                    .catch { e -> // Menggunakan operator .catch untuk menangani pengecualian dari Flow
                        // Mengeluarkan daftar kosong jika terjadi kesalahan untuk mencegah crash
                        emit(emptyList())
                        // Anda bisa menambahkan logika logging atau menampilkan pesan error di UI di sini jika diperlukan
                        // e.g., _errorState.value = "Error fetching posts: ${e.message}"
                    }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}