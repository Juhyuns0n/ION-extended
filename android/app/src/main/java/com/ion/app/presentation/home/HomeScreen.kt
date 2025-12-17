package com.ion.app.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ion.app.R
import com.ion.app.core.designsystem.ui.theme.beige
import com.ion.app.core.util.buildImageUrl
import com.ion.app.presentation.home.navigation.HomeViewModel


@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    topPadding: Dp,
    bottomPadding: Dp,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        topPadding = topPadding,
        bottomPadding = bottomPadding,
        uiState = uiState
    )
}


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    topPadding: Dp,
    bottomPadding: Dp,
    uiState: HomeUiState
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(beige)
            .padding(top = topPadding + 20.dp, bottom = bottomPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = bottomPadding)
        ) {
            // 상단 프로필 영역
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val context = LocalContext.current
                val imageUrl = buildImageUrl(uiState.userImagePath)

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
                        .size(86.dp)                 // 안쪽 이미지 살짝 작게
                        .clip(CircleShape),          // 이미지도 동그랗게
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text("LV.${uiState.level}", color = Color(0xFF9D8B7C))
                    Text(
                        "${uiState.parentNickname} 님",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    // points를 경험치로 사용, total은 기존 50000 그대로
                    ParentingProgressBar(current = uiState.points, total = 50000)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = Color(0x99FF8373)
            )

            // Streak 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, bottom = 0.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_heart),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("STREAK", color = Color(0xFFFF621F), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        uiState.streakDay.toString(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        style = typography.titleLarge
                    )
                    Text(
                        "DAY",
                        color = Color(0xFFFF621F),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            // 명언
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .paint(
                        painter = painterResource(R.drawable.img_rectangle2),
                        contentScale = ContentScale.FillBounds
                    )
                    .padding(top = 20.dp, bottom = 25.dp, start = 30.dp, end = 30.dp)
            ) {
                Text(
                    uiState.phrase.ifBlank { "육아만큼 숭고하고 훌륭한 일은 이 세상에 없다." },
                    color = Color(0xFF9B7575),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 이번달 활동 요약
            Box(
                Modifier
                    .shadow(
                        2.dp,
                        RoundedCornerShape(16.dp),
                        ambientColor = Color(0xFF772A0B).copy(alpha = 0.13f)
                    )
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
                    .background(Color(0xFFFDF2CA), RoundedCornerShape(16.dp))
                    .padding(vertical = 18.dp, horizontal = 14.dp)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = "이번달 활동 요약",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E4636),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    Text(
                        text = "활동 빈도: ${uiState.monthFrequency} DAY /30 DAY",
                        color = Color(0xFF4E4636),
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                    )

                    Text(
                        text = "-------------------------",
                        color = Color(0xFFB8B6AF),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                    ) {
                        Image(painterResource(R.drawable.img_camera), null, Modifier.size(22.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "보이스리포트: ${uiState.voicereportFrequency}회",
                            fontSize = 14.sp,
                            color = Color(0xFF44403B)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Image(painterResource(R.drawable.ic_book), null, Modifier.size(22.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "워크북: ${uiState.workBookFrequency} Lesson",
                            fontSize = 14.sp,
                            color = Color(0xFF44403B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 말풍선 + 토끼 영역
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_rabbit),
                    contentDescription = null,
                    modifier = Modifier.size(112.dp)
                )
                Box(
                    modifier = Modifier
                        .paint(
                            painter = painterResource(R.drawable.img_speechbubble),
                            contentScale = ContentScale.FillBounds
                        )
                        .padding(top = 15.dp, bottom = 20.dp, start = 55.dp, end = 30.dp)
                ) {
                    Text(
                        text = uiState.message.ifBlank {
                            "보이스리포트를 이용한지 \n3일 되었어요.\n오늘은 새로운 보고서를 확인해 볼까요?"
                        },
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Image(
                painter = painterResource(R.drawable.ic_banner),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(45.dp))

            // Reward
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
                    .padding(vertical = 9.dp, horizontal = 0.dp)
            ) {
                Column {
                    Text(
                        text = "Reward",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF4E4636),
                        modifier = modifier.padding(start = 20.dp)
                    )
                    Spacer(Modifier.padding(10.dp))
                    RewardGrid(
                        rewards = uiState.rewards
                    )
                }
            }
        }
    }
}

@Composable
fun RewardGrid(rewards: List<RewardItem>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        for (row in rewards.chunked(3)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (item in row) {

                    // 획득 여부에 따른 스타일
                    val alpha = if (item.earned) 1f else 0.35f
                    val colorFilter =
                        if (item.earned) null
                        else ColorFilter.colorMatrix(
                            ColorMatrix().apply { setToSaturation(0f) }
                        )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(item.resId),
                            contentDescription = item.label,
                            modifier = Modifier
                                .size(64.dp)
                                .alpha(alpha),
                            colorFilter = colorFilter
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.label,
                            fontSize = 13.sp,
                            color = if (item.earned) Color(0xFF4D0909) else Color(0x664D0909)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParentingProgressBar(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,   // <- 드로어에서는 false로 쓸 예정
) {
    val raw = if (total > 0) current / total.toFloat() else 0f
    val progress =
        if (raw <= 0f) 0f
        else kotlin.math.max(0.05f, raw)   // 0보다 크면 최소 5%

    Column(modifier = modifier) {
        if (showLabel) {
            Text(
                text = "$current/$total",
                color = Color(0xFFD9BDE6),
                fontSize = 12.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .paint(
                    painterResource(R.drawable.img_progressbar),
                    contentScale = ContentScale.FillBounds
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .paint(
                        painterResource(R.drawable.img_filledprogressbar),
                        contentScale = ContentScale.FillBounds
                    )
            )
        }
    }
}



