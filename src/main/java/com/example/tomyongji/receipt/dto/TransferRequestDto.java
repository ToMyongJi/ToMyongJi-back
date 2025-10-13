package com.example.tomyongji.receipt.dto;

import com.example.tomyongji.admin.dto.PresidentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {
    private int year;
    private PresidentDto nextPresident;
}
