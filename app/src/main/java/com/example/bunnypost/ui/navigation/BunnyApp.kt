package com.example.bunnypost.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bunnypost.ui.screen.LoginScreen
import com.example.bunnypost.ui.screen.MainScreen
import com.example.bunnypost.ui.screen.SignUpScreen
import com.example.bunnypost.ui.screen.SplashScreen
import com.example.bunnypost.viewmodel.AuthViewModel

@Composable
fun BunnyApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController, startDestination = "splash", modifier = Modifier.background(
            MaterialTheme.colorScheme.background
        )
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
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}