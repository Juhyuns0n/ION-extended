package com.ion.app.presentation.chatbot

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ion.app.R
import com.ion.app.domain.model.chatbot.ChatMessage
import com.ion.app.domain.model.chatbot.ChatSession
import com.ion.app.presentation.chatbot.navigation.ChatHistoryViewModel

//@Preview(showBackground = true, name = "ChatScreen - 기본 상태")
//@Composable
//fun ChatScreenDefaultPreview() {
//    ChatHistoryScreen(
//        uiState = ChatUiState(
//            messages = listOf(
//                ChatMessage(
//                    id = 1,
//                    text = "아이가 요즘 잠을 잘 못 자요.",
//                    isUser = true
//                ),
//                ChatMessage(
//                    id = 2,
//                    text = "아이가 잠을 못 자면 많이 걱정되시겠어요. 먼저 규칙적인 취침 루틴(목욕, 조용한 책 읽기 등)과 일정한 기상 시간을 정해 주고, 취침 1시간 전부터는 화면·단 음식·카페인 음료를 피하는 것이 좋아요.",
//                    isUser = false
//                ),
//                        ChatMessage(
//                        id = 4,
//                text = "아이가 친구랑 자꾸 싸워요. 어떻게 도와주면 좋을까요?",
//                isUser = true
//            ),
//            ChatMessage(
//                id = 5,
//                text = "그럴 때는 먼저 아이의 감정을 충분히 들어주는 것이 중요해요. 누가 잘못했다를 따지기보다, 아이 입장에서 어떤 기분이었는지 말하게 해 주세요.",
//                isUser = false
//            ),
//            ChatMessage(
//                id = 6,
//                text = "이번엔 동생이랑도 자꾸 다투는데, 어떻게 설명해야 할지 모르겠어요.",
//                isUser = true
//            ),
//                ChatMessage(
//                    id = 7,
//                    text = "형제 갈등이 잦으면 부모님도 많이 고민되실 것 같아요.  \n" +
//                            "먼저 아이에게 “어떤 기분이었는지”를 물어 감정을 표현하도록 돕고,  \n" +
//                            "“화가 나도 때리기보단 말로 알려줄 수 있어.”처럼 명확한 행동 기준을 제시해주세요.  \n" +
//                            "또 잘 표현했을 때는 바로 칭찬해 주면 갈등 빈도를 줄이는 데 큰 도움이 됩니다.\n",
//                    isUser = false
//                )
//
//            ),
//
//            input = "",
//            isLoading = false,
//            isSending = false
//        ),
//        topPadding = 0.dp
//    )
//}
@Composable
fun ChatHistoryRoute(
    topPadding: Dp = 0.dp,
    bottomPadding: Dp = 0.dp,
    viewModel: ChatHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatHistoryScreen(
        uiState = uiState,
        topPadding = topPadding,
    )
}

@Composable
fun ChatHistoryScreen(
    uiState: ChatHistoryUiState,
    topPadding: Dp
) {
    val listState = rememberLazyListState()

    // 오래된 세션 → 최신 세션 (sessionId 기준 정렬)
    val orderedSessions: List<ChatSession> =
        uiState.sessions.sortedBy { it.sessionId }

    // 화면 들어오면 / 리스트 변경되면 맨 아래로 스크롤
    LaunchedEffect(orderedSessions.size) {
        val total = listState.layoutInfo.totalItemsCount
        if (total > 0) {
            listState.scrollToItem(total - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding, bottom = 0.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.bg_chat),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 84.dp),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 🔹 세션 단위로 그리기 + 세션 사이 Divider
            itemsIndexed(
                items = orderedSessions,
                key = { _, session -> session.sessionId }
            ) { index, session ->

                // 세션 안의 Q/A를 말풍선으로 그대로 사용
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    session.questions.forEach { qa ->
                        // 부모 질문
                        ChatBubble(
                            message = ChatMessage(
                                id = ("q_${session.sessionId}_${qa.hashCode()}").hashCode().toLong(),
                                text = qa.question,
                                isUser = true
                            )
                        )
                        // 챗봇 답변
                        ChatBubble(
                            message = ChatMessage(
                                id = ("a_${session.sessionId}_${qa.hashCode()}").hashCode().toLong(),
                                text = qa.answer,
                                isUser = false
                            )
                        )
                    }
                }

                // 마지막 세션이 아니면 Divider 추가
                if (index < orderedSessions.lastIndex) {
                    Spacer(Modifier.height(4.dp))
                    Divider(
                        color = Color.White.copy(alpha = 0.4f),
                        thickness = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        Column {
            Spacer(Modifier.weight(1f))   // 빈 공간 → 자동으로 남는 자리 채움
            ChatInputBar(
                value = "",
                onValueChange = {},   // 사용 안 함
                onSend = {},          // 사용 안 함
                enabled = false,
                placeholderText = "메시지를 입력할 수 없습니다.",
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}
