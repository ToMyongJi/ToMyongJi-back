package com.example.tomyongji.domain.receipt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
}
