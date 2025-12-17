package com.example.tomyongji.receipt.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PagingReceiptDto {
    private List<ReceiptDto> receiptDtoList;       // 실제 데이터 리스트 (영수증 10개)
    private int pageNumber;        // 현재 페이지 번호 (0부터 시작)
    private int pageSize;          // 페이지 크기 (10)
    private long totalElements;    // 전체 데이터 개수 (예: 153개)
    private int totalPages;        // 전체 페이지 수 (예: 16페이지)
    private boolean last;          // 이게 마지막 페이지인지 여부

    public static PagingReceiptDto from(Page<ReceiptDto> page) {
        return PagingReceiptDto.builder()
            .receiptDtoList(page.getContent()) // Page 안의 List 꺼내기
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }
}