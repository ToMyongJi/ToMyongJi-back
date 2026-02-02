package com.example.tomyongji.qna.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerSaveDto {
    @NotBlank(message = "답변 내용은 필수 입력값입니다")
    private String content;
}
