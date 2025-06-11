package com.example.bunnypost.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bunnypost.data.helper.Result
import com.example.bunnypost.viewmodel.AuthViewModel
import com.example.bunnypost.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onProfileUpdated: () -> Unit
) {
    val profileState by profileViewModel.profileState.collectAsState()
    val editProfileState by authViewModel.editProfileState.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var originalUsername by remember { mutableStateOf("") }
    var currentUserId by remember { mutableStateOf<String?>(null) }
    var serverProfilePictureUrl by remember { mutableStateOf<String?>(null) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        imageUri = uri
    }

    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(profileState) {
        if (profileState is Result.Success) {
            val user = (profileState as Result.Success).data
            firstName = user.firstName
            lastName = user.lastName
            bio = user.bio ?: ""
            originalUsername = user.username
            currentUserId = user.id
            serverProfilePictureUrl = user.profilePicture
        }
    }

    LaunchedEffect(editProfileState) {
        when (val state = editProfileState) {
            is Result.Success -> {
                val updatedUser = state.data
                snackbarMessage = "Profile updated successfully!"
                showSnackbar = true

                // Perbarui state di ProfileViewModel dengan data baru
                profileViewModel.updateProfileStateWithNewData(updatedUser)

                // Reset state edit dan URI gambar lokal
                authViewModel.resetEditProfileState()
                imageUri = null

                // Panggil callback untuk navigasi kembali
                onProfileUpdated()
            }
            is Result.Error -> {
                snackbarMessage = state.message
                showSnackbar = true
                authViewModel.resetEditProfileState()
            }
            else -> { /* Do nothing for Loading or null */ }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            when (profileState) {
                is Result.Loading -> {
                    CircularProgressIndicator()
                    Text("Loading profile data...")
                }
                is Result.Error -> {
                    Text("Error loading profile: ${(profileState as Result.Error).message}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { profileViewModel.fetchMyProfile() }) {
                        Text("Retry Load Profile")
                    }
                }
                is Result.Success -> {
                    val displayModel = imageUri ?: serverProfilePictureUrl

                    if (displayModel != null) {
                        AsyncImage(
                            model = displayModel,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .clickable {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = firstName.firstOrNull()?.toString()?.uppercase() ?: "U",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Text(
                        "Tap to change picture",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            currentUserId?.let { userId ->
                                authViewModel.updateProfile(
                                    userId = userId,
                                    firstName = firstName,
                                    lastName = lastName,
                                    username = originalUsername,
                                    bio = bio.ifEmpty { null },
                                    imageUri = imageUri
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = editProfileState !is Result.Loading && currentUserId != null
                    ) {
                        if (editProfileState is Result.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Save Changes", color = Color.White)
                        }
                    }
                }
                null -> {
                    LaunchedEffect(Unit) {
                        profileViewModel.fetchMyProfile()
                    }
                    CircularProgressIndicator()
                    Text("Preparing edit screen...")
                }
            }

            if (showSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showSnackbar = false }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(snackbarMessage)
                }
            }
        }
    }
}