package com.example.tomyongji.receipt.dto;

import com.example.tomyongji.receipt.entity.BreakDown;
import lombok.Data;
import java.util.List;


public class BreakDownDto {

    @Data
    public static  class PdfParseResult {
        private String issueDate;
        private String issueNumber;
        private List<BreakDown> transactions;
        private String studentClubName;
    }
}
