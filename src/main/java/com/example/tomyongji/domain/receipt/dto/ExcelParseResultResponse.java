package com.example.tomyongji.domain.receipt.dto;

import com.example.tomyongji.domain.receipt.entity.Receipt;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExcelParseResultResponse {

    // 파싱 결과
    private List<ParseRow> rows;

    public int getTotalCount() { return rows == null ? 0 : rows.size(); }

    public int getSuccessCount() {
        return rows == null ? 0 : (int) rows.stream()
                .filter(row -> row.getStatus() == ParseStatus.SUCCESS)
                .count();
    }

    public int getFailCount() {
        return rows == null ? 0 : (int) rows.stream()
                .filter(row -> row.getStatus() == ParseStatus.FAILED)
                .count();
    }

    @JsonIgnore
    public List<Receipt> getSuccessfulReceipts() {
        return rows == null ? List.of() : rows.stream()
                .filter(row -> row.getStatus() == ParseStatus.SUCCESS)
                .map(ParseRow::getReceipt)
                .filter(Objects::nonNull)
                .toList();
    }

    public enum ParseStatus {
        SUCCESS, FAILED
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ParseRow {
        private int rowNumber;
        private ParseStatus status;
        private String date;
        private String content;
        private int deposit;
        private int withdrawal;
        private List<ParseError> errors;

        @JsonIgnore
        private Receipt receipt;
    }

    @Getter
    @Builder
    public static class ParseError {
        private String errorColumn;
        private String errorMessage;
    }
}