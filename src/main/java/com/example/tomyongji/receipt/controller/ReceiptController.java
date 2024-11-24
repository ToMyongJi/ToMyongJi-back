package com.example.tomyongji.receipt.controller;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.dto.ReceiptUpdateDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="영수증 조회 api", description = "영수증과 관련된 API들입니다.")
@RestController
@RequestMapping("/api/receipt")
public class ReceiptController {

    private final ReceiptService receiptService;

    @Autowired
    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }


    @Operation(summary = "영수증 작성 api", description = "유저 아이디를 통해 특정 학생회의 영수증을 작성합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping //특정 학생회의 영수증 작성
    public ApiResponse<ReceiptDto> createReceipt(@RequestBody ReceiptCreateDto receiptCreateDto) {
        ReceiptDto createdReceipt = receiptService.createReceipt(receiptCreateDto);
        return new ApiResponse<>(201, "영수증을 성공적으로 작성했습니다.", createdReceipt); //201 created
    }

    @Operation(summary = "모든 영수증 조회 api", description = "모든 영수증을 조회합니다.")
    @GetMapping //모든 영수증 조회
    public ApiResponse<List<ReceiptDto>> getAllReceipts() {
        List<ReceiptDto> receipts = receiptService.getAllReceipts();
        return new ApiResponse<>(200, "모든 영수증을 성공적으로 조회했습니다.", receipts); // 200 OK
    }


    @Operation(summary = "특정 학생회 영수증 조회 api", description = "학생회 아이디를 통해 특정 학생회의 영수증을 조회합니다.")
    @GetMapping("/club/{clubId}") //특정 학생회 영수증 조회
    public ApiResponse<List<ReceiptDto>> getReceiptsByClub(@PathVariable Long clubId) {
        List<ReceiptDto> receipts = receiptService.getReceiptsByClub(clubId);
        return new ApiResponse<>(200, "해당 학생회의 영수증들을 성공적으로 조회했습니다.", receipts); // 200 OK
    }


    @Operation(summary = "특정 영수증 조회 api", description = "영수증 아이디를 통해 특정 영수증을 조회합니다.")
    @GetMapping("/{receiptId}") //특정 영수증 조회
    public ApiResponse<ReceiptDto> getReceiptById(@PathVariable Long receiptId) {
        ReceiptDto receipt = receiptService.getReceiptById(receiptId);
        return new ApiResponse<>(200, "영수증을 성공적으로 조회했습니다.", receipt);
    }


    @Operation(summary = "영수증 삭제 api", description = "영수증 아이디를 통해 특정 영수증을 삭제합니다.")
    @DeleteMapping("/{receiptId}") //특정 영수증 삭제
    public ApiResponse<ReceiptDto> deleteReceipt(@PathVariable Long receiptId) {
        ReceiptDto receipt = receiptService.deleteReceipt(receiptId);
        return new ApiResponse<>(200, "영수증을 성공적으로 삭제했습니다.", receipt);
    }


    @Operation(summary = "영수증 수정 api", description = "영수증 아이디를 통해 특정 영수증을 수정합니다.")
    @PatchMapping //특정 영수증 수정
    public ApiResponse<ReceiptDto> updateReceipt(@RequestBody ReceiptDto receiptDto) {
        ReceiptDto updatedReceipt = receiptService.updateReceipt(receiptDto);
        return new ApiResponse<>(200, "영수증을 성공적으로 수정했습니다.", updatedReceipt);
    }
}


