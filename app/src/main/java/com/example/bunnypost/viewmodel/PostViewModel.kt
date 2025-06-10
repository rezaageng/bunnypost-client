package com.example.bunnypost.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bunnypost.data.local.UserPreferences
import com.example.bunnypost.data.local.entity.PostEntity
import com.example.bunnypost.data.remote.model.Post
import com.example.bunnypost.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // Versi lokal agar bisa dimodifikasi tanpa sentuh DAO
    private val _postListState = MutableStateFlow<List<PostEntity>>(emptyList())
    val posts: StateFlow<List<PostEntity>> = _postListState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isPaginating = MutableStateFlow(false)
    val isPaginating: StateFlow<Boolean> = _isPaginating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _postDetail = MutableStateFlow<Post?>(null)
    val postDetail: StateFlow<Post?> = _postDetail.asStateFlow()

    val isLikedByCurrentUser: StateFlow<Boolean> =
        combine(postDetail, userPreferences.getUserId()) { post, currentUserId ->
            if (post == null || currentUserId == null) {
                false
            } else {
                post.likes.any { it.authorId == currentUserId }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    var title by mutableStateOf("")
    var content by mutableStateOf("")

    private var currentPage = 1
    private var totalPages = 1

    init {
        viewModelScope.launch {
            // Listen dari database lokal (DAO) ke _postListState
            postRepository.getPosts().collect {
                _postListState.value = it
            }
        }
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
                if (currentPage < totalPages) currentPage++
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
            _error.value = null
            try {
                val response = postRepository.fetchPosts(page = currentPage)
                totalPages = response.pagination.totalPages
                if (currentPage < totalPages) currentPage++
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isPaginating.value = false
            }
        }
    }

    fun submitPost() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                postRepository.createPost(title, content)
                title = ""
                content = ""
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getPostDetail(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _postDetail.value = postRepository.getPostById(postId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun likePostDetail(postId: String) {
        viewModelScope.launch {
            try {
                postRepository.likePost(postId)
                getPostDetail(postId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun likePostFromList(postId: String) {
        viewModelScope.launch {
            try {
                postRepository.likePost(postId)

                // Ambil data post baru dari server
                val updatedPost = postRepository.getPostById(postId)

                // Konversi ke PostEntity
                val updatedEntity = PostEntity(
                    id = updatedPost.id,
                    title = updatedPost.title,
                    content = updatedPost.content,
                    createdAt = updatedPost.createdAt,
                    authorId = updatedPost.author.id,
                    authorUsername = updatedPost.author.username,
                    authorFirstName = updatedPost.author.firstName,
                    authorLastName = updatedPost.author.lastName,
                    likesCount = updatedPost.likes.size,
                    commentsCount = updatedPost.comments.size
                )

                // Update hanya di memori (_postListState)
                _postListState.value = _postListState.value.map {
                    if (it.id == postId) updatedEntity else it
                }

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            try {
                postRepository.addComment(postId, content)
                getPostDetail(postId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
