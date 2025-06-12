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
import androidx.compose.material3.* // Import Material3 components
import androidx.compose.ui.res.stringResource // Import for string resources
import com.example.bunnypost.R // Import R class to access resources
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
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


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {


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


            items(posts, key = { it.id }) { postEntity ->

                val post = Post(
                    id = postEntity.id,
                    title = postEntity.title,
                    content = postEntity.content,
                    createdAt = postEntity.createdAt,
                    updatedAt = "",
                    authorId = postEntity.authorId,
                    author = Author(
                        id = postEntity.authorId,
                        username = postEntity.authorUsername,
                        firstName = postEntity.authorFirstName,
                        lastName = postEntity.authorLastName
                    ),
                    comments = emptyList(),
                    likes = emptyList()
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


        error?.let {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(text = it)
            }
        }


        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
}