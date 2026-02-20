package com.example.tomyongji.domain.qna.service;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.global.error.CustomException;
import com.example.tomyongji.domain.qna.dto.request.QuestionSaveDto;
import com.example.tomyongji.domain.qna.dto.response.PageResponseDto;
import com.example.tomyongji.domain.qna.dto.response.QuestionDto;
import com.example.tomyongji.domain.qna.entity.QnaQuestion;
import com.example.tomyongji.domain.qna.mapper.QnaMapper;
import com.example.tomyongji.domain.qna.repository.QnaQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.tomyongji.global.error.ErrorMsg.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class QnaQuestionService {

    private final QnaQuestionRepository qnaQuestionRepository;
    private final UserRepository userRepository;
    private final QnaMapper qnaMapper;

    // 질문 등록
    public QuestionDto createQuestion(QuestionSaveDto questionDto, String loginUserId) {
        // 유저 존재 여부 확인
        User user = validateUser(loginUserId);

        QnaQuestion question = qnaMapper.toQuestionEntity(questionDto);
        question.setWriter(user.getStudentClub());
        QnaQuestion savedQuestion = qnaQuestionRepository.save(question);
        return qnaMapper.toQuestionDto(savedQuestion);

    }

    // 질문글 페이지별 조회
    @Transactional(readOnly = true)
    public PageResponseDto<QuestionDto> findAllQuestionsPaging(int page, int size) {
        // 최신순(id 내림차순) 정렬 조건을 포함한 Pageable 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<QnaQuestion> questionPage = qnaQuestionRepository.findAll(pageable);

        // Entity Page를 DTO Page로 변환
        Page<QuestionDto> questionDtoPage = questionPage.map(qnaMapper::toQuestionDto);
        return PageResponseDto.from(questionDtoPage);
    }

    // 질문글 1개 조회
    public QuestionDto findQuestionById(long id) {
        QnaQuestion findQuestion = checkValidateQuestion(id);
        return qnaMapper.toQuestionDto(findQuestion);
    }

    // 질문글 수정
    @Transactional
    public QuestionDto updateQuestion(long questionId, QuestionSaveDto questionDto, String loginUserId) {
        // 질문글 있는지 확인
        QnaQuestion question = checkValidateQuestion(questionId);

        // 로그인한 유저가 작성글의 학생회와 일치하는지 확인
        checkWriter(question, loginUserId);

        qnaMapper.updateQuestionEntityFromDto(questionDto, question);
        qnaQuestionRepository.saveAndFlush(question);
        return qnaMapper.toQuestionDto(question);
    }

    // 질문글 삭제
    @Transactional
    public QuestionDto deleteQuestion(long questionId, String loginUserId) {
        QnaQuestion question = checkValidateQuestion(questionId);

        checkWriter(question, loginUserId);

        QuestionDto deletedQuestionDto = qnaMapper.toQuestionDto(question);
        qnaQuestionRepository.delete(question);
        return deletedQuestionDto;
    }

    private User validateUser(String loginUsername) {
        return userRepository.findByUserId(loginUsername)
                .orElseThrow(() -> new CustomException(NO_AUTHORIZATION_USER, 400));
    }

    private QnaQuestion checkValidateQuestion(long id) {
        return qnaQuestionRepository.findById(id)
                .orElseThrow(() -> new CustomException(NOT_FOUND_QNAQUESTION, 404));
    }
    private void checkWriter(QnaQuestion question, String loginUserId) {
        User loginUser = validateUser(loginUserId);
        StudentClub questionClub = question.getWriter();
        StudentClub loginUserClub = loginUser.getStudentClub();

        // 방어 코드: 둘 중 하나라도 null이면 권한이 없는 것으로 간주
        if (questionClub == null || loginUserClub == null) {
            throw new CustomException(NO_AUTHORIZATION_BELONGING, 403);
        }

        if (!questionClub.getId().equals(loginUserClub.getId())) {
            throw new CustomException(NO_AUTHORIZATION_BELONGING, 403);
        }
    }
}
