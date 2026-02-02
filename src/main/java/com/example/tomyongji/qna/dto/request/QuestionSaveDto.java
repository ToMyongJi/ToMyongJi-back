package com.example.tomyongji.qna.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionSaveDto {
    @NotBlank(message = "제목은 필수 입력값입니다")
    private String title;
    @NotBlank(message = "내용은 필수 입력값입니다")
    private String content;
}
