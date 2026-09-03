package com.example.tomyongji.domain.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExcelStatusResponseDto {
    private String requestId;
    private String status;
    private List<ExcelPreviewItemDto> previewData;
    private String message;
    private int skippedRows;
    private int totalRows;
    private String dateFormatDetected;
}
