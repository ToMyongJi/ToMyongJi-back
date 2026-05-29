package com.example.tomyongji.domain.receipt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class ExcelMappingRequest {

    private String fileId;
    private ExcelColumnMappingDto analyzeResult;

}