package com.example.tomyongji.domain.my.dto;

import com.example.tomyongji.domain.receipt.dto.ClubDto;
import com.example.tomyongji.domain.receipt.dto.CollegeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CollegeAndClubResponseDto {
    private CollegeDto collegeInfo;
    private ClubDto clubInfo;
}
