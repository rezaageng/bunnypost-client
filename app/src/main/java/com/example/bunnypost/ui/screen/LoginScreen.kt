package com.example.bunnypost.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bunnypost.viewmodel.AuthViewModel
import com.example.bunnypost.data.helper.Result

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by authViewModel.loginState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { authViewModel.login(email, password) }) {
            when (val state = loginState) {
                is Result.Loading -> {
                    CircularProgressIndicator()
                }

                is Result.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }

                is Result.Success -> {
                    // Navigate on success
                    LaunchedEffect(Unit) {
                        onLoginSuccess()
                    }
                }

                null -> {

                    Text("Login")

                }
            }
        }


    }
}