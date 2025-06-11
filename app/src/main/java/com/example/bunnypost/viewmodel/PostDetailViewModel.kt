package com.example.bunnypost.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bunnypost.data.local.entity.PostEntity
import com.example.bunnypost.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    fun getPostById(postId: String): Flow<PostEntity?> {
        return repository.getPostById(postId)
    }
}
