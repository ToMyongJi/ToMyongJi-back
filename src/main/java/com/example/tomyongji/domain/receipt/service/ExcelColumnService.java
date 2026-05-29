package com.example.tomyongji.domain.receipt.service;


import com.example.tomyongji.domain.receipt.dto.ExcelColumnMappingDto;
import com.example.tomyongji.domain.receipt.dto.ExcelMappingRequest;
import com.example.tomyongji.domain.receipt.entity.ExcelColumn;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.repository.ExcelColumnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExcelColumnService {

    private final ExcelColumnRepository excelColumnRepository;

    // DB 조회
    public Optional<ExcelColumn> findByStudentClub(StudentClub studentClub) {
        return excelColumnRepository.findByStudentClub(studentClub);
    }

    // ExcelColumn -> ExcelColumnMappingDto 변환
    public ExcelColumnMappingDto toConfig(ExcelColumn entity) {
        return ExcelColumnMappingDto.builder()
                .amountType(entity.getAmountType())
                .date(entity.getDateColumnName())
                .content(entity.getContentColumnName())
                .deposit(entity.getDepositColumnName())
                .withdrawal(entity.getWithdrawalColumnName())
                .amount(entity.getAmountColumnName())
                .build();
    }

    // 향후 사용자가 "이대로 저장"을 눌렀을 때 호출할 저장 로직
    @Transactional
    public ExcelColumn saveOrUpdate(ExcelColumnMappingDto finalResult, StudentClub studentClub) {
        ExcelColumn excelColumn = excelColumnRepository.findByStudentClub(studentClub)
                .orElse(new ExcelColumn()); // 없으면 새로 생성, 있으면 업데이트

        excelColumn.setStudentClub(studentClub);
        excelColumn.setAmountType(finalResult.getAmountType());
        excelColumn.setDateColumnName(finalResult.getDate());
        excelColumn.setContentColumnName(finalResult.getContent());
        excelColumn.setDepositColumnName(finalResult.getDeposit());
        excelColumn.setWithdrawalColumnName(finalResult.getWithdrawal());
        excelColumn.setAmountColumnName(finalResult.getAmount());

        return excelColumnRepository.save(excelColumn);
    }
}
