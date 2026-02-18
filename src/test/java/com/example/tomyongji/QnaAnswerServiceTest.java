package com.example.tomyongji;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.global.error.CustomException;
import com.example.tomyongji.qna.dto.request.AnswerSaveDto;
import com.example.tomyongji.qna.dto.response.AnswerDto;
import com.example.tomyongji.qna.dto.response.PageResponseDto;
import com.example.tomyongji.qna.entity.QnaAnswer;
import com.example.tomyongji.qna.entity.QnaQuestion;
import com.example.tomyongji.qna.mapper.QnaMapper;
import com.example.tomyongji.qna.repository.QnaAnswerRepository;
import com.example.tomyongji.qna.repository.QnaQuestionRepository;
import com.example.tomyongji.qna.service.QnaAnswerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.tomyongji.global.error.ErrorMsg.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QnaAnswerServiceTest {

    @InjectMocks
    private QnaAnswerService answerService;

    @Mock private QnaQuestionRepository questionRepository;
    @Mock private QnaAnswerRepository answerRepository;
    @Mock private UserRepository userRepository;
    @Mock private QnaMapper qnaMapper;

    private final String loginUserId = "adminUser";
    private User adminUser;
    private StudentClub adminClub;
    private QnaQuestion commonQuestion;
    private QnaAnswer commonAnswer;
    private AnswerSaveDto commonSaveDto;
    private AnswerDto commonResponseDto;

    @BeforeEach
    void setUp() {
        adminClub = StudentClub.builder()
                .id(1L)
                .studentClubName("어드민")
                .build();

        adminUser = User.builder()
                .id(1L)
                .userId(loginUserId)
                .studentClub(adminClub)
                .build();

        commonQuestion = QnaQuestion.builder()
                .id(1L)
                .title("질문 제목")
                .content("질문 내용")
                .answers(new ArrayList<>())
                .build();

        commonAnswer = QnaAnswer.builder()
                .id(10L)
                .content("답변 내용")
                .writer(adminClub)
                .question(commonQuestion)
                .build();

        commonSaveDto = AnswerSaveDto.builder()
                .content("답변 내용")
                .build();

        commonResponseDto = AnswerDto.builder()
                .answerId(100L)
                .content("답변 내용입니다.")
                .build();
    }

    @Test
    @DisplayName("답변 등록 성공 - 질문글 존재 및 어드민 소속 유저")
    void createAnswer_Success() {
        // Given
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(adminUser));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(commonQuestion));
        when(qnaMapper.toAnswerEntity(commonSaveDto)).thenReturn(commonAnswer);
        when(answerRepository.save(commonAnswer)).thenReturn(commonAnswer);
        when(qnaMapper.toAnswerDto(commonAnswer)).thenReturn(commonResponseDto);

        // When
        AnswerDto result = answerService.createAnswer(1L, commonSaveDto, loginUserId);

        // Then
        // 양방향 연관관계가 잘 적용되었는지
        assertThat(commonQuestion.getAnswers().size()).isEqualTo(1);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(commonResponseDto);
        verify(userRepository).findByUserId(loginUserId);
        verify(questionRepository).findById(1L);
        verify(qnaMapper).toAnswerEntity(commonSaveDto);
        verify(answerRepository).save(commonAnswer);
    }

    @Test
    @DisplayName("답변 등록 실패 - 존재하지 않은 유저")
    void createAnswer_Fail_Unauthorized() {
        // Given
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerService.createAnswer(1L, commonSaveDto, loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_USER);
        verify(userRepository).findByUserId(loginUserId);
        verify(questionRepository, never()).findById(any());
    }

    @Test
    @DisplayName("답변 등록 실패 - 질문글이 존재하지 않음")
    void createAnswer_Fail_QuestionNotFound() {
        // Given
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(adminUser));
        when(questionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerService.createAnswer(1L, commonSaveDto, loginUserId))
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
        User normalUser = User.builder().userId(loginUserId).studentClub(normalClub).build();
//        QnaQuestion question = QnaQuestion.builder().id(1L).build();

        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(normalUser));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(commonQuestion));

        // When & Then
        assertThatThrownBy(() -> answerService.createAnswer(1L, commonSaveDto, loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_BELONGING);
        verify(userRepository).findByUserId(loginUserId);
        verify(questionRepository).findById(1L);
        verify(qnaMapper, never()).toAnswerEntity(any());
    }

    // 조회 메서드 테스트

    @Test
    @DisplayName("특정 질문의 답변 목록 페이징 조회 성공")
    void findAnswersByQuestionIdPaging_Success() {
        // Given
        Long questionId = commonQuestion.getId();
        int page = 0;
        int size = 5;

        Page<QnaAnswer> answerPage = new PageImpl<>(List.of(commonAnswer), PageRequest.of(page, size), 1);

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(commonQuestion));
        when(answerRepository.findByQuestionId(eq(questionId), any(Pageable.class)))
                .thenReturn(answerPage);
        when(qnaMapper.toAnswerDto(any(QnaAnswer.class))).thenReturn(commonResponseDto);

        // When
        PageResponseDto<AnswerDto> result = answerService.findAnswersByQuestionIdPaging(questionId, page, size);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.pageNo()).isEqualTo(page);
        assertThat(result.pageSize()).isEqualTo(size);
        assertThat(result.content())
                .hasSize(1)
                .extracting(AnswerDto::getAnswerId)
                .containsExactly(commonResponseDto.getAnswerId());

        verify(questionRepository).findById(questionId);

        // Pageable 캡처 및 정렬 검증
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(answerRepository).findByQuestionId(eq(questionId), pageableCaptor.capture());

        Sort sort = pageableCaptor.getValue().getSort();
        assertThat(sort.getOrderFor("id")).isNotNull();
        assertThat(sort.getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);

        verify(qnaMapper).toAnswerDto(any(QnaAnswer.class));
    }

    @Test
    @DisplayName("특정 질문글 답변 페이징 조회 실패 - 존재하지 않는 질문 ID")
    void findByQuestionId_Fail_QuestionNotFound() {
        // Given
        Long invalidQuestionId = 999L;

        when(questionRepository.findById(invalidQuestionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerService.findAnswersByQuestionIdPaging(invalidQuestionId, 0, 10))
                .isInstanceOf(CustomException.class)
                .satisfies(response -> {
                    CustomException exception = (CustomException) response;
                    assertThat(exception.getMessage()).isEqualTo(NOT_FOUND_QNAQUESTION);
                    assertThat(exception.getErrorCode()).isEqualTo(404);
                });

        verify(questionRepository).findById(invalidQuestionId);
        verify(answerRepository, never()).findByQuestionId(anyLong(), any(Pageable.class));
    }

    // 답변 수정 메서드 테스트

    @Test
    @DisplayName("답변 수정 성공 - 답변 존재 및 어드민 권한 일치")
    void updateAnswer_Success() {
        // Given
        Long answerId = 10L;
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(commonAnswer));
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(adminUser));
        when(qnaMapper.toAnswerDto(commonAnswer)).thenReturn(commonResponseDto);

        // When
        answerService.updateAnswer(answerId, commonSaveDto, loginUserId);

        // Then
        verify(answerRepository).findById(answerId);
        verify(userRepository).findByUserId(loginUserId);
        verify(qnaMapper).updateAnswerEntityFromDto(eq(commonSaveDto), eq(commonAnswer));
        verify(qnaMapper).toAnswerDto(commonAnswer);
    }

    @Test
    @DisplayName("답변 수정 실패 - 답변글 미존재")
    void updateAnswer_Fail_NotFound() {
        long invalidId = 999L;
        // Given
        when(answerRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerService.updateAnswer(invalidId, commonSaveDto, loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_FOUND_QNAANSWER);

        verify(answerRepository).findById(invalidId);
        verify(userRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("답변 수정 실패 - 유저 정보 미존재")
    void updateAnswer_Fail_NotAdmin() {
        // Given
        String normalUserId = "normalUser";

        when(answerRepository.findById(10L)).thenReturn(Optional.of(commonAnswer));
        when(userRepository.findByUserId(normalUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerService.updateAnswer(10L, commonSaveDto, normalUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_USER);

        verify(answerRepository).findById(10L);
        verify(userRepository).findByUserId(normalUserId);
        verify(qnaMapper, never()).updateAnswerEntityFromDto(any(), any());
    }

    private User createOtherClubUser() {
        // 1. StudentClub 초기화
        StudentClub otherClub = StudentClub.builder()
                .id(2L)
                .studentClubName("다른동아리")
                .Balance(10000)
                .verification(true)
                .build();

        // 2. User 초기화 (StudentClub 연관관계 포함)
        User otherClubUser = User.builder()
                .id(2L)
                .userId(loginUserId)
                .name("사용자")
                .studentClub(otherClub)
                .build();
        return otherClubUser;
    }

    @Test
    @DisplayName("답변 수정 실패 - 작성자 학생회 불일치")
    void updateAnswer_Fail_NotSameClub() {
        // Given
        User otherClubUser = createOtherClubUser();

        when(answerRepository.findById(1L)).thenReturn(Optional.of(commonAnswer));
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(otherClubUser));

        // When & Then
        assertThatThrownBy(() -> answerService.updateAnswer(1L, commonSaveDto, loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_BELONGING);
        verify(answerRepository).findById(1L);
        verify(userRepository).findByUserId(loginUserId);
        verify(qnaMapper, never()).updateQuestionEntityFromDto(any(), any());

    }

    // 삭제 메서드 테스트

    @Test
    @DisplayName("답변 삭제 성공 - 답변 존재 및 작성자 일치")
    void deleteAnswer_Success() {
        // Given
        when(answerRepository.findById(1L)).thenReturn(Optional.of(commonAnswer));
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(adminUser));
        when(qnaMapper.toAnswerDto(commonAnswer)).thenReturn(commonResponseDto);
        // When
        AnswerDto resultDto = answerService.deleteAnswer(1L, loginUserId);

        // Then
        assertThat(resultDto).isEqualTo(commonResponseDto);
        verify(answerRepository).findById(1L);
        verify(userRepository).findByUserId(loginUserId);
        verify(qnaMapper).toAnswerDto(commonAnswer);
        verify(answerRepository).delete(commonAnswer);
    }

    @Test
    @DisplayName("답변 삭제 실패 - 답변글 미존재")
    void deleteAnswer_Fail_NotFound() {
        // Given
        long invalidAnswerId = 999L;
        when(answerRepository.findById(invalidAnswerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerService.deleteAnswer(invalidAnswerId, loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_FOUND_QNAANSWER);

        verify(answerRepository).findById(invalidAnswerId);
        verify(qnaMapper, never()).toQuestionDto(any());
    }

    @Test
    @DisplayName("답변 삭제 실패 - 유저 정보 미존재")
    void deleteAnswer_Fail_NotWriter() {
        // Given
        String hackerId = "hacker";
        when(answerRepository.findById(1L)).thenReturn(Optional.of(commonAnswer));
        when(userRepository.findByUserId(hackerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerService.deleteAnswer(1L, hackerId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_USER);

        verify(answerRepository).findById(1L);
        verify(userRepository).findByUserId(hackerId);
        verify(qnaMapper, never()).toAnswerDto(any());
    }

    @Test
    @DisplayName("질문 삭제 실패 - 작성자 학생회 불일치")
    void deleteQuestion_Fail_NotSameClub() {
        // Given
        User otherClubUser = createOtherClubUser();

        when(answerRepository.findById(1L)).thenReturn(Optional.of(commonAnswer));
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(otherClubUser));

        // When & Then
        assertThatThrownBy(() -> answerService.deleteAnswer(1L, loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_BELONGING);
        verify(answerRepository).findById(1L);
        verify(userRepository).findByUserId(loginUserId);
        verify(qnaMapper, never()).toAnswerDto(any());

    }
}
