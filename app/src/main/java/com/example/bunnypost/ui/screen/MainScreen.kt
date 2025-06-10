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
import com.example.bunnypost.viewmodel.ProfileViewModel // Import ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit // Ini adalah callback untuk navigasi utama (misal: ke LoginScreen)
) {
    val bottomNavController = rememberNavController()
    val postViewModel: PostViewModel = hiltViewModel()

    Scaffold(
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
                    val profileViewModel: ProfileViewModel = hiltViewModel()
                    ProfileScreen(
                        profileViewModel = profileViewModel,
                        onLogout = { // Ini adalah callback dari ProfileScreen
                            // Panggil logout dari AuthViewModel, dan teruskan onLogout dari MainScreen
                            // sebagai callback setelah logout selesai.
                            authViewModel.logout {
                                onLogout()
                            }
                        }
                    )
                }
            }
        }
    }
}