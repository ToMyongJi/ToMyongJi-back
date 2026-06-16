package com.example.tomyongji.domain.receipt.controller;

import com.example.tomyongji.domain.receipt.dto.ExcelConfirmRequestDto;
import com.example.tomyongji.domain.receipt.dto.ExcelStatusResponseDto;
import com.example.tomyongji.domain.receipt.service.ExcelAnalyzeService;
import com.example.tomyongji.domain.receipt.service.ExcelConfirmService;
import com.example.tomyongji.domain.receipt.service.ExcelUploadService;
import com.example.tomyongji.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelAnalyzeService excelAnalyzeService;
    private final ExcelUploadService excelUploadService;
    private final ExcelConfirmService excelConfirmService;

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<ExcelStatusResponseDto>> analyze(
        @RequestPart("file") MultipartFile file,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        ExcelStatusResponseDto result = excelAnalyzeService.analyze(file, currentUser);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<ExcelStatusResponseDto>> upload(
        @RequestPart("file") MultipartFile file,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        ExcelStatusResponseDto result = excelUploadService.upload(file, currentUser);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Integer>> confirm(
        @RequestBody ExcelConfirmRequestDto requestDto,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        int savedCount = excelConfirmService.confirm(requestDto, currentUser);
        return ResponseEntity.ok(ApiResponse.onSuccess(savedCount));
    }
}
