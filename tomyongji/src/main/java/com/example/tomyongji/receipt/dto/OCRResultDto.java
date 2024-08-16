package com.example.tomyongji.receipt.dto;

import com.example.tomyongji.receipt.util.CustomDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import lombok.Data;

@Data
public class OCRResultDto {
    @JsonDeserialize(using = CustomDateDeserializer.class)
    Date date;
    String content;
    int withdrawal;

    public OCRResultDto(Date date, String content, int withdrawal) {
        this.date = date;
        this.content = content;
        this.withdrawal = withdrawal;
    }
}
