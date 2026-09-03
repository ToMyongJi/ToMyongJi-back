package com.example.tomyongji.domain.receipt.controller;

import com.example.tomyongji.global.annotation.ApiErrorExample;
import com.example.tomyongji.global.annotation.ApiErrorExamples;
import com.example.tomyongji.global.common.response.ApiResponse;
import com.example.tomyongji.global.error.ErrorMsg;
import com.example.tomyongji.domain.receipt.dto.CsvExportDto;
import com.example.tomyongji.domain.receipt.entity.Receipt;
import com.example.tomyongji.domain.receipt.service.CSVService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "csv api", description = "이전 excel csv에 저장된 영수증 파일을 불러옵니다. ")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/csv")
public class CsvController {

    private final CSVService csvService;

    @Operation(summary = "CSV 업로드 api", description = "엑셀 CSV 파일을 업로드하여 영수증 데이터를 불러옵니다.")
    @ApiErrorExamples({
        @ApiErrorExample(status = 400, message = ErrorMsg.NOT_FOUND_USER),
        @ApiErrorExample(status = 400, message = ErrorMsg.NO_AUTHORIZATION_BELONGING)
    })
    @PostMapping("/upload/{userIndexId}")
    public ResponseEntity<ApiResponse<List<Receipt>>> readCsv(@RequestPart("file") MultipartFile file, @PathVariable long userIndexId, @AuthenticationPrincipal
        UserDetails currentUser) {
        List<Receipt> receipts = csvService.loadDataFromCSV(file,userIndexId, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.onSuccess(receipts)
        );
    }

    @Operation(summary = "CSV 내보내기 api", description = "영수증 데이터를 CSV 파일로 내보냅니다.")
    @ApiErrorExamples({
        @ApiErrorExample(status = 400, message = ErrorMsg.NOT_FOUND_USER),
        @ApiErrorExample(status = 400, message = ErrorMsg.NO_AUTHORIZATION_BELONGING)
    })
    @PostMapping("/export")
    public ResponseEntity<ApiResponse<Void>> exportCsv(@RequestBody CsvExportDto csvExportDto, HttpServletResponse response, @AuthenticationPrincipal UserDetails currentUser) {
        csvService.writeCsv(response,csvExportDto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.onSuccess(null)
        );
    }
}
