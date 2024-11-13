package com.example.tomyongji.my.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberRequestDto {

    @NotBlank(message="학번은 필수 입력값입니다")
    private String studentNum;
    @NotBlank(message="이름은 필수 입력값입니다")
    private String name;

}
