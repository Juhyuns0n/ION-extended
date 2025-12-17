package capstone.chatbot.service;

import capstone.chatbot.dto.AnswerDto;
import capstone.chatbot.dto.QuestionAnswerDto;
import capstone.common.error.AppException;
import capstone.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class ChatbotPythonClient {

    private final @Qualifier("chatbotWebClient") WebClient chatbotWebClient;

    public AnswerDto ask(int userId, Integer sessionId, List<QuestionAnswerDto> history) {
        if (history == null || history.isEmpty()) {
            throw new AppException(
                    ErrorCode.BAD_REQUEST,
                    "questions history must not be empty"
            );
        }

        try {
            return chatbotWebClient.post()
                    .uri("/api/chatbot")
                    .header("userId", String.valueOf(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(
                            Map.of(
                                    "sessionId", sessionId,
                                    "questions", history
                            )
                    )
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::map4xx)
                    .onStatus(HttpStatusCode::is5xxServerError, this::map5xx)
                    .bodyToMono(AnswerDto.class)
                    .timeout(Duration.ofSeconds(300))
                    .onErrorMap(TimeoutException.class, e ->
                            new AppException(
                                    ErrorCode.GATEWAY_TIMEOUT,
                                    "챗봇 서버 응답 시간 초과입니다.",
                                    "userId=" + userId + ", sessionId=" + sessionId,
                                    e
                            )
                    )
                    .block();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(
                    ErrorCode.UPSTREAM_ERROR,
                    "챗봇 서버 호출 중 예기치 못한 오류가 발생했습니다.",
                    "userId=" + userId + ", sessionId=" + sessionId + ", cause=" + e.getClass().getName(),
                    e
            );
        }
    }

    private Mono<? extends Throwable> map4xx(ClientResponse resp) {
        return resp.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> new AppException(
                        ErrorCode.UPSTREAM_BAD_REQUEST,
                        "챗봇 서버에 잘못된 요청입니다.",
                        "status=" + resp.statusCode().value() + ", body=" + body
                ));
    }

    private Mono<? extends Throwable> map5xx(ClientResponse resp) {
        return resp.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> new AppException(
                        ErrorCode.UPSTREAM_ERROR,
                        "챗봇 서버 내부 오류가 발생했습니다.",
                        "status=" + resp.statusCode().value() + ", body=" + body
                ));
    }
}

