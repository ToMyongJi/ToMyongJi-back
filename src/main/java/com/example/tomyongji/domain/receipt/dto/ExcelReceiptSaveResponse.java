package com.example.tomyongji.domain.receipt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExcelReceiptSaveResponse {

    // 저장 성공 여부
    private Status status;

    // 최종 파싱 결과
    private ExcelParseResultResponse parseResult;

    public enum Status {
        SAVED,
        VALIDATION_FAILED
    }
}