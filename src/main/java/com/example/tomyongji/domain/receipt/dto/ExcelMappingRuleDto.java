package com.example.tomyongji.domain.receipt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelMappingRuleDto {

    @JsonProperty("date_column")
    private String dateColumn;

    @JsonProperty("content_column")
    private String contentColumn;

    @JsonProperty("amount_type")
    private String amountType;

    @JsonProperty("deposit_column")
    private String depositColumn;

    @JsonProperty("withdrawal_column")
    private String withdrawalColumn;

    @JsonProperty("amount_column")
    private String amountColumn;

    @JsonProperty("data_start_row")
    private Integer dataStartRow;

    @Schema(description = "Gemini가 추론한 날짜 형식 (Java DateTimeFormatter 패턴)", example = "yyyy년 M월 d일")
    @JsonProperty("date_format")
    private String dateFormat;
}
