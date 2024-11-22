package com.example.tomyongji.receipt.dto;

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
public class ReceiptUpdateDto {

    private long receiptId; //기존의 영수증 아이디

    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date date;
    private String content;

    private int deposit;
    private int withdrawal;

}
