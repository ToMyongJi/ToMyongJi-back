package com.example.tomyongji.my.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminSaveMemberDto {
    @NotBlank(message="학생회 아이디는 필수 입력값입니다")
    private long clubId;
    @NotBlank(message="학번은 필수 입력값입니다")
    private String studentNum;
    @NotBlank(message="이름은 필수 입력값입니다")
    private String name;

}
