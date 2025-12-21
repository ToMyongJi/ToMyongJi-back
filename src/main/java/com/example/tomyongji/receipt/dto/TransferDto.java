package com.example.tomyongji.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferDto {
    private String studentClubName;
    private int totalDeposit;
    private int netAmount;
}