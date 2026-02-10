package com.example.tomyongji.qna.service;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.global.error.CustomException;
import com.example.tomyongji.qna.dto.request.AnswerSaveDto;
import com.example.tomyongji.qna.dto.request.QuestionSaveDto;
import com.example.tomyongji.qna.dto.response.QuestionDto;
import com.example.tomyongji.qna.entity.QnaAnswer;
import com.example.tomyongji.qna.entity.QnaQuestion;
import com.example.tomyongji.qna.mapper.QnaMapper;
import com.example.tomyongji.qna.repository.QnaAnswerRepository;
import com.example.tomyongji.qna.repository.QnaQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.tomyongji.global.error.ErrorMsg.*;


@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaQuestionRepository qnaQuestionRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final UserRepository userRepository;
    private final QnaMapper qnaMapper;

    private static final String ADMIN_CLUB_NAME = "어드민";

    // 질문 등록
    public QnaQuestion createQuestion(QuestionSaveDto questionDto, String loginUserId) {
        // 유저 존재 여부 확인
        User user = validateUser(loginUserId);

        QnaQuestion question = qnaMapper.toQuestionEntity(questionDto);
        question.setWriter(user);
        return qnaQuestionRepository.save(question);
    }

    // 답변 등록
    public QnaAnswer createAnswer(Long questionId, AnswerSaveDto answerDto, String loginUserId) {
        // 유저 존재 여부 확인
        User user = validateUser(loginUserId);
        // 질문글 존재 여부 확인
        QnaQuestion question = qnaQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_QNAQUESTION, 404));
        // 관리자인지 체크
        if (user.getStudentClub() == null || !ADMIN_CLUB_NAME.equals(user.getStudentClub().getStudentClubName())) {
            throw new CustomException(NO_AUTHORIZATION_BELONGING, 403);
        }

        QnaAnswer answer = qnaMapper.toAnswerEntity(answerDto);
        answer.setWriter(user);
        question.addAnswer(answer);

        return qnaAnswerRepository.save(answer);
    }

    private User validateUser(String loginUsername) {
        return userRepository.findByUserId(loginUsername)
                .orElseThrow(() -> new CustomException(NO_AUTHORIZATION_USER, 400));
    }

    // 질문글 전체 조회(페이지네이션)
    public List<QuestionDto>
    // 질문글 1개 조회
    // 질문글 해당 답변글 전체 조회(페이지네이션)
    // 질문/답변글 수정/삭제
}
