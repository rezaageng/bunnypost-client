package com.example.bunnypost.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bunnypost.viewmodel.AuthViewModel
import com.example.bunnypost.data.helper.Result
import androidx.compose.ui.graphics.Color

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    signUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showEmptyError by remember { mutableStateOf(false) }
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
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (showEmptyError) {
            Text(
                text = "Email and password cannot be empty.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        } else if (loginState is Result.Error) {
            Text(
                text = (loginState as Result.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        } else if (loginState is Result.Success) {
            onLoginSuccess()
        }

        Button(onClick = {
            if (email.isEmpty() || password.isEmpty()) {
                showEmptyError = true
                return@Button
            } else {
                showEmptyError = false
            }
            authViewModel.login(email, password)
        }) {
            if (loginState is Result.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Login", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ", color = Color.White)
            TextButton(onClick = { signUp() }, contentPadding = PaddingValues(0.dp)) {
                Text("Sign Up")
            }
        }

    }
}