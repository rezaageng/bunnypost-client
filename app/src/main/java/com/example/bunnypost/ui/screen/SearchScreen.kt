package com.example.bunnypost.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.bunnypost.R

@Composable
fun SearchScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("Posts", "Users")

    val dummyPosts = listOf(
        Triple("userone", "User One", "Ini adalah tweet pertama"),
        Triple("usertwo", "User Two", "Postingan kedua yang menarik perhatian.")
    )

    val dummyUsers = listOf(
        Pair("userone", "User One"),
        Pair("usertwo", "User Two")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Twitter") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(dummyPosts) { (username, name, content) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .clickable { /* navigate to Post */ }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground), // dummy avatar
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(end = 8.dp)
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "@$username",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = content)
                            }
                        }
                        Divider()
                    }
                }
            }

            1 -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(dummyUsers) { (username, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .clickable { /* navigate to profile */ }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(end = 8.dp)
                            )
                            Column {
                                Text(
                                    text = name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "@$username",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
}
