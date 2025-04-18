package com.example.tomyongji.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubVerifyRequestDto {
    private Long clubId;
    private String studentNum;
    private String role;
}
