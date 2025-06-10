package com.example.bunnypost.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bunnypost.data.local.entity.PostEntity
import com.example.bunnypost.data.local.entity.UserEntity
import com.example.bunnypost.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResultsPosts: StateFlow<List<PostEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else postRepository.searchPosts(query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val searchResultsUsers: StateFlow<List<PostEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else postRepository.searchUsersFromPosts(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())




    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
