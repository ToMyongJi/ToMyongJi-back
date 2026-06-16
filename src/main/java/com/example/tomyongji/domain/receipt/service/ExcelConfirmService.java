package com.example.tomyongji.domain.receipt.service;

import static com.example.tomyongji.global.error.ErrorMsg.*;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.dto.ExcelMappingRuleDto;
import com.example.tomyongji.domain.receipt.dto.ExcelPreviewItemDto;
import com.example.tomyongji.domain.receipt.entity.ExcelMappingRule;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.repository.ExcelMappingRuleRepository;
import com.example.tomyongji.domain.receipt.repository.StudentClubRepository;
import com.example.tomyongji.domain.receipt.dto.ExcelConfirmRequestDto;
import com.example.tomyongji.global.error.CustomException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelConfirmService {

    private final UserRepository userRepository;
    private final StudentClubRepository studentClubRepository;
    private final ExcelMappingRuleRepository excelMappingRuleRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ReceiptService receiptService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public int confirm(ExcelConfirmRequestDto requestDto, UserDetails currentUser) {
        User user = userRepository.findByUserId(currentUser.getUsername())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));
        StudentClub studentClub = studentClubRepository.findById(requestDto.getStudentClubId())
            .orElseThrow(() -> new CustomException(NOT_FOUND_STUDENT_CLUB, 404));
        if (!studentClub.equals(user.getStudentClub())) {
            throw new CustomException(NO_AUTHORIZATION_BELONGING, 400);
        }

        String previewJson = stringRedisTemplate.opsForValue().get("excel:preview:" + requestDto.getRequestId());
        if (previewJson == null) {
            throw new CustomException(EXCEL_PREVIEW_EXPIRED, 400);
        }

        List<ExcelPreviewItemDto> previewData;
        try {
            previewData = objectMapper.readValue(previewJson, new TypeReference<>() {});
        } catch (Exception e) {
            throw new CustomException(EXCEL_PREVIEW_EXPIRED, 400);
        }

        bulkInsertReceipts(previewData, studentClub);
        updateBalance(studentClub, previewData);
        saveMappingRuleIfPresent(requestDto.getRequestId(), studentClub);
        clearRedisKeys(requestDto.getRequestId());

        receiptService.clearReceiptCache(studentClub.getId());
        receiptService.checkAndUpdateVerificationStatus(studentClub.getId());

        return previewData.size();
    }

    private void bulkInsertReceipts(List<ExcelPreviewItemDto> previewData, StudentClub studentClub) {
        jdbcTemplate.batchUpdate(
            "INSERT INTO receipt (date, content, deposit, withdrawal, verification, student_club_id) VALUES (?, ?, ?, ?, false, ?)",
            new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ExcelPreviewItemDto item = previewData.get(i);
                    ps.setDate(1, new java.sql.Date(item.getDate().getTime()));
                    ps.setString(2, item.getContent());
                    ps.setInt(3, item.getDeposit());
                    ps.setInt(4, item.getWithdrawal());
                    ps.setLong(5, studentClub.getId());
                }
                @Override
                public int getBatchSize() {
                    return previewData.size();
                }
            }
        );
    }

    private void updateBalance(StudentClub studentClub, List<ExcelPreviewItemDto> previewData) {
        int totalDeposit = previewData.stream().mapToInt(ExcelPreviewItemDto::getDeposit).sum();
        int totalWithdrawal = previewData.stream().mapToInt(ExcelPreviewItemDto::getWithdrawal).sum();
        studentClub.setBalance(studentClub.getBalance() + totalDeposit - totalWithdrawal);
    }

    private void saveMappingRuleIfPresent(String requestId, StudentClub studentClub) {
        String mappingJson = stringRedisTemplate.opsForValue().get("excel:mapping:" + requestId);
        if (mappingJson == null) return;

        try {
            ExcelMappingRuleDto dto = objectMapper.readValue(mappingJson, ExcelMappingRuleDto.class);
            ExcelMappingRule rule = excelMappingRuleRepository.findByStudentClub(studentClub)
                .orElse(new ExcelMappingRule());
            rule.setStudentClub(studentClub);
            rule.setDateColumn(dto.getDateColumn());
            rule.setContentColumn(dto.getContentColumn());
            rule.setAmountType(dto.getAmountType());
            rule.setDepositColumn(dto.getDepositColumn());
            rule.setWithdrawalColumn(dto.getWithdrawalColumn());
            rule.setAmountColumn(dto.getAmountColumn());
            rule.setDataStartRow(dto.getDataStartRow());
            rule.setDateFormat(dto.getDateFormat());
            rule.setUpdatedAt(LocalDateTime.now());
            excelMappingRuleRepository.save(rule);
        } catch (Exception ignored) {}
    }

    private void clearRedisKeys(String requestId) {
        stringRedisTemplate.delete("excel:preview:" + requestId);
        stringRedisTemplate.delete("excel:mapping:" + requestId);
    }
}
