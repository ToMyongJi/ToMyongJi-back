package com.example.tomyongji.receipt.mapper;

import com.example.tomyongji.admin.mapper.AdminMapper;
import com.example.tomyongji.receipt.dto.OCRResultDto;
import com.example.tomyongji.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.dto.ReceiptUpdateDto;
import com.example.tomyongji.receipt.entity.Receipt;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ReceiptMapper {

    ReceiptMapper INSTANCE = Mappers.getMapper(ReceiptMapper.class);

    // ReceiptCreateDto to Receipt Entity
    Receipt toReceiptEntity(ReceiptCreateDto receiptCreateDto);

    // ReceiptUpdateDto to Receipt Entity
    Receipt toReceiptEntity(ReceiptUpdateDto receiptUpdateDto);

    // ReceiptCreateDto to ReceiptDto
    ReceiptDto toReceiptDto(ReceiptCreateDto receiptCreateDto);

    // ReceiptUpdateDto to ReceiptDto
    ReceiptDto toReceiptDto(ReceiptUpdateDto receiptUpdateDto);

    // Receipt Entity to Receipt Dto
    ReceiptDto toReceiptDto(Receipt receipt);

    // OCRResultDto to Receipt Dto
    ReceiptDto toReceiptDto(OCRResultDto ocrResultDto);

    // ReceiptDto to ReceiptCreateDto
    ReceiptCreateDto toReceiptCreateDto(ReceiptDto receiptDto);



}
