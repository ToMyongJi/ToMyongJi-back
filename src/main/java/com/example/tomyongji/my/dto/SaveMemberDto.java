package com.example.tomyongji.my.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveMemberDto {
    @NotBlank(message="학생회장 아이디는 필수 입력값입니다")
    private long presidentUserId;
    @NotBlank(message="학번은 필수 입력값입니다")
    private String studentNum;
    @NotBlank(message="이름은 필수 입력값입니다")
    private String name;

}
