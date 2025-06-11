// File: ProfileScreen.kt (VERSI FINAL YANG SUDAH DIPERBAIKI)
package com.example.bunnypost.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
// --- IMPORT YANG HILANG, SEKARANG SUDAH DITAMBAHKAN ---
import com.example.bunnypost.data.helper.Result
import com.example.bunnypost.data.local.entity.PostEntity
import com.example.bunnypost.viewmodel.ProfileViewModel
import androidx.compose.runtime.DisposableEffect // <-- TAMBAHKAN IMPORT INI
import androidx.compose.ui.platform.LocalLifecycleOwner // <-- TAMBAHKAN IMPORT INI
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    onLogout: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    val profileState by profileViewModel.profileState.collectAsState()
    val userPostsState by profileViewModel.userPostsState.collectAsState()
    val likedPostsState by profileViewModel.likedPostsState.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                profileViewModel.fetchMyProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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


                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            if (!user.profilePicture.isNullOrEmpty()) {
                                Card(
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .align(Alignment.BottomStart)
                                        .offset(x = 16.dp, y = 50.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    user.profilePicture?.let {
                                        AsyncImage(
                                            model = it,
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
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

                        }
                        Spacer(modifier = Modifier.height(66.dp))
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "${user.firstName} ${user.lastName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                            if(!user.bio.isNullOrEmpty()) {
                                Text(text = "@${user.username}", fontSize = 16.sp)
                                user.bio?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = it)
                                }
                            }else{
                                Text(text = "No bio available.",fontSize = 16.sp)
                                user.bio?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = it)
                                }

                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onEditProfileClick,
                            ) {
                                Text(
                                    "Edit Profile",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onLogout) {
                                Text("Logout")
                            }
                        }
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
                    0 -> {
                        UserContentList(state = userPostsState, emptyMessage = "You haven't posted anything yet.")
                    }
                    1 -> {
                        UserContentList(state = likedPostsState, emptyMessage = "You haven't liked any posts yet.")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))


            }
            is Result.Error -> {
                Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { profileViewModel.fetchMyProfile() }) {
                    Text("Retry")
                }
            }
            null -> {
                // Initial state, do nothing
            }
        }
    }
}

@Composable
fun UserContentList(state: Result<List<PostEntity>>, emptyMessage: String) {
    when (state) {
        is Result.Loading -> {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is Result.Success -> {
            val posts = state.data
            if (posts.isEmpty()) {
                Text(emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts, key = { it.id }) { postEntity ->
                        SimplePostCard(post = postEntity)
                    }
                }
            }
        }
        is Result.Error -> {
            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun SimplePostCard(post: PostEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(post.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(post.content, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        }
    }
}