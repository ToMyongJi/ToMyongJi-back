package com.example.tomyongji.qna.mapper;

import com.example.tomyongji.qna.dto.request.AnswerSaveDto;
import com.example.tomyongji.qna.dto.request.QuestionSaveDto;
import com.example.tomyongji.qna.dto.response.AnswerDto;
import com.example.tomyongji.qna.dto.response.QuestionDto;
import com.example.tomyongji.qna.entity.QnaAnswer;
import com.example.tomyongji.qna.entity.QnaQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface QnaMapper {

    QnaMapper INSTANCE = Mappers.getMapper(QnaMapper.class);

    QnaQuestion toQuestionEntity(QuestionSaveDto questionSaveDto);

    QnaAnswer toAnswerEntity(AnswerSaveDto answerSaveDto);

    @Mapping(source = "id", target = "questionId")
    @Mapping(source = "writer.studentClubName", target = "writer")
    QuestionDto toQuestionDto(QnaQuestion question);

    @Mapping(source = "id", target = "answerId")
    @Mapping(source = "writer.studentClubName", target = "writer")
    @Mapping(source = "question.id", target = "questionId")
    AnswerDto toAnswerDto(QnaAnswer answer);

    void updateQuestionEntityFromDto(QuestionSaveDto dto, @MappingTarget QnaQuestion entity);
    void updateAnswerEntityFromDto(AnswerSaveDto dto, @MappingTarget QnaAnswer entity);
}
