package com.example.tomyongji.status.controller;

import com.example.tomyongji.status.dto.MaintenanceUpdateRequestDto;
import com.example.tomyongji.status.dto.StatusResponseDto;
import com.example.tomyongji.status.service.StatusService;
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

    @GetMapping("/api/status")
    public ResponseEntity<StatusResponseDto> getServerStatus() {
        StatusResponseDto status = statusService.getCurrentStatus();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/api/admin/status")
    public ResponseEntity<Void> updateStatus(@RequestBody MaintenanceUpdateRequestDto dto) {
        statusService.updateMaintenanceStatus(dto);
        return ResponseEntity.ok().build();
    }

}
