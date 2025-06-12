package com.example.bunnypost.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bunnypost.data.remote.model.Post
import com.example.bunnypost.viewmodel.PostViewModel
import androidx.compose.material.icons.filled.ArrowBack // Import this

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(postId: String,  onBack: () -> Unit, viewModel: PostViewModel = hiltViewModel()) {
    // Mengambil semua state yang dibutuhkan dari ViewModel
    val postDetail by viewModel.postDetail.collectAsState()
    val commentText by viewModel.commentText.collectAsState() // State untuk input field
    val isCommenting by viewModel.isCommenting.collectAsState() // State khusus untuk proses kirim comment
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLiked by viewModel.isLikedByCurrentUser.collectAsState()

    // Fetch data saat screen pertama kali dibuka
    LaunchedEffect(postId) {
        viewModel.getPostDetail(postId)
    }

    Scaffold(
        // Menempatkan input komentar di bagian bawah layar
        topBar = { // Add this topBar block
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Post Detail",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        bottomBar = {
            CommentInputField(
                value = commentText,
                onValueChange = { viewModel.onCommentTextChanged(it) }, // Update state di ViewModel
                onSendClick = { viewModel.addComment(postId) },
                isSending = isCommenting
            )
        }
    ) { paddingValues ->
        // Konten utama (detail post dan daftar komentar)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding dari Scaffold
        ) {
            when {
                // Tampilkan loading indicator besar hanya saat data awal dimuat
                isLoading && postDetail == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                // Tampilkan pesan error jika ada
                error != null -> {
                    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
                    }
                }
                // Tampilkan konten jika data berhasil dimuat
                postDetail != null -> {
                    PostContent(post = postDetail!!, isLiked = isLiked, onLikeClick = {
                        viewModel.toggleLikeOnDetail(postId)
                    })
                }
            }
        }
    }
}

@Composable
fun PostContent(post: Post, isLiked: Boolean, onLikeClick: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // --- KARTU DETAIL POST ---
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
                        "by ${post.author.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(post.content, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))
                    // Tombol Like dan Comment
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
                            onClick = onLikeClick
                        )
                        CountDisplay(
                            icon = Icons.Default.Comment,
                            count = post.comments.size,
                            text = "Comments"
                        )
                    }
                }
            }
        }

        // --- JUDUL BAGIAN KOMENTAR ---
        item {
            Text(
                "Komentar",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // --- DAFTAR KOMENTAR ---
        items(post.comments, key = { it.id }) { comment ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = comment.author.username,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun CommentInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean // State untuk menunjukkan proses pengiriman
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp // Beri bayangan agar terpisah dari konten
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("Tambahkan komentar...") },
                modifier = Modifier.weight(1f),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSendClick,
                enabled = value.isNotBlank() && !isSending // Nonaktifkan jika kosong atau sedang mengirim
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Kirim Komentar")
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
    isToggled: Boolean = false,
    onClick: (() -> Unit)? = null // onClick sekarang opsional
) {
    val rowModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = rowModifier.padding(vertical = 4.dp),
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
    }
}