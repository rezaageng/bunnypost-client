package com.example.bunnypost.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bunnypost.ui.navigation.BottomNavigationBar
import com.example.bunnypost.ui.navigation.NavigationItem
import com.example.bunnypost.viewmodel.AuthViewModel
import com.example.bunnypost.viewmodel.PostViewModel
import androidx.compose.material3.TopAppBar // <-- Tambahkan ini
import androidx.compose.material3.Text // <-- Tambahkan ini
import androidx.compose.material3.TopAppBarDefaults // <-- Tambahkan ini
import androidx.compose.ui.graphics.Color // <-- Tambahkan ini
import androidx.compose.material3.MaterialTheme // <-- Tambahkan ini

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onPostClick: (String) -> Unit
) {
    val bottomNavController = rememberNavController()
    val postViewModel: PostViewModel = hiltViewModel()

    Scaffold(
        // Tambahkan TopAppBar di sini
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bunny Post", // Nama aplikasi Anda
                        color = Color.White, // Sesuaikan warna teks
                        style = MaterialTheme.typography.titleLarge // Sesuaikan gaya teks
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary // Warna background header
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController = bottomNavController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = bottomNavController,
                startDestination = NavigationItem.Home.route
            ) {
                composable(NavigationItem.Home.route) {
                    HomeScreen(
                        viewModel = postViewModel,
                        onPostClick = onPostClick
                    )
                }

                composable(NavigationItem.Search.route) {
                    SearchScreen(navController = bottomNavController)
                }
                composable(NavigationItem.Profile.route) {
                    ProfileScreen(onLogout = {
                        authViewModel.logout(onLogout)
                    })
                }
                composable("post/{id}") { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("id") ?: ""
                    PostDetailScreen(
                        postId = postId,
                        onBack = { bottomNavController.popBackStack() } // Pass the back action
                    )
                }
                composable("profile/{username}") { backStackEntry ->
                    val username = backStackEntry.arguments?.getString("username") ?: ""
                    ProfileDetailScreen(
                        username = username,
                        onBack = { bottomNavController.popBackStack() }
                    )
                }
            }
        }
    }
}