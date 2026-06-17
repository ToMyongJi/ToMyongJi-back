package com.example.tomyongji.domain.receipt.service;

import static com.example.tomyongji.global.error.ErrorMsg.*;

import com.alibaba.excel.EasyExcel;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.dto.ExcelMappingRuleDto;
import com.example.tomyongji.domain.receipt.dto.ExcelPreviewItemDto;
import com.example.tomyongji.domain.receipt.dto.ExcelStatusResponseDto;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.global.error.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelAnalyzeService {

    public record ConvertResult(List<ExcelPreviewItemDto> items, int skippedRows) {}

    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    private Client geminiClient;
    private static final long REDIS_TTL_MINUTES = 10;

    @PostConstruct
    private void initGeminiClient() {
        geminiClient = Client.builder().apiKey(geminiApiKey).build();
    }

    public ExcelStatusResponseDto analyze(MultipartFile file, UserDetails currentUser) {
        var user = userRepository.findByUserId(currentUser.getUsername())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));
        StudentClub studentClub = user.getStudentClub();
        if (studentClub == null) {
            throw new CustomException(NOT_HAVE_STUDENT_CLUB, 401);
        }

        List<Map<Integer, Object>> allRows;
        try {
            allRows = EasyExcel.read(file.getInputStream())
                .headRowNumber(0)
                .sheet(0)
                .doReadSync();
        } catch (Exception e) {
            throw new CustomException(EXCEL_PARSE_ERROR, 400);
        }

        if (allRows == null || allRows.isEmpty()) {
            throw new CustomException(EXCEL_PARSE_ERROR, 400);
        }

        String csvPreviewText = buildCsvText(allRows, Math.min(allRows.size(), 11));
        String requestId = UUID.randomUUID().toString();

        try {
            String prompt = buildPrompt(csvPreviewText);

            String responseText = geminiClient.models.generateContent(
                "gemini-2.5-flash",
                prompt,
                GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .build()
            ).text();

            ExcelMappingRuleDto rule = objectMapper.readValue(responseText, ExcelMappingRuleDto.class);
            ConvertResult convertResult = convertToPreview(allRows, rule);
            List<ExcelPreviewItemDto> allPreviewData = convertResult.items();

            int dataStartIdx = (rule.getDataStartRow() != null ? rule.getDataStartRow() : 2) - 1;
            int totalRows = Math.max(0, allRows.size() - dataStartIdx);

            // confirm 시 전체 데이터 insert를 위해 Redis에는 전체 저장
            stringRedisTemplate.opsForValue().set(
                "excel:preview:" + requestId,
                objectMapper.writeValueAsString(allPreviewData),
                REDIS_TTL_MINUTES, TimeUnit.MINUTES);
            stringRedisTemplate.opsForValue().set(
                "excel:mapping:" + requestId,
                objectMapper.writeValueAsString(rule),
                REDIS_TTL_MINUTES, TimeUnit.MINUTES);

            List<ExcelPreviewItemDto> previewSample = allPreviewData.subList(0, Math.min(5, allPreviewData.size()));
            return ExcelStatusResponseDto.builder()
                .requestId(requestId)
                .status("COMPLETED")
                .previewData(previewSample)
                .skippedRows(convertResult.skippedRows())
                .totalRows(totalRows)
                .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini AI 분석 실패 - requestId: {}, error: {}", requestId, e.getMessage(), e);
            throw new CustomException(GEMINI_API_ERROR, 500);
        }
    }

    ConvertResult convertToPreview(List<Map<Integer, Object>> allRows, ExcelMappingRuleDto rule) {
        List<ExcelPreviewItemDto> result = new ArrayList<>();
        int startIdx = (rule.getDataStartRow() != null ? rule.getDataStartRow() : 2) - 1;
        int skippedRows = 0;

        for (int i = startIdx; i < allRows.size(); i++) {
            Map<Integer, Object> row = allRows.get(i);
            try {
                Date date = parseDate(getString(row, columnNameToIndex(rule.getDateColumn())));
                String content = getString(row, columnNameToIndex(rule.getContentColumn()));
                if (date == null || content == null || content.isBlank()) {
                    skippedRows++;
                    continue;
                }

                int deposit = 0, withdrawal = 0;
                if ("split".equals(rule.getAmountType())) {
                    String depCol = rule.getDepositColumn();
                    String withCol = rule.getWithdrawalColumn();
                    deposit = (depCol != null) ? parseAmount(getString(row, columnNameToIndex(depCol))) : 0;
                    withdrawal = (withCol != null) ? parseAmount(getString(row, columnNameToIndex(withCol))) : 0;
                } else {
                    String amtCol = rule.getAmountColumn();
                    if (amtCol != null) {
                        int amount = parseAmount(getString(row, columnNameToIndex(amtCol)));
                        if (amount >= 0) deposit = amount;
                        else withdrawal = -amount;
                    }
                }

                if (deposit == 0 && withdrawal == 0) {
                    skippedRows++;
                    continue;
                }

                result.add(ExcelPreviewItemDto.builder()
                    .date(date).content(content).deposit(deposit).withdrawal(withdrawal).build());
            } catch (Exception ignored) {}
        }
        return new ConvertResult(result, skippedRows);
    }

    private String buildCsvText(List<Map<Integer, Object>> rows, int maxRows) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(rows.size(), maxRows); i++) {
            Map<Integer, Object> row = rows.get(i);
            int maxCol = row.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
            StringJoiner joiner = new StringJoiner(",");
            for (int j = 0; j <= maxCol; j++) {
                Object val = row.get(j);
                joiner.add(val != null ? val.toString().trim() : "");
            }
            sb.append(joiner).append("\n");
        }
        return sb.toString();
    }

    private String buildPrompt(String csvText) {
        return "아래 엑셀 데이터를 분석하여 반드시 다음 JSON 형식으로만 응답하세요.\n"
            + "amount_type이 'split'이면 deposit_column, withdrawal_column을 채우고 amount_column은 null로,\n"
            + "'single'이면 amount_column을 채우고 deposit_column/withdrawal_column은 null로 하세요.\n"
            + "single일 때 양수=입금, 음수=출금입니다.\n"
            + "date_format은 데이터에서 관찰된 날짜 패턴을 Java DateTimeFormatter 형식으로 반환하세요.\n"
            + "예: 'yyyy-MM-dd', 'yyyy년 M월 d일', 'MM/dd/yyyy', 'yyyy.MM.dd'\n\n"
            + "{\n"
            + "  \"date_column\": \"A\",\n"
            + "  \"content_column\": \"B\",\n"
            + "  \"amount_type\": \"split\",\n"
            + "  \"deposit_column\": \"C\",\n"
            + "  \"withdrawal_column\": \"D\",\n"
            + "  \"amount_column\": null,\n"
            + "  \"data_start_row\": 2,\n"
            + "  \"date_format\": \"yyyy-MM-dd\"\n"
            + "}\n\n"
            + "엑셀 데이터:\n" + csvText;
    }

    private String getString(Map<Integer, Object> row, int idx) {
        Object val = row.get(idx);
        return val != null ? val.toString().trim() : "";
    }

    Date parseDate(String value) {
        if (value == null || value.isBlank()) return null;

        // ReDoS 방어: 30자 초과 입력 거부
        if (value.length() > 30) return null;

        // Step 1 — Excel serial: 숫자 파싱 후 1000 < serial < 100000
        try {
            double serial = Double.parseDouble(value.replaceAll(",", ""));
            if (serial > 1000 && serial < 100000) {
                return fromExcelSerial(serial);
            }
        } catch (NumberFormatException ignored) {}

        Matcher m;

        // Step 2 — 한국어: yyyy년 M월 d일
        m = Pattern.compile("(\\d{4})년\\s*(\\d{1,2})월\\s*(\\d{1,2})일").matcher(value);
        if (m.find()) {
            try {
                LocalDate localDate = LocalDate.of(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)));
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (Exception ignored) {}
        }

        // Step 3 — 연도 선두: yyyy.MM.dd / yyyy-MM-dd / yyyy/MM/dd
        m = Pattern.compile("(\\d{4})[.\\-/](\\d{1,2})[.\\-/](\\d{1,2})").matcher(value);
        if (m.find()) {
            try {
                LocalDate localDate = LocalDate.of(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)));
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (Exception ignored) {}
        }

        // Step 4 — 8자리: yyyyMMdd
        m = Pattern.compile("^\\d{8}$").matcher(value.trim());
        if (m.matches()) {
            try {
                LocalDate localDate = LocalDate.parse(value.trim(), DateTimeFormatter.ofPattern("yyyyMMdd"));
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {}
        }

        // Step 5 — 연도 후미: MM/dd/yyyy 또는 dd/MM/yyyy (첫 번째 숫자 > 12이면 dd/MM)
        m = Pattern.compile("(\\d{1,2})[.\\-/](\\d{1,2})[.\\-/](\\d{4})").matcher(value);
        if (m.find()) {
            try {
                int first = Integer.parseInt(m.group(1));
                int second = Integer.parseInt(m.group(2));
                int year = Integer.parseInt(m.group(3));
                int month, day;
                if (first > 12) {
                    // dd/MM/yyyy
                    day = first;
                    month = second;
                } else {
                    // MM/dd/yyyy
                    month = first;
                    day = second;
                }
                LocalDate localDate = LocalDate.of(year, month, day);
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (Exception ignored) {}
        }

        // Step 6 — 부분 날짜: MM/dd (현재 연도 사용)
        m = Pattern.compile("^(\\d{1,2})[.\\-/](\\d{1,2})$").matcher(value.trim());
        if (m.matches()) {
            try {
                int month = Integer.parseInt(m.group(1));
                int day = Integer.parseInt(m.group(2));
                LocalDate localDate = LocalDate.of(LocalDate.now().getYear(), month, day);
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (Exception ignored) {}
        }

        // Step 7 — 모두 실패 시 null 반환
        return null;
    }

    private Date fromExcelSerial(double serial) {
        // Excel serial: 1899-12-30 기준 일수 (1900 윤년 버그 보정)
        LocalDate date = LocalDate.of(1899, 12, 30).plusDays((long) serial);
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private int parseAmount(String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return (int) Double.parseDouble(value.replaceAll("[,\\s원]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    int columnNameToIndex(String col) {
        if (col == null || col.isBlank()) return 0;
        int result = 0;
        for (char c : col.toUpperCase().toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result - 1;
    }
}
