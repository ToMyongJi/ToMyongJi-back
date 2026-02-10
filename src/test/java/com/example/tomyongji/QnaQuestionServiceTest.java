package com.example.tomyongji;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.global.error.CustomException;
import com.example.tomyongji.qna.dto.request.QuestionSaveDto;
import com.example.tomyongji.qna.dto.response.PageResponseDto;
import com.example.tomyongji.qna.dto.response.QuestionDto;
import com.example.tomyongji.qna.entity.QnaAnswer;
import com.example.tomyongji.qna.entity.QnaQuestion;
import com.example.tomyongji.qna.mapper.QnaMapper;
import com.example.tomyongji.qna.repository.QnaQuestionRepository;
import com.example.tomyongji.qna.service.QnaQuestionService;
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

import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_QNAQUESTION;
import static com.example.tomyongji.global.error.ErrorMsg.NO_AUTHORIZATION_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QnaQuestionServiceTest {

    @InjectMocks private QnaQuestionService questionService;

    @Mock private QnaQuestionRepository questionRepository;
    @Mock private UserRepository userRepository;
    @Mock private QnaMapper qnaMapper;

    private final String loginUserId = "testUser";

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

    // 조회 메서드 테스트 시나리오

    @Test
    @DisplayName("질문 목록 페이징 조회 성공")
    void findAllQuestionsPaging_Success_WithSortVerification() {
        // Given
        int page = 0;
        int size = 10;

        // 가짜 엔티티 및 DTO 설정
        QnaQuestion question = QnaQuestion.builder()
                .id(100L)
                .title("페이징 테스트")
                .build();

        List<QnaQuestion> questions = List.of(question);

        Page<QnaQuestion> questionPage = new PageImpl<>(questions, PageRequest.of(page, size), 1);

        QuestionDto questionDto = new QuestionDto();
        questionDto.setQuestionId(100L);
        questionDto.setTitle("페이징 테스트");

        // repository.findAll(Pageable)이 호출되면 준비한 questionPage를 반환
        when(questionRepository.findAll(any(Pageable.class))).thenReturn(questionPage);
        when(qnaMapper.toQuestionDto(any(QnaQuestion.class))).thenReturn(questionDto);

        // When
        PageResponseDto<QuestionDto> result = questionService.findAllQuestionsPaging(page, size);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.pageNo()).isEqualTo(page);
        assertThat(result.pageSize()).isEqualTo(size);
        assertThat(result.content())
                .hasSize(1)
                .extracting(QuestionDto::getQuestionId)
                .containsExactly(100L);

        // Verification: 서비스 내부에서 생성된 Pageable 가로채기
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(questionRepository).findAll(pageableCaptor.capture());

        // 정렬 조건 검증
        Sort sort = pageableCaptor.getValue().getSort();
        assertThat(sort.getOrderFor("id")).isNotNull();
        assertThat(sort.getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
        verify(qnaMapper).toQuestionDto(any(QnaQuestion.class));
    }

    @Test
    @DisplayName("질문글 상세 조회 성공")
    void findQuestionById_Success() {
        // Given
        Long id = 1L;
        QnaQuestion question = QnaQuestion.builder().id(id).title("질문 제목").build();
        QuestionDto dto = QuestionDto.builder().questionId(id).title("질문 제목").build();

        when(questionRepository.findById(id)).thenReturn(Optional.of(question));
        when(qnaMapper.toQuestionDto(question)).thenReturn(dto);

        // When
        QuestionDto result = questionService.findQuestionById(id);

        // Then
        assertThat(result.getQuestionId()).isEqualTo(id);
        assertThat(result.getTitle()).isEqualTo("질문 제목");
        verify(questionRepository).findById(id);
    }

    @Test
    @DisplayName("질문글 상세 조회 실패 - 존재하지 않는 ID")
    void findQuestionById_Fail() {
        // Given
        Long id = 99L;
        when(questionRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.findQuestionById(id))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_FOUND_QNAQUESTION);
        verify(questionRepository).findById(id);
        verify(qnaMapper, never()).toQuestionDto(any());
    }

    // 수정, 삭제 테스트용 헬퍼 메서드
    private User createTestUser(String userId) {
        return User.builder().userId(userId).name("사용자").build();
    }

    private QnaQuestion createTestQuestion(Long id, User writer) {
        return QnaQuestion.builder()
                .id(id)
                .title("기존 제목")
                .content("기존 내용")
                .writer(writer)
                .answers(List.of(QnaAnswer.builder().id(10L).build()))
                .build();
    }

    // 수정 메서드 테스트 시나리오

    @Test
    @DisplayName("질문 수정 성공 - 질문 존재 및 작성자 일치")
    void updateQuestion_Success() {
        // Given
        String userId = "user123";
        Long questionId = 1L;
        User user = createTestUser(userId);
        QnaQuestion question = createTestQuestion(questionId, user);
        QuestionSaveDto updateDto = new QuestionSaveDto("새 제목", "새 내용");

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(qnaMapper.toQuestionDto(question)).thenReturn(new QuestionDto());
        // When
        questionService.updateQuestion(questionId, updateDto, userId);

        // Then
        verify(questionRepository).findById(questionId);
        verify(qnaMapper).updateQuestionEntityFromDto(eq(updateDto), any(QnaQuestion.class));
        verify(qnaMapper).toQuestionDto(question);
    }

    @Test
    @DisplayName("질문 수정 실패 - 질문글 미존재")
    void updateQuestion_Fail_NotFound() {
        // Given
        String userId = "user123";
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.updateQuestion(999L, new QuestionSaveDto(), userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_FOUND_QNAQUESTION);
        verify(questionRepository).findById(999L);
        verify(qnaMapper, never()).updateQuestionEntityFromDto(any(), any());
    }

    @Test
    @DisplayName("질문 수정 실패 - 작성자 미동일")
    void updateQuestion_Fail_NotWriter() {
        // Given
        User owner = createTestUser("owner");
        QnaQuestion question = createTestQuestion(1L, owner);
        String hackerId = "hacker";

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        // When & Then
        assertThatThrownBy(() -> questionService.updateQuestion(1L, new QuestionSaveDto(), hackerId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_USER);
        verify(questionRepository).findById(1L);
        verify(qnaMapper, never()).updateQuestionEntityFromDto(any(), any());
    }

    // 삭제 메서드 테스트 시나리오

    @Test
    @DisplayName("질문 삭제 성공 - 질문 존재 및 작성자 일치")
    void deleteQuestion_Success() {
        // Given
        String userId = "user123";
        User user = createTestUser(userId);
        QnaQuestion question = createTestQuestion(1L, user);
        QuestionDto returnDto = new QuestionDto();

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(qnaMapper.toQuestionDto(question)).thenReturn(returnDto);
        // When
        QuestionDto resultDto = questionService.deleteQuestion(1L, userId);

        // Then
        assertThat(resultDto).isEqualTo(returnDto);
        verify(questionRepository).findById(1L);
        verify(qnaMapper).toQuestionDto(question);
        verify(questionRepository).delete(question);
    }

    @Test
    @DisplayName("질문 삭제 실패 - 질문글 미존재")
    void deleteQuestion_Fail_NotFound() {
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.deleteQuestion(999L, "anyUser"))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_FOUND_QNAQUESTION);

        verify(questionRepository).findById(999L);
        verify(qnaMapper, never()).toQuestionDto(any());
    }

    @Test
    @DisplayName("질문 삭제 실패 - 작성자 미동일")
    void deleteQuestion_Fail_NotWriter() {
        QnaQuestion question = createTestQuestion(1L, createTestUser("owner"));

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> questionService.deleteQuestion(1L, "hacker"))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_USER);

        verify(questionRepository).findById(1L);
        verify(qnaMapper, never()).toQuestionDto(any());
    }

}
