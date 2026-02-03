package com.example.tomyongji.domain.receipt.controller;

import com.example.tomyongji.global.common.response.ApiResponse;
import com.example.tomyongji.domain.receipt.service.ReceiptService;
import com.example.tomyongji.domain.receipt.dto.PagingReceiptDto;
import com.example.tomyongji.domain.receipt.dto.ReceiptByStudentClubDto;
import com.example.tomyongji.domain.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.domain.receipt.dto.ReceiptDto;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="영수증 조회 api", description = "영수증과 관련된 API들입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/receipt")
public class ReceiptController {

    private final ReceiptService receiptService;

    @Operation(summary = "영수증 작성 api", description = "유저 아이디를 통해 특정 학생회의 영수증을 작성합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping //특정 학생회의 영수증 작성
    public ResponseEntity<ApiResponse<ReceiptDto>> createReceipt(@RequestBody ReceiptCreateDto receiptCreateDto, @AuthenticationPrincipal UserDetails currentUser) {
        ReceiptDto createdReceipt = receiptService.createReceipt(receiptCreateDto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.onCreated(createdReceipt)); //201 created
    }

    @Operation(summary = "모든 영수증 조회 api", description = "모든 영수증을 조회합니다.")
    @GetMapping //모든 영수증 조회
    public ResponseEntity<ApiResponse<List<ReceiptDto>>> getAllReceipts() {
        List<ReceiptDto> receipts = receiptService.getAllReceipts();
        return ResponseEntity.ok(
                ApiResponse.onSuccess(receipts)); // 200 OK
    }


    @Operation(summary = "특정 학생회 영수증 조회 api", description = "학생회 아이디를 통해 특정 학생회의 영수증을 조회합니다.")
    @GetMapping("/club/{id}") //특정 학생회 영수증 조회
    public ResponseEntity<ApiResponse<ReceiptByStudentClubDto>> getReceiptsByClub(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails currentUser) {
        ReceiptByStudentClubDto receipts = receiptService.getReceiptsByClub(id, currentUser);
        return ResponseEntity.ok(
                ApiResponse.onSuccess(receipts)); // 200 OK
    }

    @Operation(summary = "특정 학생회 영수증 조회 일반 학생용 api", description = "학생회 아이디를 통해 특정 학생회의 영수증을 조회합니다.")
    @GetMapping("/club/{clubId}/student") //특정 학생회 영수증 조회
    public ResponseEntity<ApiResponse<List<ReceiptDto>>> getReceiptsByClubForStudent(@PathVariable("clubId") Long clubId) {
        List<ReceiptDto> receipts = receiptService.getReceiptsByClubForStudent(clubId);
        return ResponseEntity.ok(
                ApiResponse.onSuccess(receipts)); // 200 OK
    }

    @Operation(summary = "특정 학생회 영수증 페이지별 조회 일반 학생용 api", description = "학생회 아이디와 페이지 정보, 영수증 수를 통해 특정 학생회의 영수증을 조회합니다.")
    @GetMapping("/club/{clubId}/paging")
    public ResponseEntity<ApiResponse<PagingReceiptDto>> getReceiptsByClubPaging(
        @PathVariable("clubId") Long clubId,
        @RequestParam(defaultValue = "0") int page, // 값이 안 오면 0(첫페이지)
        @RequestParam(defaultValue = "10") int size, // 값이 안 오면 10개씩
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month
    ) {
        PagingReceiptDto receiptPage = receiptService.getReceiptsByClubPaging(clubId, page, size, year, month);
        return ResponseEntity.ok(
            ApiResponse.onSuccess(receiptPage)); // 200 OK
    }


    @Operation(summary = "특정 영수증 조회 api", description = "영수증 아이디를 통해 특정 영수증을 조회합니다.")
    @GetMapping("/{receiptId}") //특정 영수증 조회
    public ResponseEntity<ApiResponse<ReceiptDto>> getReceiptById(@PathVariable("receiptId") Long receiptId) {
        ReceiptDto receipt = receiptService.getReceiptById(receiptId);
        return ResponseEntity.ok(
                ApiResponse.onSuccess(receipt)); // 200 OK
    }




    @Operation(summary = "영수증 삭제 api", description = "영수증 아이디를 통해 특정 영수증을 삭제합니다.")
    @DeleteMapping("/{receiptId}") //특정 영수증 삭제
    public ResponseEntity<ApiResponse<ReceiptDto>> deleteReceipt(@PathVariable("receiptId") Long receiptId, @AuthenticationPrincipal UserDetails currentUser) {
        ReceiptDto receipt = receiptService.deleteReceipt(receiptId, currentUser);
        return ResponseEntity.ok(
                ApiResponse.onSuccess(receipt)); // 200 OK
    }

    @Operation(summary = "영수증 수정 api", description = "영수증 아이디를 통해 특정 영수증을 수정합니다.")
    @PutMapping //특정 영수증 수정
    public ResponseEntity<ApiResponse<ReceiptDto>> updateReceipt(@RequestBody ReceiptDto receiptDto, @AuthenticationPrincipal UserDetails currentUser) {
        ReceiptDto updatedReceipt = receiptService.updateReceipt(receiptDto, currentUser);
        return ResponseEntity.ok(
                ApiResponse.onSuccess(updatedReceipt)); // 200 OK
    }

    @Operation(summary = "영수증 검색 api", description = "두 글자 이상의 검색어를 통해 특정 영수증을 조회합니다.")
    @GetMapping("/keyword") //특정 영수증 수정
    public ResponseEntity<ApiResponse<List<ReceiptDto>>> searchReceiptByKeyword(@RequestParam String keyword, @AuthenticationPrincipal UserDetails currentUser) {
        List<ReceiptDto> receipts = receiptService.searchReceiptByKeyword(keyword, currentUser);
        return ResponseEntity.ok(
                ApiResponse.onSuccess(receipts)); // 200 OK
    }
}
