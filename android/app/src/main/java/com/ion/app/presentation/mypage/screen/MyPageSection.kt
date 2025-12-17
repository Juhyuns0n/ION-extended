package com.ion.app.presentation.mypage.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ion.app.presentation.mypage.MenuSection
import com.ion.app.presentation.mypage.MyPageItem

@Composable
fun MyPageSection(section: MenuSection, onItemClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()
        .background(Color(0xA6FFFBF4))
        .padding(start = 16.dp)
    ) {
        if (section.title.isNotEmpty()) {
            Text(
                text = section.title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        section.items.forEach { item ->
            MyPageItem(item = item, onItemClick = onItemClick)
        }
    }
}