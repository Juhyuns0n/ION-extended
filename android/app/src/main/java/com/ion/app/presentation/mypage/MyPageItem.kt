package com.ion.app.presentation.mypage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyPageItem(item: com.ion.app.presentation.mypage.MenuItem, onItemClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item.key) }
            .padding(vertical = 10.dp, horizontal = 16.dp)
    ) {
        Text(text = item.title, fontSize = 15.sp)
    }
}