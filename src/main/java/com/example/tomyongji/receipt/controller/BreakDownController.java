package com.example.tomyongji.receipt.controller;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.receipt.dto.BreakDownDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.service.BreakDownService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name="거래내역서 파싱 api", description = "PDF 거래내역서 파싱과 관련된 API들입니다.")
@RestController
@RequestMapping("/api/breakdown")
public class BreakDownController {

    private final BreakDownService breakDownService;

    @Autowired
    public BreakDownController(BreakDownService breakDownService) {
        this.breakDownService = breakDownService;
    }

    @Operation(summary = "PDF 거래내역서 파싱 api", description = "PDF 파일을 업로드하여 거래내역을 파싱합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/parse")
    public ApiResponse<BreakDownDto> parsePdfFile(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails currentUser) throws Exception {

        BreakDownDto breakDownDto = breakDownService.parsePdf(file, currentUser);
        breakDownService.fetchAndProcessDocument(breakDownDto);
        return new ApiResponse<>(200, "PDF 파싱을 성공적으로 완료했습니다.", breakDownDto);
    }
}