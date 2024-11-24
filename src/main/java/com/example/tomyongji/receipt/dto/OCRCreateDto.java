package com.example.tomyongji.receipt.dto;

import com.example.tomyongji.receipt.util.CustomDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import lombok.Data;

@Data
public class OCRCreateDto {

    private long userId;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    Date date;
    String content;
    int withdrawal;

}