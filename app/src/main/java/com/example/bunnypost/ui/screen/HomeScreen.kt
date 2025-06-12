package com.example.bunnypost.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bunnypost.data.remote.model.Author
import com.example.bunnypost.data.remote.model.Post
import com.example.bunnypost.ui.components.PostItem
import com.example.bunnypost.viewmodel.PostViewModel
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    viewModel: PostViewModel,
    onPostClick: (String) -> Unit
) {
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
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Item untuk membuat postingan baru
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = viewModel.title,
                        onValueChange = { viewModel.title = it },
                        label = { Text("Judul") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.content,
                        onValueChange = { viewModel.content = it },
                        label = { Text("Konten") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.submitPost() },
                        enabled = viewModel.title.isNotBlank() && viewModel.content.isNotBlank(),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Posting")
                    }
                }
            }

            // Item untuk daftar postingan
            items(posts, key = { it.id }) { postEntity ->
                // Obyek 'Post' ini dibuat hanya untuk kompatibilitas dengan 'PostItem'
                // Data utama yang ditampilkan berasal dari 'postEntity'
                val post = Post(
                    id = postEntity.id,
                    title = postEntity.title,
                    content = postEntity.content,
                    // DIPERBAIKI: Gunakan 'timestamp' dan format ke String
                    createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date(postEntity.timestamp)),
                    updatedAt = "", // Tidak digunakan di list
                    // DIPERBAIKI: Gunakan 'userId'
                    authorId = postEntity.userId,
                    author = Author(
                        // DIPERBAIKI: Gunakan 'userId'
                        id = postEntity.userId,
                        username = postEntity.authorUsername,
                        firstName = postEntity.authorFirstName,
                        lastName = postEntity.authorLastName,
                        profilePicture = postEntity.profilePicture
                    ),
                    comments = emptyList(), // Tidak digunakan di list
                    likes = emptyList()      // Tidak digunakan di list
                )

                PostItem(
                    post = post,
                    onClick = { onPostClick(post.id) },
                    onLikeClick = { viewModel.toggleLikeOnList(post.id) },
                    likesCount = postEntity.likesCount,
                    commentsCount = postEntity.commentsCount,
                    isLiked = postEntity.isLiked
                )
            }

            // Indikator loading di bagian bawah saat paginasi
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

        // Logika untuk mendeteksi scroll hingga ke bawah untuk memuat lebih banyak data
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
            if (isScrolledToEnd && !isPaginating && !isLoading) {
                viewModel.loadMorePosts()
            }
        }

        // Menampilkan pesan error jika ada
        error?.let {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(text = it)
            }
        }

        // Indikator pull-to-refresh di bagian atas
        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}