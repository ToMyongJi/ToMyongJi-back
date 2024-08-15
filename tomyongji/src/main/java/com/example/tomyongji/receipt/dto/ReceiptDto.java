package com.example.tomyongji.receipt.dto;

import com.example.tomyongji.receipt.util.CustomDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import lombok.Data;

@Data
public class ReceiptDto {

    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date date;

    private String content;

    private int deposit;
    private int withdrawal;

}
