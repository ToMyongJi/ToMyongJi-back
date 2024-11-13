package com.example.tomyongji.receipt.dto;

import com.example.tomyongji.receipt.entity.StudentClub;
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
