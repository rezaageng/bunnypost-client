// app/src/main/java/com/example/bunnypost/ui/screen/HomeScreen.kt
package com.example.bunnypost.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bunnypost.data.remote.model.Author
import com.example.bunnypost.data.remote.model.Post
import com.example.bunnypost.ui.components.PostItem
import com.example.bunnypost.viewmodel.PostViewModel
import java.time.Instant // Import ini

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(viewModel: PostViewModel) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPaginating by viewModel.isPaginating.collectAsState()
    val error by viewModel.error.collectAsState()
    val listState = rememberLazyListState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.refreshPosts() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        if (posts.isNotEmpty()) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(posts, key = { it.id }) { postEntity ->
                    val post = Post(
                        id = postEntity.id,
                        title = postEntity.title,
                        content = postEntity.content,
                        // Perbaikan di sini: gunakan timestamp dari PostEntity dan konversi ke String
                        createdAt = Instant.ofEpochMilli(postEntity.timestamp).toString(),
                        updatedAt = "", // Anda mungkin perlu mengatur ini berdasarkan kebutuhan API Anda
                        // Perbaikan di sini: gunakan userId dari PostEntity
                        authorId = postEntity.userId,
                        author = Author(
                            id = postEntity.userId, // Perbaikan di sini
                            username = postEntity.authorUsername,
                            firstName = postEntity.authorFirstName,
                            lastName = postEntity.authorLastName
                        ),
                        comments = emptyList(),
                        likes = emptyList()
                    )
                    PostItem(post = post)
                }
                if (isPaginating) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            // Derived state to trigger pagination
            val isScrolledToEnd by remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val visibleItemsInfo = layoutInfo.visibleItemsInfo
                    if (layoutInfo.totalItemsCount == 0) {
                        false
                    } else {
                        val lastVisibleItem = visibleItemsInfo.lastOrNull()
                        val viewportEndOffset = layoutInfo.viewportEndOffset
                        (lastVisibleItem?.index ?: 0) >= (layoutInfo.totalItemsCount - 5) &&
                                (lastVisibleItem?.offset ?: 0) <= viewportEndOffset
                    }
                }
            }

            LaunchedEffect(isScrolledToEnd) {
                if (isScrolledToEnd) {
                    viewModel.loadMorePosts()
                }
            }
        } else if (!isLoading) {
            Text("No posts found. Pull to refresh.", modifier = Modifier.align(Alignment.Center))
        }

        error?.let {
            Text(text = it, modifier = Modifier.align(Alignment.Center))
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}