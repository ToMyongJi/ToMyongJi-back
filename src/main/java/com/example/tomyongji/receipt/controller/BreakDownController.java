package com.example.tomyongji.receipt.controller;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.receipt.dto.BreakDownDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.service.BreakDownService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name="거래내역서 파싱 api", description = "PDF 거래내역서 파싱과 관련된 API들입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/breakdown")
public class BreakDownController {

    private final BreakDownService breakDownService;

    @Operation(summary = "PDF 거래내역서 파싱 api", description = "PDF 파일을 업로드하여 거래내역을 파싱합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/parse")
    public ApiResponse<List<ReceiptDto>> parsePdfFile(
            @RequestPart("file") MultipartFile file,
            @RequestPart("userId") String userId,
            @RequestPart("keyword") String keyword,
            @AuthenticationPrincipal UserDetails currentUser) throws Exception {

        BreakDownDto breakDownDto = breakDownService.parsePdf(file, userId, keyword, currentUser);
        List<ReceiptDto> receiptDtoList = breakDownService.fetchAndProcessDocument(breakDownDto);

        return new ApiResponse<>(200, "PDF 파싱을 성공적으로 완료했습니다.", receiptDtoList);
    }

    @Operation(summary = "엑셀 거래내역서 업로드 api", description = "토스뱅크 엑셀 거래내역 파일(.xlsx)을 업로드하여 영수증을 생성합니다. ")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/parse/excel")
    public ApiResponse<List<ReceiptDto>> uploadExcelFile(
            @RequestPart("file") MultipartFile file,
            @RequestPart("userId") String userId,
            @RequestPart(value = "excelPassword", required = false) String excelPassword,
            @RequestPart(value = "keyword", required = false) String keyword,
            @AuthenticationPrincipal UserDetails currentUser) {

        List<ReceiptDto> receiptDtoList = breakDownService.loadDataFromExcel(
                file, excelPassword, keyword, userId, currentUser);

        return new ApiResponse<>(200, "엑셀 파일 업로드를 성공적으로 완료했습니다.", receiptDtoList);
    }
}