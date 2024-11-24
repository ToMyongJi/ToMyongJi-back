package com.example.tomyongji.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberDto {

    private Long memberId;

    @NotBlank(message="학번은 필수 입력값입니다")
    private String studentNum;
    @NotBlank(message="이름은 필수 입력값입니다")
    private String name;

}
