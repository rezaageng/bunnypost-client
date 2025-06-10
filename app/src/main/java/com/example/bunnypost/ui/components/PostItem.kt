package com.example.bunnypost.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bunnypost.data.remote.model.Post

@Composable
fun PostItem(
    post: Post,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    likesCount: Int,
    commentsCount: Int,
    isLiked: Boolean // <-- TAMBAHKAN PARAMETER INI
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "by ${post.author.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.content, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- BAGIAN INI DIUBAH TOTAL ---
                Row(
                    modifier = Modifier.clickable { onLikeClick() }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        // Gunakan ikon berbeda berdasarkan status 'isLiked'
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Likes",
                        modifier = Modifier.size(18.dp),
                        // Gunakan warna berbeda berdasarkan status 'isLiked'
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Text(
                        text = "$likesCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isLiked) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                // Baris untuk Comment (tidak bisa diklik)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        // Ganti ikon agar konsisten dengan gaya di detail
                        imageVector = Icons.Filled.ChatBubble,
                        contentDescription = "Comments",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = "$commentsCount",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}