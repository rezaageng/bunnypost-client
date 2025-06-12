package com.example.bunnypost.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bunnypost.ui.screen.*
import com.example.bunnypost.viewmodel.AuthViewModel
import com.example.bunnypost.viewmodel.ProfileViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun BunnyApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Global logout event handler
    LaunchedEffect(Unit) {
        authViewModel.sessionManager.logoutEvent.collect {
            navController.navigate("login") {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable("splash") {
            SplashScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                signUp = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = {
                    navController.navigate("main") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logout {
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                },
                onEditProfileClick = {
                    navController.navigate("editProfile")
                },
                onPostClick = { postId ->
                    navController.navigate("postDetail/$postId")
                }
            )
        }

        composable("editProfile") {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            EditProfileScreen(
                authViewModel = authViewModel,
                profileViewModel = profileViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onProfileUpdated = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "postDetail/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            PostDetailScreen(postId = postId)
        }
    }
}