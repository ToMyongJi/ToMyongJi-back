package com.example.tomyongji.domain.receipt.controller;

import com.example.tomyongji.domain.receipt.dto.ExcelConfirmRequestDto;
import com.example.tomyongji.domain.receipt.dto.ExcelStatusResponseDto;
import com.example.tomyongji.domain.receipt.service.ExcelAnalyzeService;
import com.example.tomyongji.domain.receipt.service.ExcelConfirmService;
import com.example.tomyongji.domain.receipt.service.ExcelUploadService;
import com.example.tomyongji.global.annotation.ApiErrorExample;
import com.example.tomyongji.global.annotation.ApiErrorExamples;
import com.example.tomyongji.global.common.response.ApiResponse;
import com.example.tomyongji.global.error.ErrorMsg;
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

    @ApiErrorExamples({
        @ApiErrorExample(status = 400, message = ErrorMsg.NOT_FOUND_USER),
        @ApiErrorExample(status = 400, message = ErrorMsg.EXCEL_PARSE_ERROR),
        @ApiErrorExample(status = 401, message = ErrorMsg.NOT_HAVE_STUDENT_CLUB),
        @ApiErrorExample(status = 500, message = ErrorMsg.GEMINI_API_ERROR)
    })
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<ExcelStatusResponseDto>> analyze(
        @RequestPart("file") MultipartFile file,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        ExcelStatusResponseDto result = excelAnalyzeService.analyze(file, currentUser);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @ApiErrorExamples({
        @ApiErrorExample(status = 400, message = ErrorMsg.NOT_FOUND_USER),
        @ApiErrorExample(status = 400, message = ErrorMsg.NOT_FOUND_MAPPING_RULE),
        @ApiErrorExample(status = 400, message = ErrorMsg.EXCEL_PARSE_ERROR),
        @ApiErrorExample(status = 401, message = ErrorMsg.NOT_HAVE_STUDENT_CLUB),
        @ApiErrorExample(status = 422, message = ErrorMsg.MAPPING_RULE_MISMATCH),
        @ApiErrorExample(status = 500, message = ErrorMsg.EXCEL_REDIS_ERROR)
    })
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<ExcelStatusResponseDto>> upload(
        @RequestPart("file") MultipartFile file,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        ExcelStatusResponseDto result = excelUploadService.upload(file, currentUser);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @ApiErrorExamples({
        @ApiErrorExample(status = 400, message = ErrorMsg.NOT_FOUND_USER),
        @ApiErrorExample(status = 400, message = ErrorMsg.NO_AUTHORIZATION_BELONGING),
        @ApiErrorExample(status = 400, message = ErrorMsg.EXCEL_PREVIEW_EXPIRED),
        @ApiErrorExample(status = 404, message = ErrorMsg.NOT_FOUND_STUDENT_CLUB)
    })
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Integer>> confirm(
        @RequestBody ExcelConfirmRequestDto requestDto,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        int savedCount = excelConfirmService.confirm(requestDto, currentUser);
        return ResponseEntity.ok(ApiResponse.onSuccess(savedCount));
    }
}
