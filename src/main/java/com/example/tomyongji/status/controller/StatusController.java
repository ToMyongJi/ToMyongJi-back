package com.example.tomyongji.status.controller;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.status.dto.MaintenanceUpdateRequestDto;
import com.example.tomyongji.status.dto.StatusResponseDto;
import com.example.tomyongji.status.service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    @Operation(summary = "점검상태 확인 api", description = "점검 상태를 normal/maintenance 로 반환합니다.")
    @GetMapping("/api/status")
    public ApiResponse<StatusResponseDto> getServerStatus() {
        StatusResponseDto status = statusService.getCurrentStatus();
        return new ApiResponse<>(200, "점검상태 호출에 성공했습니다.", status);
    }

    @Operation(summary = "점검상태 변경 api", description = "어드민이 점검 상태 및 메세지와 시간을 설정합니다.")
    @PostMapping("/api/admin/status")
    public ApiResponse<Void> updateStatus(@RequestBody MaintenanceUpdateRequestDto dto) {
        statusService.updateMaintenanceStatus(dto);
        return new ApiResponse<>(200, "점검상태 변경에 성공했습니다.");
    }
}
