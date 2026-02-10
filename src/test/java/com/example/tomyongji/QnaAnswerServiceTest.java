package com.example.tomyongji;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.global.error.CustomException;
import com.example.tomyongji.qna.dto.request.AnswerSaveDto;
import com.example.tomyongji.qna.dto.response.AnswerDto;
import com.example.tomyongji.qna.dto.response.PageResponseDto;
import com.example.tomyongji.qna.dto.response.QuestionDto;
import com.example.tomyongji.qna.entity.QnaAnswer;
import com.example.tomyongji.qna.entity.QnaQuestion;
import com.example.tomyongji.qna.mapper.QnaMapper;
import com.example.tomyongji.qna.repository.QnaAnswerRepository;
import com.example.tomyongji.qna.repository.QnaQuestionRepository;
import com.example.tomyongji.qna.service.QnaAnswerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
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

    private final String loginUserId = "testUser";

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
        QnaAnswer result = answerService.createAnswer(1L, dto, loginUserId);

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

    @Test
    @DisplayName("답변 등록 실패 - 존재하지 않은 유저")
    void createAnswer_Fail_Unauthorized() {
        // Given
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerService.createAnswer(1L, new AnswerSaveDto(), loginUserId))
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
        assertThatThrownBy(() -> answerService.createAnswer(1L, new AnswerSaveDto(), loginUserId))
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
        assertThatThrownBy(() -> answerService.createAnswer(1L, new AnswerSaveDto(), loginUserId))
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
        Long questionId = 1L;
        int page = 0;
        int size = 5;

        QnaQuestion question = QnaQuestion.builder()
                .id(questionId)
                .build();
        QnaAnswer answer = QnaAnswer.builder()
                .id(100L)
                .content("답변 내용입니다.")
                .build();

        Page<QnaAnswer> answerPage = new PageImpl<>(List.of(answer), PageRequest.of(page, size), 1);
        AnswerDto answerDto =  AnswerDto.builder().answerId(100L).content("답변 내용입니다.").build();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(answerRepository.findByQuestionId(eq(questionId), any(Pageable.class)))
                .thenReturn(answerPage);
        when(qnaMapper.toAnswerDto(any(QnaAnswer.class))).thenReturn(answerDto);

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
                .containsExactly(100L);

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

    // 수정, 삭제 테스트용 헬퍼 메서드
    private User createTestUser(String userId) {
        return User.builder().userId(userId).name("사용자").build();
    }

    private QnaAnswer createTestAnswer(Long id, User writer) {
        return QnaAnswer.builder()
                .id(id)
                .content("기존 내용")
                .writer(writer)
                .build();
    }

    // 답변 수정 메서드 테스트

    @Test
    @DisplayName("답변 수정 성공 - 답변 존재 및 어드민 권한 일치")
    void updateAnswer_Success() {
        // Given
        String userId = "admin123";
        Long answerId = 10L;
        User adminUser = createTestUser(userId);
        QnaAnswer answer = createTestAnswer(answerId, adminUser);
        AnswerSaveDto updateDto = new AnswerSaveDto("수정된 답변 내용");

        when(answerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(qnaMapper.toAnswerDto(answer)).thenReturn(new AnswerDto());

        // When
        answerService.updateAnswer(answerId, updateDto, userId);

        // Then
        verify(answerRepository).findById(answerId);
        verify(qnaMapper).updateAnswerEntityFromDto(eq(updateDto), eq(answer));
        verify(qnaMapper).toAnswerDto(answer);
    }

    @Test
    @DisplayName("답변 수정 실패 - 답변글 미존재")
    void updateAnswer_Fail_NotFound() {
        // Given
        String userId = "admin123";
        when(answerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerService.updateAnswer(999L, new AnswerSaveDto(), userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_FOUND_QNAANSWER);

        verify(answerRepository).findById(999L);
        verify(qnaMapper, never()).updateAnswerEntityFromDto(any(), any());
    }

    @Test
    @DisplayName("답변 수정 실패 - 작성자 미동일")
    void updateAnswer_Fail_NotAdmin() {
        // Given
        String normalUserId = "normalUser";
        QnaAnswer answer = createTestAnswer(10L, createTestUser("admin"));

        when(answerRepository.findById(10L)).thenReturn(Optional.of(answer));

        // When & Then
        assertThatThrownBy(() -> answerService.updateAnswer(10L, new AnswerSaveDto(), normalUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_USER);

        verify(answerRepository).findById(10L);
        verify(qnaMapper, never()).updateAnswerEntityFromDto(any(), any());
    }

    // 삭제 메서드 테스트

    @Test
    @DisplayName("답변 삭제 성공 - 답변 존재 및 작성자 일치")
    void deleteAnswer_Success() {
        // Given
        String adminUserId = "adminUser";
        User user = createTestUser(adminUserId);
        QnaAnswer answer = createTestAnswer(1L, user);
        AnswerDto returnDto = new AnswerDto();

        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer));
        when(qnaMapper.toAnswerDto(answer)).thenReturn(returnDto);
        // When
        AnswerDto resultDto = answerService.deleteAnswer(1L, adminUserId);

        // Then
        assertThat(resultDto).isEqualTo(returnDto);
        verify(answerRepository).findById(1L);
        verify(qnaMapper).toAnswerDto(answer);
        verify(answerRepository).delete(answer);
    }

    @Test
    @DisplayName("답변 삭제 실패 - 답변글 미존재")
    void deleteAnswer_Fail_NotFound() {
        // Given
        long invalidAnswerId = 999L;
        when(answerRepository.findById(invalidAnswerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerService.deleteAnswer(invalidAnswerId, "admin"))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_FOUND_QNAANSWER);

        verify(answerRepository).findById(invalidAnswerId);
        verify(qnaMapper, never()).toQuestionDto(any());
    }

    @Test
    @DisplayName("답변 삭제 실패 - 작성자 미동일")
    void deleteAnswer_Fail_NotWriter() {
        // Given
        String invalidUserId = "normalUser";
        String ownerUserId = "owner";
        QnaAnswer answer = createTestAnswer(1L, createTestUser(ownerUserId));

        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer));

        // When & Then
        assertThatThrownBy(() -> answerService.deleteAnswer(1L, invalidUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_USER);

        verify(answerRepository).findById(1L);
        verify(qnaMapper, never()).toQuestionDto(any());
    }
}
