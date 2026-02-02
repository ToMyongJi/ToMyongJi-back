package com.example.tomyongji;

import com.example.tomyongji.global.error.CustomException;
import com.example.tomyongji.qna.dto.request.AnswerSaveDto;
import com.example.tomyongji.qna.dto.request.QuestionSaveDto;
import com.example.tomyongji.qna.entity.QnaAnswer;
import com.example.tomyongji.qna.entity.QnaQuestion;
import com.example.tomyongji.qna.repository.QnaQuestionRepository;
import com.example.tomyongji.qna.service.QnaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_QNAQUESTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class QnaServiceTest {

    @Autowired private QnaService qnaService;
    @Autowired private QnaQuestionRepository questionRepository;

    @Test
    @DisplayName("질문글 등록 테스트")
    void createQuestionTest() {
        // Given
        QuestionSaveDto dto = QuestionSaveDto.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        // When
        QnaQuestion savedQ = qnaService.createQuestion(dto);

        // Then
        QnaQuestion foundQ = questionRepository.findById(savedQ.getId()).orElseThrow();
        assertEquals("테스트 제목", foundQ.getTitle());
        assertEquals("테스트 내용", foundQ.getContent());
    }

    @Test
    @DisplayName("답변글 등록 테스트 - 성공")
    void createAnswerTest() {
        // Given
        QnaQuestion q = qnaService.createQuestion(QuestionSaveDto.builder()
                .title("질문 제목")
                .content("질문 내용")
                .build());

        AnswerSaveDto aDto = AnswerSaveDto.builder()
                .content("답변입니다")
                .build();

        // When
        QnaAnswer savedA = qnaService.createAnswer(q.getId(), aDto);

        // Then
        assertNotNull(savedA.getId());
        assertEquals("답변입니다", savedA.getContent());
        assertEquals(q.getId(), savedA.getQuestion().getId());
    }

    @Test
    @DisplayName("답변 등록 테스트 - 실패 (존재하지 않는 질문)")
    void createAnswerFailTest() {
        // Given
        Long invalidQuestionId = -1L;
        AnswerSaveDto aDto = AnswerSaveDto.builder()
                .content("유령 질문에 다는 답변")
                .build();

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            qnaService.createAnswer(invalidQuestionId, aDto);
        }, "존재하지 않는 질문에 답변을 달면 CustomException이 발생해야 합니다.");

        assertEquals(404, exception.getErrorCode());
        assertEquals(NOT_FOUND_QNAQUESTION, exception.getMessage());
    }
}
