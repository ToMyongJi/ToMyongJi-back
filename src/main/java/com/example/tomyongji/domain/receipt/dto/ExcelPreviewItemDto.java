package com.example.tomyongji.domain.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExcelPreviewItemDto {
    private LocalDate date;
    private String content;
    private int deposit;
    private int withdrawal;
}
