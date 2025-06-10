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
        // Simpan title dan content sebelum di-reset
        val postTitle = title
        val postContent = content

        if (postTitle.isBlank() || postContent.isBlank()) return

        // Buat ID sementara untuk post optimistis
        val optimisticPostId = "temp_${System.currentTimeMillis()}"

        // Buat entitas sementara
        // Anda mungkin perlu mengambil data author dari UserPreferences
        val optimisticPost = PostEntity(
            id = optimisticPostId,
            title = postTitle,
            content = postContent,
            createdAt = "...", // Placeholder
            authorId = "current_user_id", // Placeholder, perlu data user
            authorUsername = "current_user", // Placeholder
            authorFirstName = "Current", // Placeholder
            authorLastName = "User", // Placeholder
            likesCount = 0,
            commentsCount = 0,
            isLiked = false
        )

        // Langsung tambahkan ke state list di posisi paling atas
        _postListState.value = listOf(optimisticPost) + _postListState.value

        // Reset field input
        title = ""
        content = ""

        viewModelScope.launch {
            _error.value = null
            try {
                // Panggil repository untuk membuat post di server
                postRepository.createPost(postTitle, postContent)
                // Setelah berhasil, refresh seluruh list untuk mendapatkan data asli
                refreshPosts()
            } catch (e: Exception) {
                _error.value = "Gagal memposting: ${e.message}"
                // Jika gagal, hapus post sementara dari list
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
                // 1. Lakukan aksi ke server seperti biasa
                if (isCurrentlyLiked) {
                    postRepository.unlikePost(postId)
                } else {
                    postRepository.likePost(postId)
                }

                // 2. KELOLA STATE SECARA MANUAL
                val currentUserId = userPreferences.getUserId().first()
                if (currentUserId == null) {
                    _error.value = "Tidak bisa memproses like, user ID tidak ditemukan."
                    return@launch
                }

                // Salin daftar 'likes' ke dalam list yang bisa diubah (mutable)
                val mutableLikes = originalPost.likes.toMutableList()

                // 3. --- INI ADALAH PERBAIKANNYA ---
                if (isCurrentlyLiked) {
                    // Jika tadinya 'liked', cari indeks dari 'like' PERTAMA yang cocok
                    val indexToRemove = mutableLikes.indexOfFirst { it.authorId == currentUserId }
                    // Jika ditemukan (indeksnya bukan -1), HAPUS SATU saja dari daftar
                    if (indexToRemove != -1) {
                        mutableLikes.removeAt(indexToRemove)
                    }
                } else {
                    // Jika tadinya 'not liked', tambahkan 'like' baru (logika ini sudah benar)
                    mutableLikes.add(
                        com.example.bunnypost.data.remote.model.Like(
                            id = "temp-like-${System.currentTimeMillis()}",
                            authorId = currentUserId
                        )
                    )
                }

                // 4. Update state _postDetail dengan daftar 'likes' yang sudah dimodifikasi
                _postDetail.value = originalPost.copy(likes = mutableLikes)

            } catch (e: Exception) {
                _error.value = "Gagal memperbarui status like: ${e.message}"
            }
        }
    }
    fun toggleLikeOnList(postId: String) {
        viewModelScope.launch {
            // 1. Ambil postingan yang akan diubah dari state saat ini
            val postToUpdate = _postListState.value.find { it.id == postId } ?: return@launch
            val isCurrentlyLiked = postToUpdate.isLiked

            try {
                // 2. Lakukan aksi ke server seperti biasa
                if (isCurrentlyLiked) {
                    postRepository.unlikePost(postId)
                } else {
                    postRepository.likePost(postId)
                }

                // 3. Buat entitas post yang BARU dengan data yang sudah diupdate
                val updatedEntity = postToUpdate.copy(
                    isLiked = !isCurrentlyLiked,
                    likesCount = if (isCurrentlyLiked) postToUpdate.likesCount - 1 else postToUpdate.likesCount + 1
                )

                // 4. Simpan perubahan ini ke database lokal (INI KUNCINYA)
                postRepository.updatePostInDb(updatedEntity)

            } catch (e: Exception) {
                _error.value = "Gagal memperbarui like: ${e.message}"
                // Jika gagal, kita tidak perlu melakukan apa-apa. Tampilan tidak akan berubah
                // karena kita tidak pernah mengubahnya secara manual.
            }
        }
    }

    fun addComment(postId: String, content: String) {
        if (content.isBlank()) return

        val currentPost = _postDetail.value ?: return

        // Buat komentar sementara
        val optimisticCommentId = "temp_comment_${System.currentTimeMillis()}"
        val optimisticComment = com.example.bunnypost.data.remote.model.Comment(
            id = optimisticCommentId,
            content = content,
            createdAt = "...", // Placeholder
            authorId = "current_user_id" // Placeholder, perlu data user
        )

        // Update UI secara optimis
        _postDetail.value = currentPost.copy(
            comments = currentPost.comments + optimisticComment
        )

        viewModelScope.launch {
            try {
                postRepository.addComment(postId, content)
                // Refresh detail post untuk mendapatkan data asli
                getPostDetail(postId)
            } catch (e: Exception) {
                _error.value = "Gagal menambahkan komentar."
                // Rollback jika gagal
                _postDetail.value = currentPost.copy(
                    comments = currentPost.comments.filter { it.id != optimisticCommentId }
                )
            }
        }
    }
}
