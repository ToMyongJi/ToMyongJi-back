package com.example.tomyongji;

import com.example.tomyongji.status.dto.MaintenanceUpdateRequestDto;
import com.example.tomyongji.status.dto.StatusResponseDto;
import com.example.tomyongji.status.entity.MaintenanceConfig;
import com.example.tomyongji.status.repository.MaintenanceConfigRepository;
import com.example.tomyongji.status.service.StatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatusServiceTest {

    @Mock
    private MaintenanceConfigRepository maintenanceConfigRepository;

    @InjectMocks
    private StatusService statusService;

    @Test
    @DisplayName("점검상태 조회 테스트 - DB에 config 존재")
    void getCurrentStatus_configExists() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2025, 10, 11, 2, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 10, 11, 4, 0);
        MaintenanceConfig maintenanceConfig = MaintenanceConfig.builder()
            .id(1L)
            .status("maintenance")
            .message("정기 점검 테스트")
            .startTime(startTime)
            .expectedEndTime(endTime)
            .build();

        when(maintenanceConfigRepository.findById(1L)).thenReturn(Optional.of(maintenanceConfig));

        // when
        StatusResponseDto responseDto = statusService.getCurrentStatus();

        // then
        assertThat(responseDto.getStatus()).isEqualTo("maintenance");
        assertThat(responseDto.getMessage()).isEqualTo("정기 점검 테스트");
        assertThat(responseDto.getStartTime()).isEqualTo(startTime);
        assertThat(responseDto.getExpectedEndTime()).isEqualTo(endTime);
    }

    @Test
    @DisplayName("점검상태 조회 테스트 - DB에 config 부재")
    void getCurrentStatus_configNotExists() {
        // given
        when(maintenanceConfigRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        StatusResponseDto resultDto = statusService.getCurrentStatus();

        // then
        assertThat(resultDto.getStatus()).isEqualTo("normal");
        assertThat(resultDto.getMessage()).isEqualTo("서버가 정상적으로 운영중입니다.");
        assertThat(resultDto.getStartTime()).isNull();
        assertThat(resultDto.getExpectedEndTime()).isNull();
    }

    @Test
    @DisplayName("점검상태 변경 테스트")
    void updateCurrentStatus() {
        // given
        LocalDateTime startTime = LocalDateTime.of(2025, 10, 12, 2, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 10, 12, 4, 0);
        MaintenanceUpdateRequestDto updateRequestDto = new MaintenanceUpdateRequestDto(
            "maintenance",
            "긴급 점검입니다.",
            startTime,
            endTime
        );

        MaintenanceConfig existingConfig = new MaintenanceConfig();
        when(maintenanceConfigRepository.findById(1L)).thenReturn(Optional.of(existingConfig));

        // when
        statusService.updateMaintenanceStatus(updateRequestDto);

        // then
        verify(maintenanceConfigRepository, times(1)).save(argThat(savedConfig ->
            savedConfig.getStatus().equals("maintenance") &&
                savedConfig.getMessage().equals("긴급 점검입니다.") &&
                savedConfig.getStartTime().equals(startTime)
        ));    }
}
