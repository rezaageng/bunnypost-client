package com.example.bunnypost.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bunnypost.ui.screen.LoginScreen
import com.example.bunnypost.viewmodel.AuthViewModel

@Composable
fun BunnyApp() {
    val navController = rememberNavController()


    NavHost(
        navController = navController, startDestination = "login", modifier = Modifier.background(
            MaterialTheme.colorScheme.background
        )
    ) {
        composable("login") {
            val viewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                authViewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                signUp = {
                    navController.navigate("signup") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            // Tambahkan HomeScreen di sini nanti
        }
        composable("signup") {
            // Tambahkan SignUpScreen di sini nanti
        }
    }
}