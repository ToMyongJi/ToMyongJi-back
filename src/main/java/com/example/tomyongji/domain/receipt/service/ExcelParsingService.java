package com.example.tomyongji.domain.receipt.service;
import com.alibaba.excel.EasyExcel;
import com.example.tomyongji.domain.receipt.dto.ExcelColumnMappingDto;
import com.example.tomyongji.domain.receipt.dto.ExcelMappingRequest;
import com.example.tomyongji.domain.receipt.dto.ExcelParseResultResponse;
import com.example.tomyongji.domain.receipt.entity.Receipt;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.global.error.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_EXCEL_HEADER;

@Slf4j
@Service
public class ExcelParsingService {

    // 메서드 간에 전달할 헤더 매핑 정보
    public record HeaderMapping(
            int headerRowIndex,
            Map<String, Integer> columnIndexMap
    ) {}

    // 전체 파일 읽기 (딱 한 번만 실행)
    public List<Map<Integer, String>> readExcelInMemory(InputStream inputStream) {
        return EasyExcel.read(inputStream)
                .sheet()
                .headRowNumber(0)
                .doReadSync();
    }


    // 헤더 찾기
    public HeaderMapping searchHeader(List<Map<Integer, String>> allRows, ExcelColumnMappingDto config) {
        Map<String, Integer> indexMap = new HashMap<>();

        for (int i = 0; i < allRows.size(); i++) {
            Map<Integer, String> row = allRows.get(i);

            if (rowContains(row, config.getDate()) && rowContains(row, config.getContent())) {
                indexMap.put("date", findIndex(row, config.getDate()));
                indexMap.put("content", findIndex(row, config.getContent()));

                if ("SPLIT".equalsIgnoreCase(config.getAmountType())) {
                    indexMap.put("deposit", findIndex(row, config.getDeposit()));
                    indexMap.put("withdrawal", findIndex(row, config.getWithdrawal()));
                } else {
                    indexMap.put("amount", findIndex(row, config.getAmount()));
                }
                return new HeaderMapping(i, indexMap);
            }
        }
        throw new CustomException(NOT_FOUND_EXCEL_HEADER, 400);
    }


    // 3. 단일 행 파싱 (핵심 비즈니스 로직)
    public Receipt parseSingleRow(Map<Integer, String> row, HeaderMapping mapping, ExcelColumnMappingDto config, StudentClub studentClub) {
        // 빈 행 방어 로직
        if (row == null || row.isEmpty() || row.values().stream().allMatch(Objects::isNull)) {
            return null;
        }

        Map<String, Integer> idx = mapping.columnIndexMap();
        Receipt.ReceiptBuilder builder = Receipt.builder()
                .date(parseDate(row.get(idx.get("date"))))
                .content(row.get(idx.get("content")))
                .studentClub(studentClub);

        if ("SPLIT".equalsIgnoreCase(config.getAmountType())) {
            builder.deposit(parseAmount(row.get(idx.get("deposit"))));
            builder.withdrawal(parseAmount(row.get(idx.get("withdrawal"))));
        } else {
            int amount = parseAmount(row.get(idx.get("amount")));
            if (amount > 0) {
                builder.deposit(amount);
                builder.withdrawal(0);
            } else if(amount < 0){
                builder.deposit(0);
                builder.withdrawal(Math.abs(amount));
            }
        }
        return builder.build();
    }

    // =========================================================================
    // 4. 활용 1: 미리보기 파싱 (Preview)
    // =========================================================================
    public List<Receipt> getPreview(List<Map<Integer, String>> allRows, HeaderMapping mapping, ExcelColumnMappingDto config, int limit, StudentClub studentClub) {
        List<Receipt> previews = new ArrayList<>();
        int startIndex = mapping.headerRowIndex() + 1;

        for (int i = startIndex; i < allRows.size() && previews.size() < limit; i++) {
            try {
                Receipt receipt = parseSingleRow(allRows.get(i), mapping, config, studentClub);

                // 빈 행 방어
                if (receipt == null) continue;

                // 2. 유효성 검사 (parseAll과 동일한 엄격한 기준 적용)
                if (receipt.getDate() == null) continue;
                if (receipt.getContent() == null || receipt.getContent().isBlank()) continue;
                if (receipt.getDeposit() <= 0 && receipt.getWithdrawal() <= 0) continue;

                previews.add(receipt);
            } catch (Exception e) {
                log.warn("{}번 행 미리보기 파싱 실패", i);
            }
        }

        return previews;
    }

    public ExcelParseResultResponse parseAll(
            List<Map<Integer, String>> allRows,
            HeaderMapping mapping,
            ExcelColumnMappingDto config,
            StudentClub studentClub
    ) {
        List<ExcelParseResultResponse.ParseRow> rows = new ArrayList<>();
        Map<String, Integer> idx = mapping.columnIndexMap();

        int startIndex = mapping.headerRowIndex() + 1;

        for (int i = startIndex; i < allRows.size(); i++) {
            int actualExcelRow = i + 1;
            Map<Integer, String> row = allRows.get(i);

            // 빈 행 스킵
            if (row == null || row.isEmpty() || row.values().stream().allMatch(Objects::isNull)) continue;

            String rawDate = row.get(idx.get("date"));
            String rawContent = row.get(idx.get("content"));

            Receipt receipt = null;
            List<ExcelParseResultResponse.ParseError> errors = new ArrayList<>();

            try {
                receipt = parseSingleRow(row, mapping, config, studentClub);

                if (receipt == null) {
                    continue;
                }

                if (receipt.getDate() == null) {
                    errors.add(buildError("date", "날짜를 인식할 수 없습니다."));
                }

                if (receipt.getContent() == null || receipt.getContent().isBlank()) {
                    errors.add(buildError("content", "내용이 누락되었습니다."));
                }

                if (receipt.getDeposit() <= 0 && receipt.getWithdrawal() <= 0) {
                    errors.add(buildError("deposit", "유효하지 않은 금액 형식입니다."));
                    errors.add(buildError("withdrawal", "유효하지 않은 금액 형식입니다."));
                }

            } catch (Exception e) {
                errors.add(buildError("unknown", "데이터 형식을 읽을 수 없습니다."));
            }

            boolean success = errors.isEmpty();

            rows.add(ExcelParseResultResponse.ParseRow.builder()
                    .rowNumber(actualExcelRow)
                    .status(success ? ExcelParseResultResponse.ParseStatus.SUCCESS : ExcelParseResultResponse.ParseStatus.FAILED)
                    .date(receipt != null && receipt.getDate() != null ? receipt.getDate().toString() : rawDate)
                    .content(receipt != null && receipt.getContent() != null ? receipt.getContent() : rawContent)
                    .deposit(receipt != null ? receipt.getDeposit() : 0)
                    .withdrawal(receipt != null ? receipt.getWithdrawal() : 0)
                    .errors(success ? null : errors)
                    .receipt(success ? receipt : null)
                    .build());
        }

        return ExcelParseResultResponse.builder()
                .rows(rows)
                .build();
    }

    // 에러 객체 생성 헬퍼
    private ExcelParseResultResponse.ParseError buildError(String errorColumn, String message) {
        return ExcelParseResultResponse.ParseError.builder()
                .errorColumn(errorColumn)
                .errorMessage(message)
                .build();
    }

    // =========================================================================
    // 유틸리티 메서드 (안전한 추출 및 변환)
    // =========================================================================

    private boolean rowContains(Map<Integer, String> row, String targetName) {
        if (targetName == null || targetName.isBlank()) return false;
        return row.values().stream()
                .filter(Objects::nonNull)
                .anyMatch(val -> val.replace(" ", "").contains(targetName.replace(" ", "")));
    }

    private Integer findIndex(Map<Integer, String> row, String targetName) {
        if (targetName == null || targetName.isBlank()) return null;
        return row.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue().replace(" ", "").contains(targetName.replace(" ", "")))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    // "2026.04.02", "2026/04/02", "20260402" -> LocalDate 처리
    private static Date parseDate(String val) {
        if (val == null || val.isBlank()) return null;
        val = val.trim();

        // 2. 시간 정보 제거 (예: "09:12", "14:33:00" 등이 포함된 경우 잘라냄)
        val = val.replaceAll("\\d{1,2}:\\d{2}.*", "");

        // 3. 숫자 이외의 모든 문자(., /, 년, 월, 일 등)를 '-'로 변환
        // 예: "2026년 4월 2일" -> "2026-4-2" / "03/02" -> "03-02"
        val = val.replaceAll("[^0-9]+", "-").replaceAll("^-+|-+$", "");

        // 4. '-'를 기준으로 연, 월, 일 토큰 분리
        String[] parts = val.split("-");
        LocalDate localDate;

        try {
            if (parts.length == 3) {
                // Case 1: YYYY-MM-DD 또는 YY-MM-DD (예: "2026-4-2", "26-04-01")
                int year = Integer.parseInt(parts[0]);
                if (year < 100) year += 2000; // "26" -> 2026
                localDate = LocalDate.of(year, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

            } else if (parts.length == 2) {
                // Case 2: MM-DD (예: "03-02")
                int year = LocalDate.now().getYear(); // 연도 누락 시 올해 연도 삽입
                localDate = LocalDate.of(year, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));

            } else if (parts.length == 1) {
                // Case 3: 구분자가 아예 없는 8자리(YYYYMMDD) 또는 6자리(YYMMDD)
                if (parts[0].length() == 8) {
                    localDate = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyyMMdd"));
                } else if (parts[0].length() == 6) {
                    localDate = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyMMdd"));
                } else {
                    throw new IllegalArgumentException("지원하지 않는 날짜 길이: " + val);
                }
            } else {
                throw new IllegalArgumentException("지원하지 않는 날짜 형식: " + val);
            }

            return Date.valueOf(localDate);

        } catch (Exception e) {
            System.out.print(val+" "+e.getMessage());
            return null; // 시스템을 터뜨리지 않고 해당 영수증의 날짜만 비워둠
        }
    }

    // "1,200", "-500", " 100 " -> Integer 타입으로 안전하게 변환
    private Integer parseAmount(String val) {
        if (val == null || val.isBlank()) return 0;
        // 숫자와 마이너스 기호만 남기고 콤마나 띄어쓰기 제거
        String cleanNum = val.replaceAll("[^0-9\\-]", "");
        if (cleanNum.isBlank() || cleanNum.equals("-")) return 0;
        return Integer.parseInt(cleanNum);
    }
}
