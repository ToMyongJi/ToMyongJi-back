package com.example.tomyongji.domain.receipt.dto;

import com.example.tomyongji.domain.admin.dto.PresidentDto;
import lombok.Getter;

import java.util.List;

@Getter
public class ClubTransferRequestDto {
    private PresidentDto presidentInfo; // 기존 회장 정보
    private List<String> remainingMemberIds;
}
