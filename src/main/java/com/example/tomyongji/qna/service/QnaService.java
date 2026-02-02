package com.example.tomyongji.qna.service;

import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.global.error.CustomException;
import com.example.tomyongji.qna.dto.request.AnswerSaveDto;
import com.example.tomyongji.qna.dto.request.QuestionSaveDto;
import com.example.tomyongji.qna.entity.QnaAnswer;
import com.example.tomyongji.qna.entity.QnaQuestion;
import com.example.tomyongji.qna.mapper.QnaMapper;
import com.example.tomyongji.qna.repository.QnaAnswerRepository;
import com.example.tomyongji.qna.repository.QnaQuestionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_QNAQUESTION;


@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaQuestionRepository qnaQuestionRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final QnaMapper qnaMapper;

    // 질문 등록
    public QnaQuestion createQuestion(QuestionSaveDto questionDto) {
        QnaQuestion question = qnaMapper.toQuestionEntity(questionDto);

        return qnaQuestionRepository.save(question);
    }

    // 답변 등록
    public QnaAnswer createAnswer(Long questionId, AnswerSaveDto answerDto) {
        QnaQuestion question = qnaQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_QNAQUESTION, 404));

        QnaAnswer answer = qnaMapper.toAnswerEntity(answerDto);
        question.addAnswer(answer);

        return qnaAnswerRepository.save(answer);
    }
}
