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
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

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

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _isCommenting = MutableStateFlow(false)
    val isCommenting: StateFlow<Boolean> = _isCommenting.asStateFlow()


    fun onCommentTextChanged(newText: String) {
        _commentText.value = newText
    }

    val isLikedByCurrentUser: StateFlow<Boolean> =
        combine(postDetail, userPreferences.getUserId()) { post, currentUserId ->
            if (post == null || currentUserId == null) {
                false
            } else {
                post.likes.any { it.authorId == currentUserId }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentUserId: StateFlow<String?> = userPreferences.getUserId().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val currentUsername: StateFlow<String?> = userPreferences.getUsername().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    var title by mutableStateOf("")
    var content by mutableStateOf("")

    private var currentPage = 1
    private var totalPages = 1

    init {
        viewModelScope.launch {
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
        val postTitle = title
        val postContent = content

        if (postTitle.isBlank() || postContent.isBlank()) return

        val optimisticPostId = "temp_${System.currentTimeMillis()}"

        val optimisticPost = PostEntity(
            id = optimisticPostId,
            title = postTitle,
            content = postContent,
            createdAt = "...",
            authorId = currentUserId.value ?: "unknown_user_id",
            authorUsername = currentUsername.value ?: "Unknown User",
            authorFirstName = "Current",
            authorLastName = "User",
            likesCount = 0,
            commentsCount = 0,
            isLiked = false
        )

        _postListState.value = listOf(optimisticPost) + _postListState.value

        title = ""
        content = ""

        viewModelScope.launch {
            _error.value = null
            try {
                postRepository.createPost(postTitle, postContent)
                refreshPosts()
            } catch (e: Exception) {
                _error.value = "Gagal memposting: ${e.message}"
                _postListState.value = _postListState.value.filter { it.id != optimisticPostId }
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

    fun toggleLikeOnDetail(postId: String) {
        viewModelScope.launch {
            val originalPost = _postDetail.value ?: return@launch
            val isCurrentlyLiked = isLikedByCurrentUser.value

            try {
                if (isCurrentlyLiked) {
                    postRepository.unlikePost(postId)
                } else {
                    postRepository.likePost(postId)
                }

                val currentUserId = userPreferences.getUserId().first()
                if (currentUserId == null) {
                    _error.value = "Tidak bisa memproses like, user ID tidak ditemukan."
                    return@launch
                }

                val mutableLikes = originalPost.likes.toMutableList()

                if (isCurrentlyLiked) {
                    val indexToRemove = mutableLikes.indexOfFirst { it.authorId == currentUserId }
                    if (indexToRemove != -1) {
                        mutableLikes.removeAt(indexToRemove)
                    }
                } else {
                    mutableLikes.add(
                        com.example.bunnypost.data.remote.model.Like(
                            id = "temp-like-${System.currentTimeMillis()}",
                            authorId = currentUserId
                        )
                    )
                }

                _postDetail.value = originalPost.copy(likes = mutableLikes)

            } catch (e: Exception) {
                _error.value = "Gagal memperbarui status like: ${e.message}"
            }
        }
    }

    fun toggleLikeOnList(postId: String) {
        viewModelScope.launch {
            val postToUpdate = _postListState.value.find { it.id == postId } ?: return@launch
            val isCurrentlyLiked = postToUpdate.isLiked

            try {
                if (isCurrentlyLiked) {
                    postRepository.unlikePost(postId)
                } else {
                    postRepository.likePost(postId)
                }

                val updatedEntity = postToUpdate.copy(
                    isLiked = !isCurrentlyLiked,
                    likesCount = if (isCurrentlyLiked) postToUpdate.likesCount - 1 else postToUpdate.likesCount + 1
                )

                postRepository.updatePostInDb(updatedEntity)

            } catch (e: Exception) {
                _error.value = "Gagal memperbarui like: ${e.message}"
            }
        }
    }

    fun addComment(postId: String) {
        val commentContent = _commentText.value.trim()
        if (commentContent.isBlank()) return

        viewModelScope.launch {
            _isCommenting.value = true
            _error.value = null
            try {
                val result = postRepository.addComment(postId, commentContent)

                result.onSuccess {
                    _commentText.value = ""
                    getPostDetail(postId)
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Terjadi kesalahan"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isCommenting.value = false
            }
        }
    }
}