package com.example.tomyongji.domain.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollegesDto {
    private long collegeId;
    private String collegeName;
    private List<ClubDto> clubs;
}
