package com.example.tomyongji.domain.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelColumnMappingDto {
    private String amountType;
    private String dateOrder;
    private String date;
    private String content;
    private String deposit;
    private String withdrawal;
    private String amount;
}