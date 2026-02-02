package com.example.tomyongji;

import com.example.tomyongji.global.error.CustomException;
import com.example.tomyongji.qna.dto.request.AnswerSaveDto;
import com.example.tomyongji.qna.dto.request.QuestionSaveDto;
import com.example.tomyongji.qna.entity.QnaAnswer;
import com.example.tomyongji.qna.entity.QnaQuestion;
import com.example.tomyongji.qna.mapper.QnaMapper;
import com.example.tomyongji.qna.repository.QnaAnswerRepository;
import com.example.tomyongji.qna.repository.QnaQuestionRepository;
import com.example.tomyongji.qna.service.QnaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_QNAQUESTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QnaServiceTest {

    @InjectMocks
    private QnaService qnaService;

    @Mock private QnaQuestionRepository questionRepository;
    @Mock private QnaAnswerRepository answerRepository;
    @Mock private QnaMapper qnaMapper;

    @Test
    @DisplayName("질문글 등록 테스트")
    void createQuestionTest() {
        // Given
        QuestionSaveDto dto = QuestionSaveDto.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        QnaQuestion savedQuestion = QnaQuestion.builder()
                .id(1L)
                .title("테스트 제목")
                .content("테스트 내용")
                .answers(new ArrayList<>())
                .build();

        when(qnaMapper.toQuestionEntity(dto)).thenReturn(savedQuestion);
        when(questionRepository.save(savedQuestion))
                .thenReturn(savedQuestion);

        // When
        QnaQuestion result = qnaService.createQuestion(dto);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("테스트 제목");
        assertThat(result.getContent()).isEqualTo("테스트 내용");
        verify(qnaMapper).toQuestionEntity(dto);
        verify(questionRepository).save(savedQuestion);
    }

    @Test
    @DisplayName("답변글 등록 테스트 - 성공")
    void createAnswerTest() {
        // Given
        QnaQuestion question = QnaQuestion.builder()
                .id(1L)
                .title("질문 제목")
                .content("질문 내용")
                .answers(new ArrayList<>())
                .build();

        AnswerSaveDto dto = AnswerSaveDto.builder()
                .content("답변입니다")
                .build();

        QnaAnswer savedAnswer = QnaAnswer.builder()
                .id(10L)
                .content("답변입니다")
                .question(question)
                .build();

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(qnaMapper.toAnswerEntity(dto)).thenReturn(savedAnswer);
        when(answerRepository.save(savedAnswer)).thenReturn(savedAnswer);

        // When
        QnaAnswer result = qnaService.createAnswer(1L, dto);

        // Then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getContent()).isEqualTo("답변입니다");
        assertThat(result.getQuestion().getId()).isEqualTo(1L);
        verify(questionRepository).findById(1L);
        verify(qnaMapper).toAnswerEntity(dto);
        verify(answerRepository).save(savedAnswer);
    }

    @Test
    @DisplayName("답변 등록 테스트 - 실패 (존재하지 않는 질문)")
    void createAnswerFailTest() {
        // Given
        Long invalidQuestionId = -1L;

        AnswerSaveDto dto = AnswerSaveDto.builder()
                .content("유령 질문에 다는 답변")
                .build();

        when(questionRepository.findById(invalidQuestionId))
                .thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                qnaService.createAnswer(invalidQuestionId, dto)
        );

        assertThat(exception.getErrorCode()).isEqualTo(404);
        assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_QNAQUESTION);
        verify(questionRepository).findById(invalidQuestionId);
        verify(qnaMapper, never()).toAnswerEntity(any());
    }
}
