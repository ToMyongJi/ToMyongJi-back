package com.example.tomyongji.qna.service;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.global.error.CustomException;
import com.example.tomyongji.qna.dto.request.AnswerSaveDto;
import com.example.tomyongji.qna.dto.response.AnswerDto;
import com.example.tomyongji.qna.dto.response.PageResponseDto;
import com.example.tomyongji.qna.entity.QnaAnswer;
import com.example.tomyongji.qna.entity.QnaQuestion;
import com.example.tomyongji.qna.mapper.QnaMapper;
import com.example.tomyongji.qna.repository.QnaAnswerRepository;
import com.example.tomyongji.qna.repository.QnaQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.tomyongji.global.error.ErrorMsg.*;

@Service
@RequiredArgsConstructor
public class QnaAnswerService {

    private final QnaQuestionRepository qnaQuestionRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final UserRepository userRepository;
    private final QnaMapper qnaMapper;

    private static final String ADMIN_CLUB_NAME = "어드민";

    private static void checkAdminClub(User user) {
        if (user.getStudentClub() == null || !ADMIN_CLUB_NAME.equals(user.getStudentClub().getStudentClubName())) {
            throw new CustomException(NO_AUTHORIZATION_BELONGING, 403);
        }
    }

    // 답변 등록
    public QnaAnswer createAnswer(Long questionId, AnswerSaveDto answerDto, String loginUserId) {
        // 유저 존재 여부 확인
        User user = checkValidateUser(loginUserId);
        // 질문글 존재 여부 확인
        QnaQuestion question = checkValidateQuestion(questionId);
        // 관리자인지 체크 -> 추후에 필터로 제한할 시 코드 삭제
        checkAdminClub(user);

        QnaAnswer answer = qnaMapper.toAnswerEntity(answerDto);
        answer.setWriter(user);
        question.addAnswer(answer);

        return qnaAnswerRepository.save(answer);
    }

    // 질문글 해당 답변글 페이지별 조회
    @Transactional(readOnly = true)
    public PageResponseDto<AnswerDto> findAnswersByQuestionIdPaging(Long questionId, int page, int size) {
        // 질문글 존재 여부 확인
        checkValidateQuestion(questionId);

        // 답변글은 오래된 순으로 정렬
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));

        Page<QnaAnswer> answerPage = qnaAnswerRepository.findByQuestionId(questionId, pageable);

        Page<AnswerDto> answerDtoPage = answerPage.map(qnaMapper::toAnswerDto);
        return PageResponseDto.from(answerDtoPage);
    }

    // 답변글 수정
    @Transactional
    public AnswerDto updateAnswer(Long answerId, AnswerSaveDto answerDto, String loginUserId) {
        QnaAnswer answer = checkValidateAnswer(answerId);

        checkWriter(answer, loginUserId);

        qnaMapper.updateAnswerEntityFromDto(answerDto, answer);
        return qnaMapper.toAnswerDto(answer);
    }

    // 답변글 삭제
    @Transactional
    public AnswerDto deleteAnswer(Long answerId, String loginUserId) {
        QnaAnswer answer = checkValidateAnswer(answerId);

        checkWriter(answer, loginUserId);

        AnswerDto deletedAnswerDto = qnaMapper.toAnswerDto(answer);
        qnaAnswerRepository.delete(answer);
        return deletedAnswerDto;
    }


    private QnaAnswer checkValidateAnswer(Long answerId) {
        QnaAnswer answer = qnaAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_QNAANSWER, 404));
        return answer;
    }


    private QnaQuestion checkValidateQuestion(Long questionId) {
        QnaQuestion question = qnaQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_QNAQUESTION, 404));
        return question;
    }


    private User checkValidateUser(String loginUsername) {
        return userRepository.findByUserId(loginUsername)
                .orElseThrow(() -> new CustomException(NO_AUTHORIZATION_USER, 400));
    }


    private void checkWriter(QnaAnswer answer, String loginUserId) {
        if(!answer.getWriter().getUserId().equals(loginUserId)) {
            throw new CustomException(NO_AUTHORIZATION_USER, 403);
        }
    }
}
