package com.example.bunnypost.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bunnypost.viewmodel.AuthViewModel
import com.example.bunnypost.viewmodel.ProfileViewModel
import com.example.bunnypost.data.helper.Result
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight

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
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(profileState) {
        if (profileState is Result.Success) {
            val user = (profileState as Result.Success).data
            firstName = user.firstName
            lastName = user.lastName
            username = user.username
            bio = user.bio ?: ""
        }
    }

    LaunchedEffect(editProfileState) {
        when (editProfileState) {
            is Result.Success -> {
                snackbarMessage = "Profile updated successfully!"
                showSnackbar = true
                authViewModel.resetEditProfileState()
                profileViewModel.fetchMyProfile()
                onProfileUpdated()
            }
            is Result.Error -> {
                snackbarMessage = (editProfileState as Result.Error).message
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
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
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
                            authViewModel.updateProfile(firstName, lastName, username, bio.ifEmpty { null })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = editProfileState !is Result.Loading
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