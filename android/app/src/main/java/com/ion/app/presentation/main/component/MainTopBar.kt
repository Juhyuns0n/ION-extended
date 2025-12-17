package com.ion.app.presentation.main.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ion.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    onMenuClick: () -> Unit
) {
    Column {
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu),
                        contentDescription = "menu",
                        tint = Color.Unspecified
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Divider(
            color = Color(0xFFF3F1EA), // 10% 불투명한 블랙
            thickness = 1.4.dp
        )
    }
}
