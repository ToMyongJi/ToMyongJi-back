package com.example.tomyongji;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.global.error.CustomException;
import com.example.tomyongji.qna.dto.request.QuestionSaveDto;
import com.example.tomyongji.qna.dto.response.PageResponseDto;
import com.example.tomyongji.qna.dto.response.QuestionDto;
import com.example.tomyongji.qna.entity.QnaQuestion;
import com.example.tomyongji.qna.mapper.QnaMapper;
import com.example.tomyongji.qna.repository.QnaQuestionRepository;
import com.example.tomyongji.qna.service.QnaQuestionService;
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
public class  QnaQuestionServiceTest {

    @InjectMocks private QnaQuestionService questionService;

    @Mock private QnaQuestionRepository questionRepository;
    @Mock private UserRepository userRepository;
    @Mock private QnaMapper qnaMapper;

    // 공용 테스트 데이터 필드
    private final String loginUserId = "testUser";
    private User testUser;
    private StudentClub testClub;
    private QuestionSaveDto commonSaveDto;
    private QnaQuestion commonQuestion;
    private QuestionDto commonResponseDto;

    @BeforeEach
    void setUp() {
        // 1. StudentClub 초기화
        testClub = StudentClub.builder()
                .id(1L)
                .studentClubName("테스트동아리")
                .Balance(10000)
                .verification(true)
                .build();

        // 2. User 초기화 (StudentClub 연관관계 포함)
        testUser = User.builder()
                .id(1L)
                .userId(loginUserId)
                .name("사용자")
                .studentClub(testClub)
                .build();

        // 3. QuestionSaveDto 초기화
        commonSaveDto = QuestionSaveDto.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        // 4. QnaQuestion 초기화 (모든 필드 빌더 채움)
        commonQuestion = QnaQuestion.builder()
                .id(1L)
                .title("질문 제목")
                .content("질문 내용")
                .writer(testClub) // 변경된 로직 반영 (StudentClub이 작성자)
                .answers(new ArrayList<>())
                .build();

        commonResponseDto = QuestionDto.builder()
                .questionId(100L)
                .title("질문 응답 DTO")
                .build();
    }

    @Test
    @DisplayName("질문 등록 성공")
    void createQuestion_Success() {
        // Given
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(testUser));
        when(qnaMapper.toQuestionEntity(commonSaveDto)).thenReturn(commonQuestion);
        when(questionRepository.save(commonQuestion)).thenReturn(commonQuestion);

        // When
        QnaQuestion result = questionService.createQuestion(commonSaveDto, loginUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(commonQuestion.getId());
        assertThat(result.getTitle()).isEqualTo(commonQuestion.getTitle());
        assertThat(result.getContent()).isEqualTo(commonQuestion.getContent());
        assertThat(result.getWriter()).isEqualTo(testClub);
        verify(qnaMapper).toQuestionEntity(commonSaveDto);
        verify(questionRepository).save(commonQuestion);
    }

    @Test
    @DisplayName("질문 등록 실패 - 존재하지 않는 유저")
    void createQuestion_Fail_Unauthorized() {
        // Given
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.createQuestion(commonSaveDto, loginUserId))
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

        List<QnaQuestion> questions = List.of(commonQuestion);

        Page<QnaQuestion> questionPage = new PageImpl<>(questions, PageRequest.of(page, size), 1);

        // repository.findAll(Pageable)이 호출되면 준비한 questionPage를 반환
        when(questionRepository.findAll(any(Pageable.class))).thenReturn(questionPage);
        when(qnaMapper.toQuestionDto(any(QnaQuestion.class))).thenReturn(commonResponseDto);

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
                .containsExactly(commonResponseDto.getQuestionId());

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
        Long id = commonQuestion.getId(); // 1L

        when(questionRepository.findById(id)).thenReturn(Optional.of(commonQuestion));
        when(qnaMapper.toQuestionDto(commonQuestion)).thenReturn(commonResponseDto);

        // When
        QuestionDto result = questionService.findQuestionById(id);

        // Then
        assertThat(result.getQuestionId()).isEqualTo(commonResponseDto.getQuestionId());
        assertThat(result.getTitle()).isEqualTo(commonResponseDto.getTitle());
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

    // 수정 메서드 테스트 시나리오

    @Test
    @DisplayName("질문 수정 성공 - 질문 존재 및 작성자 학생회 일치")
    void updateQuestion_Success() {
        // Given
        Long questionId = commonQuestion.getId();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(commonQuestion));
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(testUser));
        when(qnaMapper.toQuestionDto(commonQuestion)).thenReturn(commonResponseDto);
        // When
        questionService.updateQuestion(questionId, commonSaveDto, loginUserId);

        // Then
        verify(questionRepository).findById(questionId);
        verify(qnaMapper).updateQuestionEntityFromDto(eq(commonSaveDto), any(QnaQuestion.class));
        verify(qnaMapper).toQuestionDto(commonQuestion);
    }

    @Test
    @DisplayName("질문 수정 실패 - 질문글 미존재")
    void updateQuestion_Fail_NotFound() {
        // Given
        long invalidId = 999L;
        when(questionRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.updateQuestion(invalidId, commonSaveDto, loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_FOUND_QNAQUESTION);
        verify(questionRepository).findById(invalidId);
        verify(qnaMapper, never()).updateQuestionEntityFromDto(any(), any());
    }

    @Test
    @DisplayName("질문 수정 실패 - 유저 정보 미존재")
    void updateQuestion_Fail_NotWriter() {
        // Given
        String hackerId = "hacker";

        when(questionRepository.findById(1L)).thenReturn(Optional.of(commonQuestion));
        when(userRepository.findByUserId(hackerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.updateQuestion(1L, commonSaveDto, hackerId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_USER);
        verify(questionRepository).findById(1L);
        verify(qnaMapper, never()).updateQuestionEntityFromDto(any(), any());
    }

    @Test
    @DisplayName("질문 수정 실패 - 작성자 학생회 불일치")
    void updateQuestion_Fail_NotSameClub() {
        // Given
        User otherClubUser = createOtherClubUser();

        when(questionRepository.findById(1L)).thenReturn(Optional.of(commonQuestion));
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(otherClubUser));

        // When & Then
        assertThatThrownBy(() -> questionService.updateQuestion(1L, commonSaveDto, loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_BELONGING);
        verify(questionRepository).findById(1L);
        verify(userRepository).findByUserId(loginUserId);
        verify(qnaMapper, never()).updateQuestionEntityFromDto(any(), any());

    }

    private User createOtherClubUser() {
        StudentClub otherClub = StudentClub.builder()
                .id(2L)
                .studentClubName("다른동아리")
                .Balance(10000)
                .verification(true)
                .build();

        User otherClubUser = User.builder()
                .id(2L)
                .userId(loginUserId)
                .name("사용자")
                .studentClub(otherClub)
                .build();
        return otherClubUser;
    }

    // 삭제 메서드 테스트 시나리오

    @Test
    @DisplayName("질문 삭제 성공 - 질문 존재 및 작성자 학생회 일치")
    void deleteQuestion_Success() {
        // Given
        Long questionId = commonQuestion.getId();
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(commonQuestion));
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(testUser));
        when(qnaMapper.toQuestionDto(commonQuestion)).thenReturn(commonResponseDto);

        // When
        QuestionDto resultDto = questionService.deleteQuestion(questionId, loginUserId);

        // Then
        assertThat(resultDto).isEqualTo(commonResponseDto);
        verify(questionRepository).findById(questionId);
        verify(userRepository).findByUserId(loginUserId);
        verify(qnaMapper).toQuestionDto(commonQuestion);
        verify(questionRepository).delete(commonQuestion);
    }

    @Test
    @DisplayName("질문 삭제 실패 - 질문글 미존재")
    void deleteQuestion_Fail_NotFound() {
        // Given
        long invalidId = 999L;
        when(questionRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.deleteQuestion(invalidId, loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NOT_FOUND_QNAQUESTION);

        verify(questionRepository).findById(invalidId);
        verify(userRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("질문 삭제 실패 - 유저 정보 미존재")
    void deleteQuestion_Fail_NotWriter() {
        // Given
        String hackerId = "hacker";
        when(questionRepository.findById(1L)).thenReturn(Optional.of(commonQuestion));
        when(userRepository.findByUserId(hackerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.deleteQuestion(1L, hackerId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_USER);

        verify(questionRepository).findById(1L);
        verify(userRepository).findByUserId(hackerId);
        verify(qnaMapper, never()).toQuestionDto(any());
    }

    @Test
    @DisplayName("질문 삭제 실패 - 작성자 학생회 불일치")
    void deleteQuestion_Fail_NotSameClub() {
        // Given
        User otherClubUser = createOtherClubUser();

        when(questionRepository.findById(1L)).thenReturn(Optional.of(commonQuestion));
        when(userRepository.findByUserId(loginUserId)).thenReturn(Optional.of(otherClubUser));

        // When & Then
        assertThatThrownBy(() -> questionService.deleteQuestion(1L, loginUserId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_AUTHORIZATION_BELONGING);
        verify(questionRepository).findById(1L);
        verify(userRepository).findByUserId(loginUserId);
        verify(qnaMapper, never()).toQuestionDto(commonQuestion);

    }

}
