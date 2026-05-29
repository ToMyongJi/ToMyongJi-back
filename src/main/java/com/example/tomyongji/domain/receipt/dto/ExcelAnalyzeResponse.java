package com.example.tomyongji.domain.receipt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExcelAnalyzeResponse {

    private String analysisSource;

    private String fileId;

    private ExcelColumnMappingDto analyzeResult;

    private ExcelParseResultResponse parseResult;

}