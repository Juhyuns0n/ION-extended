package com.ion.app.presentation.mypage.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ion.app.R
import com.ion.app.core.util.buildImageUrl
import com.ion.app.presentation.home.HomeUiState
import com.ion.app.presentation.home.ParentingProgressBar
import com.ion.app.presentation.mypage.UserInfo

@Composable
fun UserProfileCard(user: UserInfo?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xD4FFFBF4))
            .padding(start = 16.dp, top = 70.dp, bottom = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        val imageUrl = buildImageUrl(user?.userImagePath)

        // 프로필 이미지 (지금은 로컬 아이콘 유지, 나중에 userImagePath로 교체 가능)
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)              // 서버 URL (null이면 placeholder만 나옴)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.ic_profile),
            error = painterResource(R.drawable.ic_profile),
            fallback = painterResource(R.drawable.ic_profile),
            contentDescription = "Profile",
            modifier = Modifier
                .size(100.dp)                 // 안쪽 이미지 살짝 작게
                .clip(CircleShape),          // 이미지도 동그랗게
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(15.dp))

        Column {
            if (user != null) {
                Text("LV.${user.level}", style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp)
                Text("${user.name} 님 환영합니다!", style = MaterialTheme.typography.titleMedium, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.width(180.dp)){
                    ParentingProgressBar(
                        current = user.points,
                        total = user.totalPoints,
                        modifier = Modifier.width(170.dp),
                        showLabel = false
                    )
                }
            } else {
                // 로딩 중 혹은 비로그인 상태일 때 placeholder 표시
                Text("LV.--", style = MaterialTheme.typography.bodyMedium)
                Text("사용자 정보를 불러오는 중...", style = MaterialTheme.typography.titleMedium)
                Box(modifier = Modifier.width(160.dp)){
                    ParentingProgressBar(
                        current = 0,
                        total = 50000,
                        modifier = Modifier.width(160.dp),
                        showLabel = false
                    )
                }
            }
        }
    }
}
