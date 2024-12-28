package com.example.tomyongji.receipt.mapper;

import com.example.tomyongji.receipt.dto.OCRResultDto;
import com.example.tomyongji.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.dto.ReceiptUpdateDto;
import com.example.tomyongji.receipt.entity.Receipt;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-27T18:21:31+0900",
    comments = "version: 1.5.0.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
@Component
public class ReceiptMapperImpl implements ReceiptMapper {

    @Override
    public Receipt toReceiptEntity(ReceiptCreateDto receiptCreateDto) {
        if ( receiptCreateDto == null ) {
            return null;
        }

        Receipt.ReceiptBuilder receipt = Receipt.builder();

        receipt.date( receiptCreateDto.getDate() );
        receipt.content( receiptCreateDto.getContent() );
        receipt.deposit( receiptCreateDto.getDeposit() );
        receipt.withdrawal( receiptCreateDto.getWithdrawal() );

        return receipt.build();
    }

    @Override
    public Receipt toReceiptEntity(ReceiptUpdateDto receiptUpdateDto) {
        if ( receiptUpdateDto == null ) {
            return null;
        }

        Receipt.ReceiptBuilder receipt = Receipt.builder();

        receipt.date( receiptUpdateDto.getDate() );
        receipt.content( receiptUpdateDto.getContent() );
        receipt.deposit( receiptUpdateDto.getDeposit() );
        receipt.withdrawal( receiptUpdateDto.getWithdrawal() );

        return receipt.build();
    }

    @Override
    public ReceiptDto toReceiptDto(ReceiptCreateDto receiptCreateDto) {
        if ( receiptCreateDto == null ) {
            return null;
        }

        ReceiptDto.ReceiptDtoBuilder receiptDto = ReceiptDto.builder();

        receiptDto.date( receiptCreateDto.getDate() );
        receiptDto.content( receiptCreateDto.getContent() );
        receiptDto.deposit( receiptCreateDto.getDeposit() );
        receiptDto.withdrawal( receiptCreateDto.getWithdrawal() );

        return receiptDto.build();
    }

    @Override
    public ReceiptDto toReceiptDto(Receipt receipt) {
        if ( receipt == null ) {
            return null;
        }

        ReceiptDto.ReceiptDtoBuilder receiptDto = ReceiptDto.builder();

        if ( receipt.getId() != null ) {
            receiptDto.receiptId( receipt.getId() );
        }
        receiptDto.date( receipt.getDate() );
        receiptDto.content( receipt.getContent() );
        receiptDto.deposit( receipt.getDeposit() );
        receiptDto.withdrawal( receipt.getWithdrawal() );

        return receiptDto.build();
    }

    @Override
    public ReceiptDto toReceiptDto(OCRResultDto ocrResultDto) {
        if ( ocrResultDto == null ) {
            return null;
        }

        ReceiptDto.ReceiptDtoBuilder receiptDto = ReceiptDto.builder();

        receiptDto.date( ocrResultDto.getDate() );
        receiptDto.content( ocrResultDto.getContent() );
        receiptDto.withdrawal( ocrResultDto.getWithdrawal() );

        return receiptDto.build();
    }

    @Override
    public ReceiptCreateDto toReceiptCreateDto(ReceiptDto receiptDto) {
        if ( receiptDto == null ) {
            return null;
        }

        ReceiptCreateDto.ReceiptCreateDtoBuilder receiptCreateDto = ReceiptCreateDto.builder();

        receiptCreateDto.date( receiptDto.getDate() );
        receiptCreateDto.content( receiptDto.getContent() );
        receiptCreateDto.deposit( receiptDto.getDeposit() );
        receiptCreateDto.withdrawal( receiptDto.getWithdrawal() );

        return receiptCreateDto.build();
    }
}
