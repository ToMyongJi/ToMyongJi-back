package com.example.tomyongji.domain.receipt.service;

import com.example.tomyongji.domain.receipt.dto.ExcelColumnMappingDto;
import com.example.tomyongji.global.error.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.tomyongji.global.error.ErrorMsg.*;

@Service
@Slf4j
public class ExcelAIService {
    private static final int MAX_SCAN_ROWS = 15;

    private final ChatClient chatClient;

    public ExcelAIService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public ExcelColumnMappingDto extractColumnConfig(List<Map<Integer, String>> allRows) {
        try {
            String excelSample = buildExcelSample(allRows);

            ExcelColumnMappingDto aiResult = chatClient.prompt()
                    .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                    .system(buildSystemPrompt())
                    .user(buildUserPrompt(excelSample))
                    .call()
                    .entity(ExcelColumnMappingDto.class);

            validateAiResult(aiResult);

            log.info(
                    "Excel AI 분석 결과 amountType={}, date={}, content={}, deposit={}, withdrawal={}, amount={}",
                    aiResult.getAmountType(),
                    aiResult.getDate(),
                    aiResult.getContent(),
                    aiResult.getDeposit(),
                    aiResult.getWithdrawal(),
                    aiResult.getAmount()
            );

            return aiResult;

        } catch (Exception e) {
            log.error("Spring AI Excel 컬럼 분석 실패", e);
            throw new CustomException(EXCEL_AI_ANALYSIS_FAILED, 500);
        }
    }

    private String buildSystemPrompt() {
        return """
                너는 학생회 회계 Excel 장부의 컬럼명을 분석하는 도우미다.

                1. amountType
                   - SPLIT: 입금/출금이 별도 컬럼으로 분리된 경우
                   - SINGLE: 하나의 금액 컬럼에 +/- 또는 절댓값으로 기록된 경우
                2. columnMapping: 각 표준 필드에 해당하는 실제 Excel 컬럼명
                   - date: 거래일자 컬럼명
                   - content: 사용처, 거래내용 컬럼명
                   - deposit: 입금 컬럼명 (SPLIT일 때, 없으면 빈 문자열)
                   - withdrawal: 출금 컬럼명 (SPLIT일 때, 없으면 빈 문자열)
                   - amount: 금액 컬럼명 (SINGLE일 때, 없으면 빈 문자열)

                """;
    }

    private String buildUserPrompt(String excelSample) {
        return """
                아래는 Excel 파일에서 추출한 일부 행 데이터다.
                이 데이터에서 학생회 영수증 저장에 필요한 컬럼 매핑을 찾아라.

                Excel 샘플:
                %s
                """.formatted(excelSample);
    }

    private String buildExcelSample(List<Map<Integer, String>> allRows) {
        if (allRows == null || allRows.isEmpty()) {
            throw new CustomException(EMPTY_EXCEL_DATA, 400);
        }

        StringBuilder sb = new StringBuilder();

        int limit = Math.min(MAX_SCAN_ROWS, allRows.size());

        for (int i = 0; i < limit; i++) {
            String rowText = formatRow(allRows.get(i));

            if (rowText == null || rowText.isBlank()) {
                continue;
            }

            sb.append("row ")
                    .append(i + 1)
                    .append(": ")
                    .append(rowText)
                    .append("\n");
        }

        return sb.toString();
    }
    private String formatRow(Map<Integer, String> row) {
        if (row == null || row.isEmpty()) {
            return "";
        }

        int maxIndex = row.keySet().stream()
                .max(Integer::compareTo)
                .orElse(0);

        List<String> values = new ArrayList<>();

        for (int i = 0; i <= maxIndex; i++) {
            String value = row.getOrDefault(i, "");
            values.add(value == null ? "" : value.trim());
        }

        return String.join(" | ", values);
    }

    private void validateAiResult(ExcelColumnMappingDto result) {
        if (result == null) {
            throw new CustomException(INVALID_AI_ANALYSIS_RESULT, 400);
        }

        if (result.getAmountType() == null) {
            throw new CustomException(INVALID_AI_ANALYSIS_RESULT, 400);
        }

        if (blankToNull(result.getDate()) == null) {
            throw new CustomException(INVALID_AI_ANALYSIS_RESULT, 400);
        }

        if (blankToNull(result.getContent()) == null) {
            throw new CustomException(INVALID_AI_ANALYSIS_RESULT, 400);
        }

        if (result.getAmountType().equals("SPLIT")) {
            boolean hasDeposit = blankToNull(result.getDeposit()) != null;
            boolean hasWithdrawal = blankToNull(result.getWithdrawal()) != null;

            if (!hasDeposit && !hasWithdrawal) {
                throw new CustomException(INVALID_AI_ANALYSIS_RESULT, 400);
            }
        }

        if (result.getAmountType().equals("SINGLE")) {
            if (blankToNull(result.getAmount()) == null) {
                throw new CustomException(INVALID_AI_ANALYSIS_RESULT, 400);
            }
        }
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
