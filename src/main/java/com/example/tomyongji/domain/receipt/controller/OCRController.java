package com.example.tomyongji.domain.receipt.controller;

import com.example.tomyongji.global.common.response.ApiResponse;
import com.example.tomyongji.domain.receipt.dto.OCRResultDto;
import com.example.tomyongji.domain.receipt.service.OCRService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "OCR api", description = "영수증 이미지를 OCR로 스캔하여 업로드하는 API들입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ocr")
public class OCRController {

    private final OCRService ocrService;


    @Operation(summary = "영수증 업로드 api", description = "유저 아이디를 통해 특정 학생회의 영수증을 ocr 스캔을 통해 업로드합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/upload/{userId}")
    public ResponseEntity<ApiResponse<OCRResultDto>> uploadImageAndExtractText(@RequestPart("file") MultipartFile file, @PathVariable("userId") String userId, @AuthenticationPrincipal
    UserDetails currentUser) {
        OCRResultDto result = ocrService.processImage(file);
        ocrService.uploadOcrReceipt(result, userId, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.onCreated(result)
        );

    }
}




