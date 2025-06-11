// app/src/main/java/com/example/bunnypost/ui/screen/SearchScreen.kt
package com.example.bunnypost.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bunnypost.R
import com.example.bunnypost.ui.viewmodel.SearchViewModel
import androidx.compose.foundation.shape.CircleShape // Tambahkan import ini
import androidx.compose.ui.draw.clip // Tambahkan import ini
import androidx.compose.ui.layout.ContentScale // Tambahkan import ini
import coil.compose.AsyncImage // Tambahkan import ini

@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val postResults by viewModel.searchResultsPosts.collectAsState()
    val userResults by viewModel.searchResultsUsers.collectAsState()

    val tabs = listOf("Posts", "Users")

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            label = { Text("Search BunnyPost") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(postResults) { post ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .clickable {
                                    navController.navigate("post/${post.id}")
                                }
                        ) {
                            // Bagian ini adalah untuk posts, tidak diubah sesuai permintaan "hanya user section"
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(end = 8.dp)
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${post.authorFirstName} ${post.authorLastName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "@${post.authorUsername}",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = post.content)
                            }
                        }
                        Divider()
                    }
                }
            }

            1 -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(userResults) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .clickable {
                                    navController.navigate("profile/${user.username}")
                                }
                        ) {
                            // --- MULAI PERUBAHAN DI SINI UNTUK BAGIAN USER ---
                            user.profilePicture?.let { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "User Profile Picture",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape), // Memastikan gambar berbentuk lingkaran
                                    contentScale = ContentScale.Crop // Memastikan gambar mengisi area tanpa distorsi
                                )
                            } ?: run {
                                // Fallback jika profilePicture null
                                Image(
                                    painter = painterResource(id = R.drawable.ic_profile), // Gunakan ikon profil default Anda
                                    contentDescription = "Default Avatar",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape) // Tetap berbentuk lingkaran
                                )
                            }
                            // --- AKHIR PERUBAHAN DI SINI UNTUK BAGIAN USER ---
                            Spacer(modifier = Modifier.width(8.dp)) // Tambahkan spasi
                            Column {
                                Text(
                                    text = "${user.firstName} ${user.lastName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "@${user.username}",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
}