package com.example.tomyongji.receipt.controller;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.receipt.dto.CsvExportDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.service.CSVService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "csv api", description = "이전 excel csv에 저장된 영수증 파일을 불러옵니다. ")
@Slf4j
@RestController
@RequestMapping("api/csv")
public class CsvController {

    private final CSVService csvService;

    @Autowired
    public CsvController(CSVService csvService){
        this.csvService = csvService;
    }

    @PostMapping("/upload/{userIndexId}")
    public ApiResponse readCsv(@RequestPart("file") MultipartFile file, @PathVariable long userIndexId, @AuthenticationPrincipal
        UserDetails currentUser) {
        List<Receipt> receipts = csvService.loadDataFromCSV(file,userIndexId, currentUser);
        return new ApiResponse(HttpStatus.OK.value(), "CSV file loaded successfully.", receipts);
    }

    @PostMapping("/export")
    public void exportCsv(@RequestBody CsvExportDto csvExportDto, HttpServletResponse response, @AuthenticationPrincipal UserDetails currentUser) {
        csvService.writeCsv(response,csvExportDto, currentUser);
    }
}
