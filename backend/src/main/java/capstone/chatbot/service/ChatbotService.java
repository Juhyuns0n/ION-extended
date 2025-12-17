package capstone.chatbot.service;

import capstone.chatbot.dto.AnswerDto;
import capstone.chatbot.dto.ChatbotListDto;
import capstone.chatbot.dto.QuestionAnswerDto;
import capstone.chatbot.dto.QuestionDto;
import capstone.chatbot.entity.Chat;
import capstone.chatbot.entity.QuestionAndAnswer;
import capstone.chatbot.repository.ChatRepository;
import capstone.common.error.AppException;
import capstone.common.error.ErrorCode;
import capstone.home.entity.UserProfile;
import capstone.home.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatbotPythonClient chatbotPythonClient;
    private final ChatRepository chatRepository;
    private final UserProfileRepository userProfileRepository;

    //질문 -> 답변
    @Transactional
    public AnswerDto getAnswer(int userId, QuestionDto questionDto) {
        String nowQuestion = questionDto.getQuestion();
        if (nowQuestion == null || nowQuestion.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "질문 내용이 비어 있습니다.");
        }

        // 세션 생성 or 기존 세션 조회
        Chat session;
        if (questionDto.getSessionId() == null) {
            session = Chat.builder()
                    .userId(userId)
                    .build();
            session = chatRepository.save(session);
        } else {session = chatRepository.findBySessionIdAndUserId(questionDto.getSessionId(), userId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "유효하지 않은 세션 id입니다."));
            if (session.isClosed()) {throw new AppException(ErrorCode.FORBIDDEN, "이미 종료된 채팅 세션입니다.");
            }
        }

        // 질문 저장
        QuestionAndAnswer qa = QuestionAndAnswer.builder()
                .question(nowQuestion)
                .build();
        session.getQuestionAndAnswers().add(qa);

        // Question limit
        if (session.getQuestionAndAnswers().size() > 10) {
            String limitMessage =
                    "정확한 답변을 위해 하나의 세션에 10개까지의 질문만 가능합니다. "
                            + "더 많은 조언을 원하시면 새로 채팅을 시작해주세요.";

            qa.setAnswer(limitMessage);
            chatRepository.save(session);

            return AnswerDto.builder()
                    .sessionId(session.getSessionId())
                    .answer(limitMessage)
                    .build();
        }

        // Python 전달 parameter 정리
        List<QuestionAnswerDto> history = session.getQuestionAndAnswers().stream()
                .map(x -> QuestionAnswerDto.builder()
                        .question(x.getQuestion())
                        .answer(x.getAnswer())
                        .build())
                .toList();

        // Python 서버 호출
        AnswerDto bot = chatbotPythonClient.ask(
                userId,
                session.getSessionId(),
                history
        );
        String answer = (bot != null ? bot.getAnswer() : null);

        // 답변 저장
        qa.setAnswer(answer);

        // 세션 저장
        chatRepository.save(session);

        // user profile 관련 처리
        UserProfile p = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "유저 프로필이 존재하지 않습니다. userId=" + userId));

        return AnswerDto.builder()
                .sessionId(session.getSessionId())
                .answer(answer)
                .build();
    }

    // 채팅 기록 조회
    @Transactional(readOnly = true)
    public Page<ChatbotListDto> list(int userId, Pageable pageable) {
        Page<Chat> page = chatRepository
                .findByUserIdOrderBySessionIdDesc(userId, pageable);

        return page.map(session ->
                ChatbotListDto.builder()
                        .sessionId(session.getSessionId())
                        .questions(
                                session.getQuestionAndAnswers().stream()
                                        .map(qa -> QuestionAnswerDto.builder()
                                                .question(qa.getQuestion())
                                                .answer(qa.getAnswer())
                                                .build())
                                        .toList()
                        )
                        .build()
        );
    }

    // 세션 종료
    @Transactional
    public void closeSession(int userId, int sessionId) {
        Chat session = chatRepository
                .findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "유효하지 않은 세션 id입니다."));

        UserProfile p = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "해당 유저 프로필이 존재하지 않습니다. userId=" + userId));
        p.setUsedChatbotOnce(1);
        int cFrequency = p.getChatBotFrequency() + 1;
        p.setChatBotFrequency(cFrequency);
        int nowPoints = p.getPoints();
        p.setPoints(nowPoints+4);

        if (!session.isClosed()) {
            session.setClosed(true);
        }
    }
}
