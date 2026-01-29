package com.example.tomyongji.domain.receipt.controller;

import com.example.tomyongji.global.common.response.ApiResponse;
import com.example.tomyongji.domain.receipt.dto.CollegesDto;
import com.example.tomyongji.domain.receipt.service.CollegeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "대학 조회 api", description = "회원가입페이지 혹은 영수증 조회 페이지에서 대학을 조회할때 사용합니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeService collegeService;

    @Operation(summary = "모든 대학 조회 api", description = "모든 대학을 조회할때 사용합니다.")
    @GetMapping("api/collegesAndClubs")
    public ResponseEntity<ApiResponse<List<CollegesDto>>> getAllCollegesAndClubs() {
        List<CollegesDto> colleges = collegeService.getAllCollegesAndClubs();
        return ResponseEntity.ok(ApiResponse.onSuccess(colleges));
    }

}
