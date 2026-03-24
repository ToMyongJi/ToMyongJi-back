package com.example.tomyongji.domain.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ClubMemberResponseDto {
    private String studentNum;
    private String name;
}
