package com.example.tomyongji;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.global.error.CustomException;
import com.example.tomyongji.qna.dto.request.AnswerSaveDto;
import com.example.tomyongji.qna.dto.request.QuestionSaveDto;
import com.example.tomyongji.qna.entity.QnaAnswer;
import com.example.tomyongji.qna.entity.QnaQuestion;
import com.example.tomyongji.qna.mapper.QnaMapper;
import com.example.tomyongji.qna.repository.QnaAnswerRepository;
import com.example.tomyongji.qna.repository.QnaQuestionRepository;
import com.example.tomyongji.qna.service.QnaQuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static com.example.tomyongji.global.error.ErrorMsg.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QnaServiceTest {

    @InjectMocks
    private QnaQuestionService questionService;

    @Mock private QnaQuestionRepository questionRepository;
    @Mock private QnaAnswerRepository answerRepository;
    @Mock private UserRepository userRepository;
    @Mock private QnaMapper qnaMapper;

    private final String loginUserId = "testUser";

    @Test
    @DisplayName("질문 등록 실패 - 존재하지 않는 유저")
    void createQuestion_Fail_Unauthorized() {
        // Given
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.createQuestion(new QuestionSaveDto(), loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_USER);
        verify(userRepository).findByUserId(loginUserId);
        verify(qnaMapper, never()).toAnswerEntity(any());
    }

    @Test
    @DisplayName("답변 등록 실패 - 존재하지 않은 유저")
    void createAnswer_Fail_Unauthorized() {
        // Given
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.createAnswer(1L, new AnswerSaveDto(), loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_USER);
        verify(userRepository).findByUserId(loginUserId);
        verify(questionRepository, never()).findById(any());
    }

    @Test
    @DisplayName("답변 등록 실패 - 질문글이 존재하지 않음")
    void createAnswer_Fail_QuestionNotFound() {
        // Given
        User user = User.builder().userId(loginUserId).build();
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(user));
        when(questionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.createAnswer(1L, new AnswerSaveDto(), loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_FOUND_QNAQUESTION);
        verify(userRepository).findByUserId(loginUserId);
        verify(questionRepository).findById(1L);
        verify(qnaMapper, never()).toAnswerEntity(any());
    }

    @Test
    @DisplayName("답변 등록 실패 - 어드민 소속이 아닌 유저")
    void createAnswer_Fail_NotAdminClub() {
        // Given
        StudentClub normalClub = StudentClub.builder().studentClubName("일반학생회").build();
        User user = User.builder().userId(loginUserId).studentClub(normalClub).build();
        QnaQuestion question = QnaQuestion.builder().id(1L).build();

        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(user));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        // When & Then
        assertThatThrownBy(() -> questionService.createAnswer(1L, new AnswerSaveDto(), loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_BELONGING);
        verify(userRepository).findByUserId(loginUserId);
        verify(questionRepository).findById(1L);
        verify(qnaMapper, never()).toAnswerEntity(any());
    }

    @Test
    @DisplayName("질문 등록 성공")
    void createQuestion_Success() {
        // Given
        User user = User.builder().userId(loginUserId).build();
        QuestionSaveDto dto = QuestionSaveDto.builder()
                .title("제목")
                .content("내용")
                .build();

        QnaQuestion question = QnaQuestion.builder()
                .id(1L)
                .title("질문 제목")
                .content("질문 내용")
                .answers(new ArrayList<>())
                .build();

        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(user));
        when(qnaMapper.toQuestionEntity(dto)).thenReturn(question);
        when(questionRepository.save(question)).thenReturn(question);

        // When
        QnaQuestion result = questionService.createQuestion(dto, loginUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("질문 제목");
        assertThat(result.getContent()).isEqualTo("질문 내용");
        assertThat(result.getWriter()).isEqualTo(user);
        verify(qnaMapper).toQuestionEntity(dto);
        verify(questionRepository).save(question);
    }

    @Test
    @DisplayName("답변 등록 성공 - 질문글 존재 및 어드민 소속 유저")
    void createAnswer_Success() {
        // Given
        StudentClub adminClub = StudentClub.builder().studentClubName("어드민").build();
        User adminUser = User.builder().userId(loginUserId).studentClub(adminClub).build();
        QnaQuestion question = QnaQuestion.builder()
                .id(1L)
                .title("질문 제목")
                .content("질문 내용")
                .answers(new ArrayList<>())
                .build();

        AnswerSaveDto dto = AnswerSaveDto.builder()
                .content("답변입니다")
                .build();

        QnaAnswer answer = QnaAnswer.builder()
                .id(10L)
                .content("답변입니다")
                .question(question)
                .build();

        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(adminUser));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(qnaMapper.toAnswerEntity(dto)).thenReturn(answer);
        when(answerRepository.save(answer)).thenReturn(answer);

        // When
        QnaAnswer result = questionService.createAnswer(1L, dto, loginUserId);

        // Then
        // 양방향 연관관계가 잘 적용되었는지
        assertThat(question.getAnswers().size()).isEqualTo(1);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getContent()).isEqualTo("답변입니다");
        verify(userRepository).findByUserId(loginUserId);
        verify(questionRepository).findById(1L);
        verify(qnaMapper).toAnswerEntity(dto);
        verify(answerRepository).save(answer);
    }

}
