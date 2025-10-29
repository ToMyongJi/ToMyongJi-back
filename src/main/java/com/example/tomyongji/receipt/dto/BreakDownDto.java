package com.example.tomyongji.receipt.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakDownDto {
    private String keyword;
    private String issueDate;
    private String issueNumber;
    private Long studentClubId;
}
