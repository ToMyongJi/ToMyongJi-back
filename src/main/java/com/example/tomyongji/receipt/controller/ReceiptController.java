package com.example.tomyongji.receipt.controller;

import com.example.tomyongji.receipt.dto.ReceiptDto;
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
    @PostMapping("/{id}") //특정 학생회의 영수증 작성
    public ResponseEntity<ReceiptDto> createReceipt(@RequestBody ReceiptDto receiptDto, @PathVariable Long id) {
        ReceiptDto createdReceipt = receiptService.createReceipt(receiptDto, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReceipt); //201 created
    }

    @Operation(summary = "모든 영수증 조회 api", description = "모든 영수증을 조회합니다.")

    @GetMapping //모든 영수증 조회
    public ResponseEntity<List<Receipt>> getAllReceipts() {
        List<Receipt> receipts = receiptService.getAllReceipts();
        return ResponseEntity.status(HttpStatus.OK).body(receipts); // 200 OK
    }


    @Operation(summary = "특정 학생회 영수증 조회 api", description = "학생회 아이디를 통해 특정 학생회의 영수증을 조회합니다.")
    @GetMapping("/club/{clubId}") //특정 학생회 영수증 조회
    public ResponseEntity<List<Receipt>> getReceiptsByClub(@PathVariable Long clubId) {
        List<Receipt> receipts = receiptService.getReceiptsByClub(clubId);
        return ResponseEntity.status(HttpStatus.OK).body(receipts); // 200 OK
    }


    @Operation(summary = "특정 영수증 조회 api", description = "영수증 아이디를 통해 특정 영수증을 조회합니다.")

    @GetMapping("/{id}") //특정 영수증 조회
    public ResponseEntity<Receipt> getReceiptById(@PathVariable Long id) {
        Receipt receipt = receiptService.getReceiptById(id);
        return ResponseEntity.status(HttpStatus.OK).body(receipt);
    }


    @Operation(summary = "영수증 삭제 api", description = "영수증 아이디를 통해 특정 영수증을 삭제합니다.")

    @DeleteMapping("/{id}") //특정 영수증 삭제
    public void deleteReceipt(@PathVariable Long id) {
        receiptService.deleteReceipt(id);
    }


    @Operation(summary = "영수증 수정 api", description = "영수증 아이디를 통해 특정 영수증을 수정합니다.")

    @PatchMapping("/{id}") //특정 영수증 수정
    public ResponseEntity<ReceiptDto> updateReceipt(@PathVariable Long id, @RequestBody ReceiptDto receiptDto) {
        ReceiptDto updatedReceipt = receiptService.updateReceipt(id, receiptDto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedReceipt);
    }
}


