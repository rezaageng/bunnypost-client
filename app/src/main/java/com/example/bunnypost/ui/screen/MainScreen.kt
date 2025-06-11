package com.example.bunnypost.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter // <-- Pastikan import ini ada
import com.example.bunnypost.R // <-- Pastikan import ini ada
import com.example.bunnypost.ui.navigation.BottomNavigationBar
import com.example.bunnypost.ui.navigation.NavigationItem
import com.example.bunnypost.viewmodel.AuthViewModel
import com.example.bunnypost.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val postViewModel: PostViewModel = hiltViewModel()
    // Mengamati data pengguna yang sedang login dari AuthViewModel
    val loggedInUser by authViewModel.loggedInUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        // Foto Profil Pengguna
                        val profilePictureUrl = loggedInUser?.profilePicture
                        if (!profilePictureUrl.isNullOrEmpty()) {
                            // Memuat gambar dari URL menggunakan Coil
                            Image(
                                painter = rememberAsyncImagePainter(profilePictureUrl),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(32.dp) // Ukuran foto profil
                                    .clip(CircleShape) // Membuat foto profil menjadi lingkaran
                            )
                        } else {
                            // Placeholder jika tidak ada foto profil atau URL kosong
                            Image(
                                painter = painterResource(id = R.drawable.ic_profile), // Menggunakan ikon profil default
                                contentDescription = "Default Profile Picture",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp)) // Memberikan jarak antara foto dan teks
                        // Nama Aplikasi
                        Text(
                            text = stringResource(id = R.string.app_name), // Mengambil nama aplikasi dari strings.xml
                            style = MaterialTheme.typography.titleLarge, // Gaya teks
                            color = MaterialTheme.colorScheme.onPrimaryContainer // Warna teks
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Warna latar belakang header
                    titleContentColor = MaterialTheme.colorScheme.onPrimary // Warna konten (teks, ikon) di header
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController = bottomNavController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(bottomNavController, startDestination = NavigationItem.Home.route) {
                composable(NavigationItem.Home.route) {
                    HomeScreen(viewModel = postViewModel)
                }
                composable(NavigationItem.Search.route) {
                    SearchScreen()
                }
                composable(NavigationItem.Profile.route) {
                    ProfileScreen(onLogout = {
                        authViewModel.logout(onLogout)
                    })
                }
            }
        }
    }
}