package com.example.tomyongji.receipt.mapper;

import com.example.tomyongji.admin.mapper.AdminMapper;
import com.example.tomyongji.receipt.dto.OCRResultDto;
import com.example.tomyongji.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.dto.ReceiptUpdateDto;
import com.example.tomyongji.receipt.entity.Receipt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    // Receipt Entity to Receipt Dto
    @Mapping(source = "id", target = "receiptId")
    ReceiptDto toReceiptDto(Receipt receipt);

    // OCRResultDto to Receipt Dto
    ReceiptDto toReceiptDto(OCRResultDto ocrResultDto);

    // ReceiptDto to ReceiptCreateDto
    ReceiptCreateDto toReceiptCreateDto(ReceiptDto receiptDto);



}
