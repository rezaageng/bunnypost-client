package com.example.bunnypost.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bunnypost.viewmodel.ProfileViewModel
import com.example.bunnypost.data.helper.Result
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    val profileState by profileViewModel.profileState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        when (val state = profileState) {
            is Result.Loading -> {
                CircularProgressIndicator()
                Text("Loading profile...", modifier = Modifier.padding(top = 8.dp))
            }
            is Result.Success -> {
                val user = state.data

                if (user.profilePicture != null && user.profilePicture.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.profilePicture)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.firstName.firstOrNull()?.toString()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Tambahkan Bio di sini
                if (!user.bio.isNullOrEmpty()) {
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    Text(
                        text = "No bio available.", // Teks placeholder jika bio kosong
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp)) // Spacer setelah bio atau placeholder

                Button(
                    onClick = {
                        println("Edit Profile Clicked!")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Edit Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                "Posts",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                "Likes",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTabIndex) {
                    0 -> UserPostsList(profileViewModel = profileViewModel)
                    1 -> UserLikesList(profileViewModel = profileViewModel)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        profileViewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Logout",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            is Result.Error -> {
                Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { profileViewModel.fetchMyProfile() }) {
                    Text("Retry")
                }
            }
            null -> {
                // Initial state, might show a loading indicator or nothing
            }
        }
    }
}

@Composable
fun UserPostsList(profileViewModel: ProfileViewModel) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Posts by this user will appear here.")
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
            items(5) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Post Title ${index + 1}", fontWeight = FontWeight.Bold)
                        Text("This is the content of post ${index + 1}.")
                    }
                }
            }
        }
    }
}

@Composable
fun UserLikesList(profileViewModel: ProfileViewModel) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Liked posts will appear here.")
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
            items(3) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Liked Post Title ${index + 1}", fontWeight = FontWeight.Bold)
                        Text("This is a liked post by the user.")
                    }
                }
            }
        }
    }
}