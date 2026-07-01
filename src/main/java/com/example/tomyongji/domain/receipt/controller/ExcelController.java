package com.example.tomyongji.domain.receipt.controller;

import com.example.tomyongji.domain.receipt.dto.ExcelExportDto;
import com.example.tomyongji.domain.receipt.service.ExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "excel api", description = "엑셀 파일로 영수증 데이터를 내보냅니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/excel")
public class ExcelController {

    private final ExcelService excelService;

    @Operation(summary = "Excel 내보내기 api", description = "영수증 데이터를 Excel 파일로 내보냅니다.")
    @PostMapping("/export")
    public void exportExcel(
            @RequestBody ExcelExportDto excelExportDto,
            HttpServletResponse response,
            @AuthenticationPrincipal UserDetails currentUser) {
        excelService.writeExcel(response, excelExportDto, currentUser);
    }
}
