package com.example.tomyongji.qna.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
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
