package com.example.bunnypost.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import com.example.bunnypost.viewmodel.ProfileViewModel

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onEditProfileClick: () -> Unit,
    onPostClick: (String) -> Unit
) {
    val bottomNavController = rememberNavController()
    // Membuat instance ViewModel di level "induk"
    val postViewModel: PostViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()

    Scaffold(
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
                    // Meneruskan instance ViewModel yang sama ke ProfileScreen
                    ProfileScreen(
                        profileViewModel = profileViewModel, // Menggunakan instance dari induk
                        onLogout = {
                            authViewModel.logout {
                                onLogout()
                            }
                        },
                        onEditProfileClick = onEditProfileClick
                    )
                }
                composable("post/{id}") { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("id") ?: ""
                    PostDetailScreen(
                        postId = postId,
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