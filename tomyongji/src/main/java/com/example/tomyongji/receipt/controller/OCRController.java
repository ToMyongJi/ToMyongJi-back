package com.example.tomyongji.receipt.controller;

import com.example.tomyongji.receipt.dto.OCRResultDto;
import com.example.tomyongji.receipt.service.OCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocr")
public class OCRController {

    private final OCRService ocrService;

    @Autowired
    public OCRController(OCRService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/upload")
    public ResponseEntity<OCRResultDto> uploadImageAndExtractText(@RequestPart("file") MultipartFile file) {
        try {
            OCRResultDto result = ocrService.processImage(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}


