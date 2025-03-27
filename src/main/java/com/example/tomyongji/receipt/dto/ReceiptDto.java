package com.example.tomyongji.receipt.dto;

import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.util.CustomDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Slf4j
public class ReceiptDto {

    private long receiptId;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date date;
    //private long receiptId;
    private String content;

    private int deposit;
    private int withdrawal;
}
