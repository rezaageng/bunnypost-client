package com.example.bunnypost.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bunnypost.data.remote.model.Comment
import com.example.bunnypost.data.remote.model.Post
import com.example.bunnypost.viewmodel.PostViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.ArrowBack


@OptIn(ExperimentalMaterial3Api::class) // Add this annotation for TopAppBar
@Composable
fun PostDetailScreen(
    postId: String,
    viewModel: PostViewModel = hiltViewModel(),
    onBack: () -> Unit // Add onBack parameter
) {
    val postDetail by viewModel.postDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var commentText by remember { mutableStateOf("") }
    val isLiked by viewModel.isLikedByCurrentUser.collectAsState()

    LaunchedEffect(postId) {
        viewModel.getPostDetail(postId)
    }

    Scaffold( // Use Scaffold to provide a top bar
        topBar = {
            TopAppBar(
                title = { Text("Post Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) { // Back button
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding -> // Apply innerPadding to the content
        when {
            isLoading && postDetail == null -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
                }
            }
            postDetail != null -> {
                val post = postDetail!!
                val comments = post.comments

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(innerPadding), // Apply inner padding here
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                Text(post.title, style = MaterialTheme.typography.headlineSmall)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Author: ${post.author.username}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(post.content, style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.height(16.dp))
                                Divider()
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                                ) {
                                    CountDisplay(
                                        icon = Icons.Default.Favorite,
                                        count = post.likes.size,
                                        text = "Likes",
                                        isToggled = isLiked,
                                        onClick = { viewModel.toggleLikeOnDetail(postId) }
                                    )
                                    CountDisplay(
                                        icon = Icons.Default.Comment,
                                        count = comments.size,
                                        text = "Comments",
                                        isToggled = false,
                                        onClick = { }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "Komentar",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(comments, key = { it.id }) { comment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                val authorDisplayName = if (comment.authorId == post.author.id) {
                                    post.author.username
                                } else {
                                    val shortId = comment.authorId?.takeLast(6) ?: "XXXXXX"
                                    "User ...$shortId"
                                }

                                Text(
                                    text = authorDisplayName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = comment.content,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            label = { Text("Tambahkan komentar") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    viewModel.addComment(postId, commentText.trim())
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank() && !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Kirim Komentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountDisplay(
    icon: ImageVector,
    count: Int,
    text: String,
    isToggled: Boolean,
    onClick: () -> Unit
) {
    val modifier = Modifier.clickable(onClick = onClick)

    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (isToggled) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isToggled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}