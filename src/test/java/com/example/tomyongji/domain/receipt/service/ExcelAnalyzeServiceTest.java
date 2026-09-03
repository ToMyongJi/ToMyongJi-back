package com.example.tomyongji.domain.receipt.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.dto.ExcelMappingRuleDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * parseDate(String value) — package-private 단일 파라미터 시그니처 기준으로 작성된 Red Phase 테스트.
 * 현재 구현은 private parseDate(String, String)이므로 아래 테스트는 모두 컴파일/실행 실패한다.
 * 구현 완료 후 fail("구현 전") 호출을 실제 assertion으로 교체해야 한다.
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ExcelAnalyzeServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private ObjectMapper objectMapper;

    private ExcelAnalyzeService excelAnalyzeService;

    @BeforeEach
    void setUp() {
        excelAnalyzeService = new ExcelAnalyzeService(userRepository, stringRedisTemplate, objectMapper);
        ReflectionTestUtils.setField(excelAnalyzeService, "geminiApiKey", "test-api-key");
    }

    // TODO: parseDate(String) 구현 완료 후 이 헬퍼를 사용하는 assertion으로 교체
    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Nested
    @DisplayName("convertToPreview 메서드는")
    class Describe_convertToPreview {

        private ExcelMappingRuleDto splitRule() {
            // dateColumn=A(0), contentColumn=B(1), amountType=split,
            // depositColumn=C(2), withdrawalColumn=D(3), dataStartRow=1(→ index 0부터)
            ExcelMappingRuleDto rule = new ExcelMappingRuleDto();
            rule.setDateColumn("A");
            rule.setContentColumn("B");
            rule.setAmountType("split");
            rule.setDepositColumn("C");
            rule.setWithdrawalColumn("D");
            rule.setDataStartRow(1);
            rule.setDateFormat("yyyy.MM.dd");
            return rule;
        }

        @Nested
        @DisplayName("정상 row가 주어지면")
        class Context_with_valid_row {

            @Test
            @DisplayName("skippedRows는 0이고 items에 1개가 포함된다")
            void convertToPreview_success() {
                // given
                Map<Integer, Object> row = new java.util.LinkedHashMap<>();
                row.put(0, "2026.01.05");
                row.put(1, "식비");
                row.put(2, "0");
                row.put(3, "10000");
                List<Map<Integer, Object>> allRows = List.of(row);

                // when
                ExcelAnalyzeService.ConvertResult result =
                    excelAnalyzeService.convertToPreview(allRows, splitRule());

                // then
                assertThat(result.skippedRows()).isEqualTo(0);
                assertThat(result.items()).hasSize(1);
            }
        }

        @Nested
        @DisplayName("date가 null인 row가 주어지면")
        class Context_with_null_date_row {

            @Test
            @DisplayName("해당 row를 스킵하여 skippedRows는 1이고 items는 비어있다")
            void convertToPreview_skipWhenDateIsNull() {
                // given
                Map<Integer, Object> row = new java.util.LinkedHashMap<>();
                row.put(0, "");          // date 없음 → parseDate returns null
                row.put(1, "식비");
                row.put(2, "0");
                row.put(3, "10000");
                List<Map<Integer, Object>> allRows = List.of(row);

                // when
                ExcelAnalyzeService.ConvertResult result =
                    excelAnalyzeService.convertToPreview(allRows, splitRule());

                // then
                assertThat(result.skippedRows()).isEqualTo(1);
                assertThat(result.items()).isEmpty();
            }
        }

        @Nested
        @DisplayName("content가 blank인 row가 주어지면")
        class Context_with_blank_content_row {

            @Test
            @DisplayName("해당 row를 스킵하여 skippedRows는 1이고 items는 비어있다")
            void convertToPreview_skipWhenContentIsBlank() {
                // given
                Map<Integer, Object> row = new java.util.LinkedHashMap<>();
                row.put(0, "2026.01.05");
                row.put(1, "   ");       // content blank
                row.put(2, "0");
                row.put(3, "10000");
                List<Map<Integer, Object>> allRows = List.of(row);

                // when
                ExcelAnalyzeService.ConvertResult result =
                    excelAnalyzeService.convertToPreview(allRows, splitRule());

                // then
                assertThat(result.skippedRows()).isEqualTo(1);
                assertThat(result.items()).isEmpty();
            }
        }

        @Nested
        @DisplayName("deposit과 withdrawal 모두 0인 row가 주어지면")
        class Context_with_zero_amounts_row {

            @Test
            @DisplayName("해당 row를 스킵하여 skippedRows는 1이고 items는 비어있다")
            void convertToPreview_skipWhenBothAmountsAreZero() {
                // given
                Map<Integer, Object> row = new java.util.LinkedHashMap<>();
                row.put(0, "2026.01.05");
                row.put(1, "식비");
                row.put(2, "0");         // deposit = 0
                row.put(3, "0");         // withdrawal = 0
                List<Map<Integer, Object>> allRows = List.of(row);

                // when
                ExcelAnalyzeService.ConvertResult result =
                    excelAnalyzeService.convertToPreview(allRows, splitRule());

                // then
                assertThat(result.skippedRows()).isEqualTo(1);
                assertThat(result.items()).isEmpty();
            }
        }

        @Nested
        @DisplayName("정상 1개와 스킵 대상 2개가 혼합된 rows가 주어지면")
        class Context_with_mixed_rows {

            @Test
            @DisplayName("skippedRows는 2이고 items에는 정상 row 1개만 포함된다")
            void convertToPreview_mixedRows() {
                // given
                Map<Integer, Object> validRow = new java.util.LinkedHashMap<>();
                validRow.put(0, "2026.01.05");
                validRow.put(1, "식비");
                validRow.put(2, "0");
                validRow.put(3, "10000");

                Map<Integer, Object> nullDateRow = new java.util.LinkedHashMap<>();
                nullDateRow.put(0, "");      // date null → skip
                nullDateRow.put(1, "교통비");
                nullDateRow.put(2, "5000");
                nullDateRow.put(3, "0");

                Map<Integer, Object> zeroAmountRow = new java.util.LinkedHashMap<>();
                zeroAmountRow.put(0, "2026.01.06");
                zeroAmountRow.put(1, "비품");
                zeroAmountRow.put(2, "0");   // deposit = 0
                zeroAmountRow.put(3, "0");   // withdrawal = 0

                List<Map<Integer, Object>> allRows = List.of(validRow, nullDateRow, zeroAmountRow);

                // when
                ExcelAnalyzeService.ConvertResult result =
                    excelAnalyzeService.convertToPreview(allRows, splitRule());

                // then
                assertThat(result.skippedRows()).isEqualTo(2);
                assertThat(result.items()).hasSize(1);
            }
        }
    }

    @Nested
    @DisplayName("parseDate 메서드는")
    class Describe_parseDate {

        @Nested
        @DisplayName("길이가 30자를 초과하는 입력이 주어지면")
        class Context_with_value_exceeding_max_length {

            @Test
            @DisplayName("ReDoS 방어를 위해 null을 반환한다")
            void parseDate_returnsNullWhenValueExceedsMaxLength() {
                // given
                String longValue = "2026년 1월 5일 이것은 너무 긴 날짜 문자열입니다!!";
                assertThat(longValue.length()).isGreaterThan(30);

                // when & then
                Date result = excelAnalyzeService.parseDate(longValue);
                assertThat(result).isNull();
            }
        }

        @Nested
        @DisplayName("Excel serial 숫자가 유효 범위(1000 ~ 100000)로 주어지면")
        class Context_with_valid_excel_serial {

            @Test
            @DisplayName("serial을 날짜로 변환하여 null이 아닌 Date를 반환한다")
            void parseDate_returnsDateFromExcelSerial() {
                // given
                String serial = "45678.0";

                // when & then
                Date result = excelAnalyzeService.parseDate(serial);
                assertThat(result).isNotNull();
            }
        }

        @Nested
        @DisplayName("한국어 날짜 형식(yyyy년 M월 d일)이 주어지면")
        class Context_with_korean_date_format {

            @Test
            @DisplayName("2026-01-05에 해당하는 Date를 반환한다")
            void parseDate_parsesKoreanDateFormat() {
                // given
                String value = "2026년 1월 5일";

                // when & then
                Date result = excelAnalyzeService.parseDate(value);
                assertThat(result).isNotNull();
                assertThat(toLocalDate(result)).isEqualTo(LocalDate.of(2026, 1, 5));
            }
        }

        @Nested
        @DisplayName("연도 선두 점 구분자 형식(yyyy.MM.dd)이 주어지면")
        class Context_with_year_leading_dot_format {

            @Test
            @DisplayName("2026-01-05에 해당하는 Date를 반환한다")
            void parseDate_parsesYearLeadingDotFormat() {
                // given
                String value = "2026.01.05";

                // when & then
                Date result = excelAnalyzeService.parseDate(value);
                assertThat(result).isNotNull();
                assertThat(toLocalDate(result)).isEqualTo(LocalDate.of(2026, 1, 5));
            }
        }

        @Nested
        @DisplayName("연도 선두 슬래시 구분자 형식(yyyy/M/d)이 주어지면")
        class Context_with_year_leading_slash_format {

            @Test
            @DisplayName("2026-01-05에 해당하는 Date를 반환한다")
            void parseDate_parsesYearLeadingSlashFormat() {
                // given
                String value = "2026/1/5";

                // when & then
                Date result = excelAnalyzeService.parseDate(value);
                assertThat(result).isNotNull();
                assertThat(toLocalDate(result)).isEqualTo(LocalDate.of(2026, 1, 5));
            }
        }

        @Nested
        @DisplayName("8자리 숫자 형식(yyyyMMdd)이 주어지면")
        class Context_with_eight_digit_format {

            @Test
            @DisplayName("2026-01-05에 해당하는 Date를 반환한다")
            void parseDate_parsesEightDigitFormat() {
                // given
                String value = "20260105";

                // when & then
                Date result = excelAnalyzeService.parseDate(value);
                assertThat(result).isNotNull();
                assertThat(toLocalDate(result)).isEqualTo(LocalDate.of(2026, 1, 5));
            }
        }

        @Nested
        @DisplayName("연도 후미 MM/dd/yyyy 형식에서 첫 번째 숫자가 12 이하이면")
        class Context_with_year_trailing_mm_dd_format {

            @Test
            @DisplayName("MM/dd로 해석하여 2026-01-05를 반환한다")
            void parseDate_parsesYearTrailingMmDdFormat() {
                // given
                String value = "01/05/2026";

                // when & then
                Date result = excelAnalyzeService.parseDate(value);
                assertThat(result).isNotNull();
                assertThat(toLocalDate(result)).isEqualTo(LocalDate.of(2026, 1, 5));
            }
        }

        @Nested
        @DisplayName("연도 후미 dd/MM/yyyy 형식에서 첫 번째 숫자가 12 초과이면")
        class Context_with_year_trailing_dd_mm_format {

            @Test
            @DisplayName("dd/MM으로 해석하여 2026-01-15를 반환한다")
            void parseDate_parsesYearTrailingDdMmFormat() {
                // given
                String value = "15/01/2026";

                // when & then
                Date result = excelAnalyzeService.parseDate(value);
                assertThat(result).isNotNull();
                assertThat(toLocalDate(result)).isEqualTo(LocalDate.of(2026, 1, 15));
            }
        }

        @Nested
        @DisplayName("월/일만 있는 부분 날짜 형식(MM/dd)이 주어지면")
        class Context_with_partial_date_format {

            @Test
            @DisplayName("현재 연도를 사용하여 null이 아닌 Date를 반환한다")
            void parseDate_parsesPartialDateUsingCurrentYear() {
                // given
                String value = "01/05";

                // when & then
                Date result = excelAnalyzeService.parseDate(value);
                assertThat(result).isNotNull();
            }
        }

        @Nested
        @DisplayName("파싱할 수 없는 문자열이 주어지면")
        class Context_with_unparseable_string {

            @Test
            @DisplayName("null을 반환한다")
            void parseDate_returnsNullForUnparseableString() {
                // given
                String value = "abc";

                // when & then
                Date result = excelAnalyzeService.parseDate(value);
                assertThat(result).isNull();
            }
        }
    }
}
