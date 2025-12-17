package capstone.workbook.service;

import capstone.home.entity.UserProfile;
import capstone.home.repository.UserProfileRepository;
import capstone.user.entity.User;
import capstone.user.repository.UserRepository;
import capstone.workbook.dto.*;
import capstone.workbook.entity.Dialogue;
import capstone.workbook.entity.Simulation;
import capstone.workbook.entity.Workbook;
import capstone.workbook.entity.WorkbookTheory;
import capstone.workbook.repository.SimulationRepository;
import capstone.workbook.repository.WorkbookRepository;
import capstone.workbook.repository.WorkbookTheoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import capstone.common.error.AppException;
import capstone.common.error.ErrorCode;

@Service
@RequiredArgsConstructor
public class WorkbookService {

    private final ObjectMapper objectMapper;
    private final WorkbookPythonClient workbookPythonClient;
    private final WorkbookRepository workbookRepository;
    private final SimulationRepository simulationRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final WorkbookTheoryRepository workbookTheoryRepository;

    // 회원 가입 -> 워크북 생성
    @Transactional
    public void createCustomWorkbook(int userId) {

        for (int chapterId = 1; chapterId <= 7; chapterId++) {
            for (int lessonId = 1; lessonId <= 4; lessonId++) {

                WorkbookDto w = workbookPythonClient.createWorkbook(userId, chapterId, lessonId);

                String firstOptionsJson = null;
                if (w.getFirstSelectiveOptions() != null) {
                    try {
                        firstOptionsJson = objectMapper.writeValueAsString(w.getFirstSelectiveOptions());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("선택지(selectiveOptions) 직렬화 중 오류가 발생했습니다.", e);
                    }
                }
                String secondOptionsJson = null;
                if (w.getSecondSelectiveOptions() != null) {
                    try {
                        secondOptionsJson = objectMapper.writeValueAsString(w.getSecondSelectiveOptions());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("선택지(selectiveOptions) 직렬화 중 오류가 발생했습니다.", e);
                    }
                }
                String thirdOptionsJson = null;
                if (w.getThirdSelectiveOptions() != null) {
                    try {
                        thirdOptionsJson = objectMapper.writeValueAsString(w.getThirdSelectiveOptions());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("선택지(selectiveOptions) 직렬화 중 오류가 발생했습니다.", e);
                    }
                }

                Workbook workbook = Workbook.builder()
                        .done(0)
                        .userId(userId)
                        .chapterId(chapterId)
                        .chapterTitle(w.getChapterTitle())
                        .lessonId(lessonId)
                        .lessonTitle(w.getLessonTitle())
                        .firstDescriptiveFormQuestion(w.getFirstDescriptiveFormQuestion())
                        .firstDescriptiveFormExample(w.getFirstDescriptiveFormExample())
                        .secondDescriptiveFormQuestion(w.getSecondDescriptiveFormQuestion())
                        .secondDescriptiveFormExample(w.getSecondDescriptiveFormExample())
                        .firstSelectiveQuestion(w.getFirstSelectiveQuestion())
                        .firstSelectiveOptions(firstOptionsJson)
                        .firstSelectiveExample(w.getFirstSelectiveExample())
                        .secondSelectiveQuestion(w.getSecondSelectiveQuestion())
                        .secondSelectiveOptions(secondOptionsJson)
                        .secondSelectiveExample(w.getSecondSelectiveExample())
                        .thirdSelectiveQuestion(w.getThirdSelectiveQuestion())
                        .thirdSelectiveOptions(thirdOptionsJson)
                        .thirdSelectiveExample(w.getThirdSelectiveExample())
                        .build();
                workbookRepository.save(workbook);
            }
        }
    }

    // 회원 가입 -> 시뮬레이션 생성
    @Transactional
    public void createCustomSimulations(int userId) {

        for (int chapterId = 1; chapterId <= 7; chapterId++) {
            int lessonId = 5;

            List<DialogueDto> dialogues = null;

            SimulationPythonResponseDto s = workbookPythonClient.createSimulation(userId, chapterId, lessonId, dialogues);
            Dialogue firstAiLine = Dialogue.builder()
                    .userLine(null)
                    .aiLine(s.getAiLine())
                    .build();

            List<Dialogue> dialogueList = new ArrayList<>();
            dialogueList.add(firstAiLine);

            Simulation simulation = Simulation.builder()
                    .done(0)
                    .userId(userId)
                    .chapterId(chapterId)
                    .chapterTitle(s.getChapterTitle())
                    .lessonId(lessonId)
                    .lessonTitle(s.getLessonTitle())
                    .simulationSituationExplain(s.getSimulationSituationExplain())
                    .dialogues(dialogueList)
                    .build();

            simulationRepository.save(simulation);
        }
    }


    // 현재 Chapter 이동
    @Transactional
    public ChapterIdDto getChapter(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "해당 유저가 존재하지 않습니다. userId=" + userId));
        int nowChapter = user.getNowChapter();
        return new ChapterIdDto(nowChapter);
    }

    // Chapter Theory
    @Transactional(readOnly = true)
    public TheoryDto getChapterTheory(int chapterId, int userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "해당 유저가 존재하지 않습니다. userId=" + userId));

        WorkbookTheory theory = workbookTheoryRepository.findById(chapterId).orElseThrow(() ->
                        new IllegalArgumentException("해당 챕터 이론이 존재하지 않습니다. chapterId=" + chapterId));

        Workbook anyWorkbook = workbookRepository
                .findTopByUserIdAndChapterIdOrderByLessonIdAsc(userId, chapterId);

        if (anyWorkbook == null) {
            throw new AppException(
                    ErrorCode.WB_NOT_FOUND,
                    "해당 유저의 워크북이 없습니다. userId=" + userId + ", chapterId=" + chapterId
            );
        }

        String chapterTitle = anyWorkbook.getChapterTitle();

        return TheoryDto.builder()
                .chapterTitle(chapterTitle)
                .necessity(theory.getNecessity())
                .studyGoal(theory.getStudyGoal())
                .notion(theory.getNotion())
                .build();
    }

    // Chapter 페이지 조회
    @Transactional(readOnly = true)
    public WorkbookListResponse getChapterPageContent(int userId, int chapterId) {

        // workbook
        List<LessonDto> lessons = new ArrayList<>(workbookRepository.findLessons(userId, chapterId));

        // simulation
        Simulation simulation = simulationRepository
                .findByUserIdAndChapterIdAndLessonId(userId, chapterId, 5)
                .orElse(null);
        if (simulation != null) {
            LessonDto simLessonDto = LessonDto.builder()
                    .lessonId(simulation.getLessonId())
                    .lessonTitle(simulation.getLessonTitle())
                    .progressStatus(simulation.getDone())
                    .build();

            lessons.add(simLessonDto);
        }

        // lessonId 기준 정렬
        lessons.sort(Comparator.comparingInt(LessonDto::getLessonId));

        // chapterTitle
        Workbook anyWorkbook = workbookRepository
                .findTopByUserIdAndChapterIdOrderByLessonIdAsc(userId, chapterId);
        if (anyWorkbook == null) {
            throw new AppException(ErrorCode.WB_NOT_FOUND, "해당 유저의 워크북이 없습니다. userId=" + userId + ", chapterId=" + chapterId);
        }
        String chapterTitle = anyWorkbook.getChapterTitle();

        // chapterProgress
        int chapterProgress = (int) lessons.stream()
                .filter(l -> l.getProgressStatus() == 1)
                .count();

        return WorkbookListResponse.builder()
                .chapterTitle(chapterTitle)
                .chapterProgress(chapterProgress)
                .lessons(lessons)
                .totalPages(1)
                .size(lessons.size())
                .build();
    }


    // 워크북 내용 GET(01-04)
    @Transactional(readOnly = true)
    public WorkbookDto getWorkbookContent(int userId, int chapterId, int lessonId) {

        Workbook workbook = workbookRepository
                .findByUserIdAndChapterIdAndLessonId(userId, chapterId, lessonId);
        if (workbook == null) {
            throw new AppException(ErrorCode.WB_NOT_FOUND, "해당 워크북이 존재하지 않습니다. userId=%d, chapterId=%d, lessonId=%d"
                            .formatted(userId, chapterId, lessonId));
        }

        List<String> firstOptions = null;
        if (workbook.getFirstSelectiveOptions() != null) {
            try {
                firstOptions = objectMapper.readValue(
                        workbook.getFirstSelectiveOptions(),
                        new TypeReference<List<String>>() {}
                );
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.PARSE_ERROR, "선택지(selectiveOptions) 파싱 중 오류가 발생했습니다.", null, e);
            }
        }

        List<String> secondOptions = null;
        if (workbook.getSecondSelectiveOptions() != null) {
            try {
                secondOptions = objectMapper.readValue(
                        workbook.getSecondSelectiveOptions(),
                        new TypeReference<List<String>>() {}
                );
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.PARSE_ERROR, "선택지(selectiveOptions) 파싱 중 오류가 발생했습니다.", null, e);
            }
        }

        List<String> thirdOptions = null;
        if (workbook.getThirdSelectiveOptions() != null) {
            try {
                thirdOptions = objectMapper.readValue(
                        workbook.getThirdSelectiveOptions(),
                        new TypeReference<List<String>>() {}
                );
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.PARSE_ERROR, "선택지(selectiveOptions) 파싱 중 오류가 발생했습니다.", null, e);
            }
        }

        return WorkbookDto.builder()
                .chapterTitle(workbook.getChapterTitle())
                .lessonTitle(workbook.getLessonTitle())
                .firstDescriptiveFormQuestion(workbook.getFirstDescriptiveFormQuestion())
                .firstDescriptiveFormExample(workbook.getFirstDescriptiveFormExample())
                .secondDescriptiveFormQuestion(workbook.getSecondDescriptiveFormQuestion())
                .secondDescriptiveFormExample(workbook.getSecondDescriptiveFormExample())
                .firstSelectiveQuestion(workbook.getFirstSelectiveQuestion())
                .firstSelectiveOptions(firstOptions)
                .firstSelectiveExample(workbook.getFirstSelectiveExample())
                .secondSelectiveQuestion(workbook.getSecondSelectiveQuestion())
                .secondSelectiveOptions(secondOptions)
                .secondSelectiveExample(workbook.getSecondSelectiveExample())
                .thirdSelectiveQuestion(workbook.getThirdSelectiveQuestion())
                .thirdSelectiveOptions(thirdOptions)
                .thirdSelectiveExample(workbook.getThirdSelectiveExample())
                .build();
    }

    // 워크북 내용 POST(01-04)
    @Transactional
    public void postWorkbookContent(int userId, int chapterId, int lessonId, WorkbookAnswerDto answerDto) {
        Workbook workbook = workbookRepository
                .findByUserIdAndChapterIdAndLessonId(userId, chapterId, lessonId);
        if (workbook == null) {
            throw new AppException(
                    ErrorCode.WB_NOT_FOUND, "해당 워크북이 존재하지 않습니다. userId=%d, chapterId=%d, lessonId=%d".formatted(userId, chapterId, lessonId));
        }

        workbook.setFirstDescriptiveFormAnswer(answerDto.getFirstDescriptiveFormAnswer());
        workbook.setSecondDescriptiveFormAnswer(answerDto.getSecondDescriptiveFormAnswer());
        workbook.setFirstSelectiveAnswer(answerDto.getFirstSelectiveAnswer());
        workbook.setSecondSelectiveAnswer(answerDto.getSecondSelectiveAnswer());
        workbook.setThirdSelectiveAnswer(answerDto.getThirdSelectiveAnswer());

        List<String> firstOptions = null;
        if (workbook.getFirstSelectiveOptions() != null) {
            try {
                firstOptions = objectMapper.readValue(
                        workbook.getFirstSelectiveOptions(),
                        new TypeReference<List<String>>() {}
                );
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.PARSE_ERROR, "선택지(selectiveOptions) 파싱 중 오류가 발생했습니다.", null, e);
            }
        }
        List<String> secondOptions = null;
        if (workbook.getSecondSelectiveOptions() != null) {
            try {
                secondOptions = objectMapper.readValue(
                        workbook.getSecondSelectiveOptions(),
                        new TypeReference<List<String>>() {}
                );
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.PARSE_ERROR, "선택지(selectiveOptions) 파싱 중 오류가 발생했습니다.", null, e);
            }
        }
        List<String> thirdOptions = null;
        if (workbook.getThirdSelectiveOptions() != null) {
            try {
                thirdOptions = objectMapper.readValue(
                        workbook.getThirdSelectiveOptions(),
                        new TypeReference<List<String>>() {}
                );
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.PARSE_ERROR, "선택지(selectiveOptions) 파싱 중 오류가 발생했습니다.", null, e);
            }
        }

        // Python feedback
        WorkbookDto dto = WorkbookDto.builder()
                .firstDescriptiveFormQuestion(workbook.getFirstDescriptiveFormQuestion())
                .firstDescriptiveFormAnswer(answerDto.getFirstDescriptiveFormAnswer())
                .firstDescriptiveFormExample(workbook.getFirstDescriptiveFormExample())

                .secondDescriptiveFormQuestion(workbook.getSecondDescriptiveFormQuestion())
                .secondDescriptiveFormAnswer(answerDto.getSecondDescriptiveFormAnswer())
                .secondDescriptiveFormExample(workbook.getSecondDescriptiveFormExample())

                .firstSelectiveQuestion(workbook.getFirstSelectiveQuestion())
                .firstSelectiveOptions(firstOptions)
                .firstSelectiveAnswer(answerDto.getFirstSelectiveAnswer())
                .firstSelectiveExample(workbook.getFirstSelectiveExample())

                .secondSelectiveQuestion(workbook.getSecondSelectiveQuestion())
                .secondSelectiveOptions(secondOptions)
                .secondSelectiveAnswer(answerDto.getSecondSelectiveAnswer())
                .secondSelectiveExample(workbook.getSecondSelectiveExample())

                .thirdSelectiveQuestion(workbook.getThirdSelectiveQuestion())
                .thirdSelectiveOptions(thirdOptions)
                .thirdSelectiveAnswer(answerDto.getThirdSelectiveAnswer())
                .thirdSelectiveExample(workbook.getThirdSelectiveExample())

                .build();
        String feedback = workbookPythonClient.createWorkbookFeedback(userId, dto);
        workbook.setWorkbookFeedback(feedback);
    }


    // 워크북 피드백 GET(01-04)
    @Transactional
    public WorkbookFeedbackDto getWorkbookFeedback(int userId, int chapterId, int lessonId) {
        Workbook workbook = workbookRepository
                .findByUserIdAndChapterIdAndLessonId(userId, chapterId, lessonId);
        if (workbook == null) {
            throw new AppException(ErrorCode.WB_NOT_FOUND, "해당 워크북이 존재하지 않습니다. userId=%d, chapterId=%d, lessonId=%d"
                            .formatted(userId, chapterId, lessonId));
        }
        workbook.setDone(1);
        String feedback = workbook.getWorkbookFeedback();
        UserProfile p = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "해당 유저 프로필이 존재하지 않습니다. userId=" + userId));
        p.setFinishedWorkbookOnce(1);
        int wFrequency = p.getWorkBookFrequency() + 1;
        p.setWorkBookFrequency(wFrequency);
        int nowPoints = p.getPoints();
        p.setPoints(nowPoints+4);

        return WorkbookFeedbackDto.builder()
                .workbookFeedback(feedback)
                .build();
    }

    // SIMULATION 내용 GET(05)
    @Transactional(readOnly = true)
    public SimulationDto getSimulationContent(int userId, int chapterId) {
        Simulation simulation = simulationRepository
                .findByUserIdAndChapterIdAndLessonId(userId, chapterId, 5)
                .orElseThrow(() -> new AppException(
                        ErrorCode.WB_NOT_FOUND, "해당 시뮬레이션이 존재하지 않습니다. userId=%d, chapterId=%d"
                                .formatted(userId, chapterId)));

        return SimulationDto.builder()
                .chapterTitle(simulation.getChapterTitle())
                .lessonTitle(simulation.getLessonTitle())
                .simulationSituationExplain(simulation.getSimulationSituationExplain())
                .dialogues(simulation.getDialogues())
                .build();
    }


    // SIMULATION 다음 대사 GET
    @Transactional
    public NextLineDto getSimulationNextLine(int userId, int chapterId, Dialogues dialogues) {
        Simulation simulation = simulationRepository
                .findByUserIdAndChapterIdAndLessonId(userId, chapterId, 5)
                .orElseThrow(() -> new AppException(ErrorCode.WB_NOT_FOUND, "해당 시뮬레이션이 존재하지 않습니다. userId=%d, chapterId=%d"
                                .formatted(userId, chapterId)));



        List<DialogueDto> dialogueDtos = dialogues.getDialogues();
        if (dialogueDtos == null || dialogueDtos.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "대화 목록이 비어 있습니다.");
        }


        SimulationPythonResponseDto s = workbookPythonClient.createSimulation(userId, chapterId, 5, dialogueDtos);
        String nextLine = s.getAiLine();

        DialogueDto lastDto = dialogueDtos.get(dialogueDtos.size() - 1);
        Dialogue newDialogue = Dialogue.builder()
                .userLine(lastDto.getUserLine())
                .aiLine(nextLine)
                .build();
        List<Dialogue> existingDialogues = simulation.getDialogues();
        if (existingDialogues == null) {
            existingDialogues = new ArrayList<>();
        }
        existingDialogues.add(newDialogue);
        simulation.setDialogues(existingDialogues);

        return NextLineDto.builder()
                .nextLine(nextLine)
                .build();
    }


    // SIMULATION 피드백 GET
    @Transactional
    public SimulationFeedbackDto getSimulationFeedback(int userId, int chapterId) {
        // 1. 시뮬레이션 찾기
        Simulation simulation = simulationRepository
                .findByUserIdAndChapterIdAndLessonId(userId, chapterId, 5)
                .orElseThrow(() -> new AppException(
                        ErrorCode.WB_NOT_FOUND, "해당 시뮬레이션이 존재하지 않습니다. userId=%d, chapterId=%d"
                                .formatted(userId, chapterId)));

        // 2. 전체 대화 가져오기
        List<Dialogue> dialogueList = simulation.getDialogues();
        if (dialogueList == null || dialogueList.isEmpty()) {
            throw new AppException(ErrorCode.WB_CONFLICT_STATE, "시뮬레이션 대화가 비어있습니다.");
        }


        // 🔥 3. userLine 이 null 이거나 공백인 턴은 제외 (아이만 말한 턴 등)
        List<DialogueDto> dialogueDtoList = dialogueList.stream()
                .filter(Objects::nonNull)
                .filter(d -> d.getUserLine() != null && !d.getUserLine().isBlank())
                .map(d -> DialogueDto.builder()
                        .userLine(d.getUserLine())
                        .aiLine(d.getAiLine())
                        .build()
                )
                .toList();

        if (dialogueDtoList.isEmpty()) {
            throw new AppException(ErrorCode.WB_CONFLICT_STATE, "부모 발화가 없어 시뮬레이션 피드백을 생성할 수 없습니다.");
        }

        // 4. python 호출
        String feedback = workbookPythonClient.createSimulationFeedback(userId, dialogueDtoList);
        simulation.setSimulationFeedback(feedback);
        simulation.setDone(1);

        // 5. 워크북/유저 진도/빈도 업데이트
        UserProfile p = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.USER_NOT_FOUND, "해당 유저 프로필이 존재하지 않습니다. userId=" + userId));

        int wFrequency = p.getWorkBookFrequency() + 1;
        p.setWorkBookFrequency(wFrequency);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.USER_NOT_FOUND, "해당 유저가 존재하지 않습니다. userId=" + userId));
        int updateChapter = user.getNowChapter() + 1;
        user.setNowChapter(updateChapter);
        int nowPoints = p.getPoints();
        p.setPoints(nowPoints+4);

        return SimulationFeedbackDto.builder()
                .simulationFeedback(feedback)
                .build();
    }


}
