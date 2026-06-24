package com.example.tomyongji.domain.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ExcelExportDto {
    private String userId;
    private int year;
    private int month;
}
