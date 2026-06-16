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

    public enum DateOrder {
        YMD, MDY, DMY
    }
    // 메서드 간에 전달할 헤더 매핑 정보
    public record HeaderMapping(
            int headerRowIndex,
            Map<String, Integer> columnIndexMap,
            DateOrder dateOrder
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

                // orderDate 검증 및 설정
                DateOrder finalOrder = validateDateOrder(allRows, config, indexMap, i);
                return new HeaderMapping(i, indexMap, finalOrder);
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
                .date(parseDate(row.get(idx.get("date")),mapping.dateOrder()))
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

    private DateOrder validateDateOrder(List<Map<Integer, String>> allRows, ExcelColumnMappingDto config, Map<String, Integer> indexMap, int headerRow) {
        List<String> rawDates = new ArrayList<>();
        int dateColIdx = indexMap.get("date");
        int scanLimit = Math.min(headerRow + 101, allRows.size());

        for (int j = headerRow + 1; j < scanLimit; j++) {
            rawDates.add(allRows.get(j).get(dateColIdx));
        }

        DateOrder suggestedOrder = null;
        try {
            if (config.getDateOrder() != null) {
                suggestedOrder = DateOrder.valueOf(config.getDateOrder().toUpperCase());
            }
        } catch (IllegalArgumentException ignored) {}

        DateOrder finalOrder = detectDateOrder(rawDates, suggestedOrder);
        return finalOrder;
    }

    private DateOrder detectDateOrder(List<String> rawDates, DateOrder suggestedOrder) {
        for (String rawDate : rawDates) {
            if (rawDate == null || rawDate.isBlank()) continue;
            if (rawDate.matches("^\\d{5}$")) continue;

            String cleanDate = rawDate.replaceAll("\\d{1,2}:\\d{2}.*", "").replaceAll("[^0-9]+", "-").replaceAll("^-+|-+$", "");
            String[] parts = cleanDate.split("-");

            if (parts.length == 3) {
                try {
                    int part1 = Integer.parseInt(parts[0]);
                    int part2 = Integer.parseInt(parts[1]);
                    int part3 = Integer.parseInt(parts[2]);

                    // 확실한 증거(13 이상)가 발견되면 AI의 의견을 무시하고 수학적 사실을 우선함
                    if (part1 > 31 || (part1 >= 20 && part2 <= 12 && part3 <= 31)) return DateOrder.YMD;

                    if (part3 > 31) {
                        if (part2 > 12) return DateOrder.MDY;
                        if (part1 > 12) return DateOrder.DMY;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        // 확실한 증거를 못 찾은 모호한 상황일 때 기존 결과를 신뢰
        if (suggestedOrder != null) {
            return suggestedOrder;
        }
        // default 값은 YMD
        return DateOrder.YMD;
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


    // 날짜 파싱 메서드
    private static Date parseDate(String val, DateOrder order) {
        if (val == null || val.isBlank()) return null;
        val = val.trim();

        if (val.matches("^\\d{5}$")) {
            LocalDate excelDate = LocalDate.of(1899, 12, 30).plusDays(Long.parseLong(val));
            return Date.valueOf(excelDate);
        }

        val = val.replaceAll("\\d{1,2}:\\d{2}.*", "").replaceAll("[^0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        String[] parts = val.split("-");
        LocalDate localDate;

        try {
            if (parts.length == 3) {
                int p1 = Integer.parseInt(parts[0]);
                int p2 = Integer.parseInt(parts[1]);
                int p3 = Integer.parseInt(parts[2]);
                int year, month, day;

                if (order == DateOrder.MDY) {
                    year = p3 < 100 ? p3 + 2000 : p3; month = p1; day = p2;
                } else if (order == DateOrder.DMY) {
                    year = p3 < 100 ? p3 + 2000 : p3; month = p2; day = p1;
                } else { // YMD
                    year = p1 < 100 ? p1 + 2000 : p1; month = p2; day = p3;
                }
                localDate = LocalDate.of(year, month, day);

            } else if (parts.length == 2) { // MM-DD, DD-MM
                int currentYear = LocalDate.now().getYear();
                int month, day;

                if (order == DateOrder.DMY) {
                    month = Integer.parseInt(parts[1]);
                    day = Integer.parseInt(parts[0]);
                } else {
                    month = Integer.parseInt(parts[0]);
                    day = Integer.parseInt(parts[1]);
                }

                localDate = LocalDate.of(currentYear, month, day);

                if (localDate.isAfter(LocalDate.now())) {
                    localDate = localDate.minusYears(1);
                }

            } else if (parts.length == 1) { // yyyyMMdd, yyMMdd
                if (parts[0].length() == 8) {
                    localDate = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyyMMdd"));
                } else if (parts[0].length() == 6) {
                    localDate = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyMMdd"));
                } else {
                    return null;
                }
            } else {
                return null;
            }

            return Date.valueOf(localDate);

        } catch (Exception e) {
            log.debug("날짜 파싱 실패 ({}): {}", val, e.getMessage());
            return null;
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
