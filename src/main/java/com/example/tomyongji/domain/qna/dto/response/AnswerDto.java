package com.example.tomyongji.domain.qna.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class AnswerDto {
    private long answerId;
    private String writer;
    private String content;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private long questionId;
}
