package com.example.tomyongji.domain.receipt.controller;

import com.example.tomyongji.domain.receipt.dto.ExcelAnalyzeResponse;
import com.example.tomyongji.domain.receipt.dto.ExcelMappingRequest;
import com.example.tomyongji.domain.receipt.dto.ExcelReceiptSaveResponse;
import com.example.tomyongji.domain.receipt.dto.ExcelParseResultResponse;
import com.example.tomyongji.domain.receipt.service.ExcelService;
import com.example.tomyongji.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ExcelAnalyzeResponse>> analyze(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "forceAnalyze", defaultValue = "false") boolean forceAnalyze,
            @AuthenticationPrincipal UserDetails currentUser
            ) throws Exception {

        ExcelAnalyzeResponse response = excelService.analyzeExcel(file, forceAnalyze, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<ExcelParseResultResponse>> previewExcelData(
            @RequestBody ExcelMappingRequest request,
            @AuthenticationPrincipal UserDetails currentUser) throws Exception {

        // DB 저장 없이 파싱 결과만 반환
        ExcelParseResultResponse response = excelService.getPreviewOnly(request, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<ExcelReceiptSaveResponse>> saveExcelData(
            @RequestBody ExcelMappingRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) throws Exception {

        // 1. Service 로직 호출 (트랜잭션 시작 -> DB 저장 -> S3 삭제 -> 결과 리포트 반환)
        ExcelReceiptSaveResponse response = excelService.saveExcelData(request, currentUser.getUsername());

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}