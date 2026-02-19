package com.example.tomyongji.domain.qna.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class QuestionDto {
    private long questionId;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
