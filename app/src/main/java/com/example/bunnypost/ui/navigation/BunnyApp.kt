package com.example.bunnypost.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bunnypost.ui.screen.* // Import semua screen yang diperlukan
import com.example.bunnypost.viewmodel.AuthViewModel
import com.example.bunnypost.viewmodel.ProfileViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun BunnyApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Mendapatkan AuthViewModel di sini untuk digunakan di LaunchEffect
    // Ini memastikan bahwa AuthViewModel tersedia untuk memeriksa status login
    // dan menangani event logout global.

    // Listen for global logout events (memerlukan SessionManager di AuthViewModel atau serupa)
    // Jika authViewModel.sessionManager tidak ada, bagian ini perlu disesuaikan
    LaunchedEffect(Unit) {
        // Asumsi: authViewModel memiliki mekanisme untuk memancarkan logout event
        // Jika Anda belum memiliki sessionManager atau logoutEvent, bagian ini mungkin perlu disesuaikan
        // atau dihapus jika tidak ada event logout global yang perlu didengarkan di sini.
        // authViewModel.sessionManager.logoutEvent.collect {
        //     navController.navigate("login") {
        //         popUpTo(navController.graph.findStartDestination().id) {
        //             inclusive = true
        //         }
        //         launchSingleTop = true
        //     }
        // }
    }

    // Tangani navigasi berdasarkan status login awal
    LaunchedEffect(isLoggedIn) {
        when (isLoggedIn) {
            true -> navController.navigate("main") { // Navigasi ke main jika sudah login
                popUpTo("login") { inclusive = true } // Hapus login dari back stack
                popUpTo("splash") { inclusive = true } // Hapus splash jika ada
            }
            false -> navController.navigate("login") { // Navigasi ke login jika belum login
                popUpTo("profile") { inclusive = true } // Hapus profile jika ada (dari sesi sebelumnya)
                popUpTo("main") { inclusive = true } // Hapus main jika ada (dari sesi sebelumnya)
                popUpTo("splash") { inclusive = true } // Hapus splash jika ada
            }
            null -> {
                // Masih memeriksa status login, mungkin tetap di splash screen atau tampilkan loading
                // Tidak melakukan navigasi apa pun sampai status diketahui
            }
        }
    }


    NavHost(
        navController = navController,
        startDestination = "splash", // Ubah startDestination ke splash screen
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
                // Meneruskan lambda untuk parameter 'signUp'
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
                    // Panggil logout dari AuthViewModel, teruskan navigasi sebagai callback
                    authViewModel.logout {
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("profile") { // Ini rute untuk ProfileScreen jika diakses langsung (misal dari MainScreen internal)
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                profileViewModel = profileViewModel,
                onLogout = {
                    // Panggil logout dari AuthViewModel, teruskan navigasi sebagai callback
                    authViewModel.logout {
                        navController.navigate("login") { // Navigate to login on logout
                            popUpTo("profile") { inclusive = true }
                            popUpTo("main") { inclusive = true } // Tambahkan ini jika ProfileScreen diakses dari MainScreen
                        }
                    }
                }
            )
        }
    }
}