package com.ion.app.presentation.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.ion.app.presentation.mypage.screen.MyPageSection
import com.ion.app.presentation.mypage.screen.UserProfileCard

@Composable
fun MyPageDrawerContent(
    onItemClick: (String) -> Unit,
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    val user = uiState.userInfo
    val sections = uiState.menuSections

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(350.dp)
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFFDCAA7))
    ) {
        UserProfileCard(
            user = user
        )

        Spacer(modifier = Modifier.height(6.dp))

        sections.forEachIndexed { index, section ->
            MyPageSection(section = section, onItemClick = onItemClick)
            if (index != sections.lastIndex) {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color(0xA6FFFBF4))
        )
    }
}
