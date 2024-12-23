package com.example.tomyongji.receipt.dto;

import com.example.tomyongji.receipt.util.CustomDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Slf4j
public class ReceiptByStudentClubDto {

    private List<ReceiptDto> receiptList;
    private int Balance;

}
