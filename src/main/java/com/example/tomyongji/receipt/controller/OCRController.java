package com.example.tomyongji.receipt.controller;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.auth.service.CustomUserDetails;
import com.example.tomyongji.receipt.dto.OCRResultDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.service.OCRService;
import com.example.tomyongji.receipt.service.ReceiptService;
import com.example.tomyongji.validation.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ocr")
public class OCRController {

    private final OCRService ocrService;


    @Operation(summary = "영수증 업로드 api", description = "유저 아이디를 통해 특정 학생회의 영수증을 ocr 스캔을 통해 업로드합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/upload/{userId}")
    public ApiResponse<OCRResultDto> uploadImageAndExtractText(@RequestPart("file") MultipartFile file, @PathVariable("userId") String userId, @AuthenticationPrincipal
    UserDetails currentUser) {
        OCRResultDto result = ocrService.processImage(file);
        ocrService.uploadOcrReceipt(result, userId, currentUser);
        return new ApiResponse<>(201, "영수증을 성공적으로 업로드했습니다.", result);

    }
}




