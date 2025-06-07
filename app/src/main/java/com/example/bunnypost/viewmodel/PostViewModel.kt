package com.example.bunnypost.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bunnypost.data.local.entity.PostEntity
import com.example.bunnypost.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    val posts: StateFlow<List<PostEntity>> = postRepository.getPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isPaginating = MutableStateFlow(false)
    val isPaginating: StateFlow<Boolean> = _isPaginating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentPage = 1
    private var totalPages = 1

    init {
        refreshPosts()
    }

    fun refreshPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                currentPage = 1
                val response = postRepository.fetchPosts(page = currentPage)
                totalPages = response.pagination.totalPages
                if (currentPage < totalPages) {
                    currentPage++
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMorePosts() {
        if (_isLoading.value || _isPaginating.value || currentPage > totalPages) return

        viewModelScope.launch {
            _isPaginating.value = true
            try {
                val response = postRepository.fetchPosts(page = currentPage)
                totalPages = response.pagination.totalPages
                if (currentPage < totalPages) {
                    currentPage++
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isPaginating.value = false
            }
        }
    }
}