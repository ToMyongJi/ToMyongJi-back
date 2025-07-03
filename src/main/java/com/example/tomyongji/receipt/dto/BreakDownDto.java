package com.example.tomyongji.receipt.dto;

import lombok.Data;

import java.util.Date;

@Data
public class BreakDownDto {
    private Long id;
    private Date issueDate;
    private String issueNumber;
    private String studentClubName;
}
