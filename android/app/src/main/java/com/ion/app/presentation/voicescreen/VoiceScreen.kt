package com.ion.app.presentation.voicescreen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ion.app.R
import com.ion.app.core.designsystem.ui.theme.beige
import com.ion.app.domain.model.voicereport.RecentVoiceSummaryModel
import com.ion.app.presentation.voicescreen.navigation.VoiceViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@Preview(showBackground = true)
@Composable
fun VoiceScreenPreview() {
    val dummyState = VoiceUiState(
        isLoading = false,
        isUploading = false,
        recentSummary = RecentVoiceSummaryModel(
            momentEmotion = "위축",
            parentFrequency = 5,
            kidFrequency = 1,
            overallFeedback = "의도는 경계 설정일 수 있으나 표현이 자존감을 해치는 방식입니다. 나이와 활발한 성향을 고려해 단호한 경계는 유지하되, 감정 인정과 안전한 회복 경로를 함께 제시하세요."
        ),
        reports = emptyList()
    )

    VoiceScreen(
        topPadding = 0.dp,
        bottomPadding = 30.dp,
        uiState = dummyState,
        viewModel = hiltViewModel(),
        navigateToVoiceReport = {}
    )
}


@Composable
fun VoiceRoute(
    modifier: Modifier = Modifier,
    topPadding: Dp,
    bottomPadding: Dp,
    navigateToVoiceReport: (Long) -> Unit,
    viewModel: VoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecentSummary()
    }

    VoiceScreen(
        navigateToVoiceReport = navigateToVoiceReport,
        topPadding = topPadding,
        bottomPadding = bottomPadding,
        viewModel = viewModel,
        uiState = uiState
    )
}

@Composable
fun VoiceScreen(
    navigateToVoice: () -> Unit = {},
    navigateToVoiceReport: (Long) -> Unit = {},
    viewModel: VoiceViewModel,
    uiState: VoiceUiState,
    topPadding: Dp,
    bottomPadding: Dp
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showResultDialog by remember { mutableStateOf(false) }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val contentResolver = context.contentResolver

            val tempFile = kotlin.io.path.createTempFile(suffix = ".mp4")
                .toFile()
            contentResolver.openInputStream(it)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val requestBody = tempFile.asRequestBody("video/mp4".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("video", tempFile.name, requestBody)

            viewModel.uploadVoiceReport(body)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(beige)
            .padding(top = topPadding, bottom = bottomPadding)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // 동영상 업로드 섹션
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .paint(
                        painter = painterResource(R.drawable.bg_addvoice),
                        contentScale = ContentScale.FillBounds
                    )
                    .size(height = 260.dp, width = 370.dp)
                    .padding(20.dp)
            ) {
                Button(
                    onClick = { if (!uiState.isUploading) launcher.launch("video/*") },
                    enabled = !uiState.isUploading,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .paint(
                            painterResource(R.drawable.button_addvoice),
                            contentScale = ContentScale.FillBounds
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Unspecified,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.Unspecified
                    )
                ) {}

                Text(
                    text = "동영상 추가",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color(0xFF6D4444),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 90.dp)
                )

            }

            // 업로드 결과 다이얼로그
            if (showResultDialog && uiState.uploadMessage != null) {
                AlertDialog(
                    onDismissRequest = { showResultDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showResultDialog = false }) {
                            Text("확인")
                        }
                    },
                    title = { Text("알림") },
                    text = { Text(uiState.uploadMessage) }
                )
            }

            Spacer(Modifier.height(36.dp))

            // 말투 분석 섹션
            Text("최근 대화 요약", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(12.dp))

            RecentSummaryBox(summary = uiState.recentSummary, errorMessage = uiState.recentSummaryError)

            Spacer(Modifier.height(35.dp))

            // 리포트 리스트
            Text("분석 리포트", fontWeight = FontWeight.Bold, fontSize = 19.sp)
            Spacer(Modifier.height(8.dp))

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                uiState.reports.isEmpty() -> {
                    Text("아직 분석 리포트가 없습니다.", color = Color.Gray, fontSize = 14.sp)
                }

                else -> {
                    uiState.reports.forEach { report ->
                        VoiceReportCard(
                            reportId = report.id,
                            title = report.subTitle ?: "무제 보고서",
                            date = report.day ?: "날짜 없음",
                            onClick = { navigateToVoiceReport(report.id) }
                        )
                    }
                }
            }
        }

        // 업로드/분석 중일 때 화면 전체 오버레이
        if (uiState.isUploading) {
            VoiceUploadingOverlay()
        }
        if (uiState.uploadMessage != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearUploadMessage() },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearUploadMessage() }) {
                        Text("확인")
                    }
                },
                title = { Text("알림") },
                text = { Text(uiState.uploadMessage!!) }
            )
        }
    }
}

@Composable
fun VoiceReportCard(
    reportId: Long,
    title: String,
    date: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xE8FFE0ED)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            // 메인 제목 (보고서 번호)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${reportId}번째 이야기",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )

                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ": $title",
                    fontSize = 14.sp,
                    color = Color(0xFF6D6D6D),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .size(width = 30.dp, height = 30.dp)
                        .paint(
                            painterResource(R.drawable.ic_select3),
                            contentScale = ContentScale.FillBounds
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Unspecified
                    )
                ) {}
            }
        }
    }
}

@Composable
fun RecentSummaryBox(
    summary: RecentVoiceSummaryModel?,
    errorMessage: String?,
) {
    val noDataMessage = "최근 대화가 없어요!\n동영상을 추가해보세요."

    // --- 공통 박스 스타일 함수 ---
    @Composable
    fun BaseBox(content: @Composable BoxScope.() -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .paint(
                    painter = painterResource(R.drawable.bg_voiceanalysis),
                    contentScale = ContentScale.FillBounds
                )
                .padding(start = 8.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
            content = content
        )
    }

    // 에러 / 데이터 없음
    if (summary == null || errorMessage != null) {
        BaseBox {
            Text(
                text = noDataMessage,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF460B0B),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        return
    }

    // 정상 데이터
    val emotionLabel = summary.momentEmotion ?: "-"
    val parentFreq = summary.parentFrequency
    val kidFreq = summary.kidFrequency

    val talkBalanceLabel = when {
        parentFreq == null || kidFreq == null -> "-"
        parentFreq >= kidFreq * 2 -> "부모 발화 많음"
        kidFreq >= parentFreq * 2 -> "아이 발화 많음"
        else -> "대화 균형"
    }

    val freqText = if (parentFreq != null && kidFreq != null) {
        "(부모 ${parentFreq}회·아이 ${kidFreq}회)"
    } else {
        noDataMessage
    }

    val feedbackText = summary.overallFeedback ?: noDataMessage

    BaseBox {
        // 왼쪽 라벨
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp, 20.dp, 10.dp, bottom = 20.dp)
        ) {
            Text("나의 감정", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF460B0B))
            Spacer(Modifier.height(18.dp))
            Text("발화 비율", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF460B0B))
            Spacer(Modifier.height(18.dp))
            Text("대화 상황", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF460B0B))
        }

        // 오른쪽 내용
        Column(
            modifier = Modifier
                .padding(start = 130.dp, top = 20.dp, end = 16.dp)
        ) {
            // 감정
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .background(Color(0xE9FFD1C4), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(emotionLabel, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(18.dp))

            // 발화 비율
            Row(verticalAlignment = Alignment.Bottom) {
                Box(
                    Modifier
                        .background(Color(0xFFFFB9D1), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(talkBalanceLabel, fontSize = 12.sp)
                }

                Spacer(Modifier.width(4.dp))

                Text(
                    text = freqText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(Modifier.height(18.dp))

            // 대화 상황 텍스트
            Text(
                text = feedbackText,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}


@Composable
private fun VoiceUploadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF6ED)),
            modifier = Modifier
                .padding(horizontal = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFFF9B6A),
                    strokeWidth = 3.dp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "아이와의 대화를 분석 중이에요.\n영상 길이에 따라 1~2분 정도 걸릴 수 있어요.",
                    fontSize = 14.sp,
                    color = Color(0xFF4A3424),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
