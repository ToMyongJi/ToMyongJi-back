package com.example.tomyongji.receipt.controller;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.service.CSVService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ApiResponse readCsv(@RequestPart("file") MultipartFile file, @PathVariable long userIndexId) {
        List<Receipt> receipts = csvService.loadDataFromCSV(file,userIndexId);
        return new ApiResponse(HttpStatus.OK.value(), "CSV file loaded successfully.", receipts);
    }
}
