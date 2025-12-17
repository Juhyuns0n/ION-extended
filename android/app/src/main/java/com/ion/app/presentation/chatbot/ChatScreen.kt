package com.ion.app.presentation.chatbot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ion.app.R
import com.ion.app.domain.model.chatbot.ChatMessage
import com.ion.app.presentation.chatbot.navigation.ChatbotViewModel

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    ChatScreen(
        uiState = ChatUiState(
            messages = listOf(
                ChatMessage(id = 1, text = "아이가 요즘 잠을 못자요", isUser = true),
                ChatMessage(id = 2, text = "도담이 네 살이고, 어두움을 무서워해 걱정되시죠. 선택지 두 가지: A) 손전등 놀이 n하루 5분씩.  빛을 조금씩 줄여요;  B) 미니도전  관찰→함께→혼자 한 단계씩 작은 성공 쌓기. 1-2주  꾸준히 시도해보시고, 필요하면 전문가 상담을 고려하세요; 문헌 참고한 팁입니다.", isUser = false)
            ),
            input = " "
        ),
        topPadding = 0.dp,
        onInputChange = {},
        onSend = {}
    )
}

@Composable
fun ChatRoute(
    topPadding: Dp = 0.dp,
    bottomPadding: Dp = 0.dp,
    viewModel: ChatbotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 이 Composable이 dispose될 때(= 화면 나갈 때) 세션 종료
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            viewModel.closeChatSession()
        }
    }

    ChatScreen(
        uiState = uiState,
        onInputChange = viewModel::updateInput,
        onSend = viewModel::send,
        topPadding = topPadding,
    )
}


@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    topPadding: Dp
) {
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    val inputEnabled = !uiState.isLoading && !uiState.isSending
    val isThinking = uiState.isLoading || uiState.isSending

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding)
    ) {
        Image(
            painter = painterResource(R.drawable.bg_chat),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // 메시지 리스트 (채팅 내용)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = 12.dp,
                    bottom = 12.dp
                )
            ) {
                items(uiState.messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg)
                    Spacer(Modifier.height(8.dp))
                }
            }

            // 아래 입력바 (IME 위에 딱 붙게)
            ChatInputBar(
                value = uiState.input,
                onValueChange = onInputChange,
                onSend = onSend,
                enabled = inputEnabled,
                isLoading = isThinking,
                modifier = Modifier.fillMaxWidth()   // imePadding은 Column에서 처리
            )
        }
    }
}


@Composable
fun ChatBubble(message: ChatMessage) {
    val maxW = (LocalConfiguration.current.screenWidthDp * 0.72f).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier.widthIn(max = maxW)
        ) {
            Image(
                painter = painterResource(
                    if (message.isUser) R.drawable.bubble_user else R.drawable.bubble_bot
                ),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize()
            )

            Text(
                text = message.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (message.isUser) Color.White else Color(0xFF333333),
                softWrap = true,
                overflow = TextOverflow.Clip,
                maxLines = Int.MAX_VALUE,
                modifier = Modifier.padding(
                    start = if (message.isUser) 14.dp else 62.dp,
                    end   = if (message.isUser) 28.dp else 26.dp,
                    top   = if (message.isUser) 6.dp else 20.dp,
                    bottom = if (message.isUser) 10.dp else 50.dp
                )
            )
        }
    }
}

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    placeholderText: String = "질문을 입력해주세요."
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val fieldBg = Color.White.copy(alpha = if (enabled) 0.95f else 0.7f)
    val borderColor = Color(0xFFFFBA8C).copy(alpha = if (enabled) 1f else 0.6f)

    Surface(
        modifier = modifier,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = Color(0xFFFFBA8C)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 40.dp),
        ) {
            // ====== 로딩 상태 표시 ======
            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "답변을 입력 중입니다...",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = fieldBg,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                ) {
                    val blockTouch = if (enabled) Modifier else
                        Modifier
                            .focusable(false)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) { awaitPointerEvent() }
                                }
                            }

                    BasicTextField(
                        value = value,
                        onValueChange = { if (enabled) onValueChange(it) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        singleLine = true,
                        readOnly = !enabled,
                        cursorBrush = SolidColor(
                            if (enabled) Color(0xFF333333) else Color.Transparent
                        ),
                        interactionSource = interactionSource,
                        modifier = blockTouch.fillMaxSize(),
                        decorationBox = { inner ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val showPlaceholder =
                                    value.isEmpty() && (!isFocused || !enabled)

                                if (showPlaceholder) {
                                    val placeholder = when {
                                        isLoading ->
                                            "답변을 입력 중입니다..."
                                        !enabled ->
                                            "메시지를 입력할 수 없습니다."
                                        else -> placeholderText
                                    }

                                    Text(
                                        text = placeholder,
                                        color = Color(0xFFA9A7A7),
                                        fontSize = 14.sp
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }

                Spacer(Modifier.width(10.dp))

                Surface(
                    onClick = { if (enabled && value.isNotBlank()) onSend() },
                    enabled = enabled,
                    shape = CircleShape,
                    color = Color(0xFFFFBA8C),
                    shadowElevation = 0.dp,
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sendchat),
                            contentDescription = "send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}


