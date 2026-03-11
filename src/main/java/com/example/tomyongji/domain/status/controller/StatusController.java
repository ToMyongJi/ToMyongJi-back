package com.example.tomyongji.domain.status.controller;

import com.example.tomyongji.global.common.response.ApiResponse;
import com.example.tomyongji.domain.status.service.StatusService;
import com.example.tomyongji.domain.status.dto.MaintenanceUpdateRequestDto;
import com.example.tomyongji.domain.status.dto.StatusResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "서버 상태 api", description = "서버 점검 상태 확인 및 변경과 관련된 API들입니다.")
@RestController
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    @Operation(summary = "점검상태 확인 api", description = "점검 상태를 normal/maintenance 로 반환합니다.")
    @GetMapping("/api/status")
    public ResponseEntity<ApiResponse<StatusResponseDto>> getServerStatus() {
        StatusResponseDto status = statusService.getCurrentStatus();
        return ResponseEntity.ok(ApiResponse.onSuccess(status));
    }

    @Operation(summary = "점검상태 변경 api", description = "어드민이 점검 상태 및 메세지와 시간을 설정합니다.")
    @PostMapping("/api/admin/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@RequestBody MaintenanceUpdateRequestDto dto) {
        statusService.updateMaintenanceStatus(dto);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }
}
