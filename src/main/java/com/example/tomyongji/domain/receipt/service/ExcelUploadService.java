package com.example.tomyongji.domain.receipt.service;

import static com.example.tomyongji.global.error.ErrorMsg.*;

import com.alibaba.excel.EasyExcel;
import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.dto.ExcelMappingRuleDto;
import com.example.tomyongji.domain.receipt.dto.ExcelPreviewItemDto;
import com.example.tomyongji.domain.receipt.dto.ExcelStatusResponseDto;
import com.example.tomyongji.domain.receipt.entity.ExcelMappingRule;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.repository.ExcelMappingRuleRepository;
import com.example.tomyongji.global.error.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ExcelUploadService {

    private final UserRepository userRepository;
    private final ExcelMappingRuleRepository excelMappingRuleRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ExcelAnalyzeService excelAnalyzeService;

    private static final long REDIS_TTL_MINUTES = 10;

    public ExcelStatusResponseDto upload(MultipartFile file, UserDetails currentUser) {
        User user = userRepository.findByUserId(currentUser.getUsername())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));
        StudentClub studentClub = user.getStudentClub();
        if (studentClub == null) {
            throw new CustomException(NOT_HAVE_STUDENT_CLUB, 401);
        }

        ExcelMappingRule rule = excelMappingRuleRepository.findByStudentClub(studentClub)
            .orElseThrow(() -> new CustomException(NOT_FOUND_MAPPING_RULE, 400));

        List<Map<Integer, Object>> allRows;
        try {
            allRows = EasyExcel.read(file.getInputStream())
                .headRowNumber(0)
                .sheet(0)
                .doReadSync();
        } catch (Exception e) {
            throw new CustomException(EXCEL_PARSE_ERROR, 400);
        }

        List<ExcelPreviewItemDto> previewData = excelAnalyzeService.convertToPreview(allRows, toDto(rule));

        String requestId = UUID.randomUUID().toString();
        try {
            stringRedisTemplate.opsForValue().set(
                "excel:preview:" + requestId,
                objectMapper.writeValueAsString(previewData),
                REDIS_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new CustomException(EXCEL_PARSE_ERROR, 500);
        }

        return ExcelStatusResponseDto.builder()
            .requestId(requestId)
            .status("COMPLETED")
            .previewData(previewData)
            .build();
    }

    private ExcelMappingRuleDto toDto(ExcelMappingRule rule) {
        return new ExcelMappingRuleDto(
            rule.getDateColumn(),
            rule.getContentColumn(),
            rule.getAmountType(),
            rule.getDepositColumn(),
            rule.getWithdrawalColumn(),
            rule.getAmountColumn(),
            rule.getDataStartRow()
        );
    }
}
