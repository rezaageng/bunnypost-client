// Sebelumnya menerima: Author
// Sekarang ubah menjadi menerima: UserEntity

package com.example.bunnypost.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bunnypost.data.local.entity.UserEntity

@Composable
fun UserItem(user: UserEntity, modifier: Modifier) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = "@${user.username}")
        Text(text = "${user.firstName} ${user.lastName}")
    }
}
