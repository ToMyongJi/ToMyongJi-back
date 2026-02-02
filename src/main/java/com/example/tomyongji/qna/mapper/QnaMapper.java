package com.example.tomyongji.qna.mapper;

import com.example.tomyongji.qna.dto.request.AnswerSaveDto;
import com.example.tomyongji.qna.dto.request.QuestionSaveDto;
import com.example.tomyongji.qna.entity.QnaAnswer;
import com.example.tomyongji.qna.entity.QnaQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface QnaMapper {

    QnaMapper INSTANCE = Mappers.getMapper(QnaMapper.class);

    QnaQuestion toQuestionEntity(QuestionSaveDto questionSaveDto);

    QnaAnswer toAnswerEntity(AnswerSaveDto answerSaveDto);
}
