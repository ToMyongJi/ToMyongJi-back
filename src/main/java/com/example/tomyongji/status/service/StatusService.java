package com.example.tomyongji.status.service;

import com.example.tomyongji.status.dto.MaintenanceUpdateRequestDto;
import com.example.tomyongji.status.dto.StatusResponseDto;
import com.example.tomyongji.status.entity.MaintenanceConfig;
import com.example.tomyongji.status.repository.MaintenanceConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final MaintenanceConfigRepository maintenanceConfigRepository;

    @Transactional(readOnly = true)
    public StatusResponseDto getCurrentStatus() {
        // DB에서 ID가 1인 설정을 찾고, 만약 없으면 "normal" 상태의 기본 객체를 반환
        MaintenanceConfig config = maintenanceConfigRepository.findById(1L)
            .orElseGet(() -> MaintenanceConfig.builder()
                .id(1L)
                .status("normal")
                .message("서버가 정상적으로 운영중입니다.")
                .build());

        return StatusResponseDto.builder()
            .status(config.getStatus())
            .message(config.getMessage())
            .startTime(config.getStartTime())
            .expectedEndTime(config.getExpectedEndTime())
            .build();
    }

    // 관리자가 점검 상태를 업데이트하기 위한 메서드
    @Transactional
    public void updateMaintenanceStatus(MaintenanceUpdateRequestDto dto) {
        MaintenanceConfig config = maintenanceConfigRepository.findById(1L)
            .orElse(new MaintenanceConfig()); // 없으면 새로 생성

        config.setStatus(dto.getStatus());
        config.setMessage(dto.getMessage());
        config.setStartTime(dto.getStartTime());
        config.setExpectedEndTime(dto.getExpectedEndTime());

        maintenanceConfigRepository.save(config);
    }
}
