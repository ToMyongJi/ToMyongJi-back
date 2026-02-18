package com.example.tomyongji.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.example.tomyongji.domain.status.dto.MaintenanceUpdateRequestDto;
import com.example.tomyongji.domain.status.dto.StatusResponseDto;
import com.example.tomyongji.domain.status.entity.MaintenanceConfig;
import com.example.tomyongji.domain.status.repository.MaintenanceConfigRepository;
import com.example.tomyongji.domain.status.service.StatusService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    @Mock
    private MaintenanceConfigRepository maintenanceConfigRepository;

    @InjectMocks
    private StatusService statusService;

    private MaintenanceConfig maintenanceConfig;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.of(2025, 10, 11, 2, 0);
        endTime = LocalDateTime.of(2025, 10, 11, 4, 0);
        maintenanceConfig = createMaintenanceConfig(1L, "maintenance", "정기 점검 테스트", startTime, endTime);
    }

    private MaintenanceConfig createMaintenanceConfig(Long id, String status, String message,
                                                      LocalDateTime startTime, LocalDateTime endTime) {
        return MaintenanceConfig.builder()
                .id(id)
                .status(status)
                .message(message)
                .startTime(startTime)
                .expectedEndTime(endTime)
                .build();
    }

    private MaintenanceUpdateRequestDto createMaintenanceUpdateRequestDto(String status, String message,
                                                                          LocalDateTime startTime, LocalDateTime endTime) {
        return new MaintenanceUpdateRequestDto(status, message, startTime, endTime);
    }

    @Nested
    @DisplayName("getCurrentStatus 메서드는")
    class Describe_getCurrentStatus {

        @Nested
        @DisplayName("DB에 점검 설정이 존재하면")
        class Context_with_existing_config {

            @Test
            @DisplayName("점검 상태 정보를 반환한다")
            void it_returns_maintenance_status() {
                // given
                given(maintenanceConfigRepository.findById(1L)).willReturn(Optional.of(maintenanceConfig));

                // when
                StatusResponseDto result = statusService.getCurrentStatus();

                // then
                assertThat(result).isNotNull();
                assertThat(result.getStatus()).isEqualTo("maintenance");
                assertThat(result.getMessage()).isEqualTo("정기 점검 테스트");
                assertThat(result.getStartTime()).isEqualTo(startTime);
                assertThat(result.getExpectedEndTime()).isEqualTo(endTime);

                then(maintenanceConfigRepository).should().findById(1L);
            }
        }

        @Nested
        @DisplayName("DB에 점검 설정이 없으면")
        class Context_without_existing_config {

            @Test
            @DisplayName("정상 운영 상태를 반환한다")
            void it_returns_normal_status() {
                // given
                given(maintenanceConfigRepository.findById(1L)).willReturn(Optional.empty());

                // when
                StatusResponseDto result = statusService.getCurrentStatus();

                // then
                assertThat(result).isNotNull();
                assertThat(result.getStatus()).isEqualTo("normal");
                assertThat(result.getMessage()).isEqualTo("서버가 정상적으로 운영중입니다.");
                assertThat(result.getStartTime()).isNull();
                assertThat(result.getExpectedEndTime()).isNull();

                then(maintenanceConfigRepository).should().findById(1L);
            }
        }
    }

    @Nested
    @DisplayName("updateMaintenanceStatus 메서드는")
    class Describe_updateMaintenanceStatus {

        @Nested
        @DisplayName("점검 상태 변경 요청이 주어지면")
        class Context_with_update_request {

            @Test
            @DisplayName("점검 설정을 업데이트한다")
            void it_updates_maintenance_config() {
                // given
                LocalDateTime newStartTime = LocalDateTime.of(2025, 10, 12, 2, 0);
                LocalDateTime newEndTime = LocalDateTime.of(2025, 10, 12, 4, 0);
                MaintenanceUpdateRequestDto updateRequest = createMaintenanceUpdateRequestDto(
                        "maintenance",
                        "긴급 점검입니다.",
                        newStartTime,
                        newEndTime
                );

                MaintenanceConfig existingConfig = new MaintenanceConfig();
                given(maintenanceConfigRepository.findById(1L)).willReturn(Optional.of(existingConfig));

                // when
                statusService.updateMaintenanceStatus(updateRequest);

                // then
                then(maintenanceConfigRepository).should(times(1)).save(argThat(savedConfig ->
                        savedConfig.getStatus().equals("maintenance") &&
                                savedConfig.getMessage().equals("긴급 점검입니다.") &&
                                savedConfig.getStartTime().equals(newStartTime) &&
                                savedConfig.getExpectedEndTime().equals(newEndTime)
                ));
            }
        }

        @Nested
        @DisplayName("정상 운영 상태로 변경 요청이 주어지면")
        class Context_with_normal_status_request {

            @Test
            @DisplayName("점검 설정을 정상 상태로 업데이트한다")
            void it_updates_to_normal_status() {
                // given
                MaintenanceUpdateRequestDto updateRequest = createMaintenanceUpdateRequestDto(
                        "normal",
                        "서버가 정상적으로 운영중입니다.",
                        null,
                        null
                );

                MaintenanceConfig existingConfig = new MaintenanceConfig();
                given(maintenanceConfigRepository.findById(1L)).willReturn(Optional.of(existingConfig));

                // when
                statusService.updateMaintenanceStatus(updateRequest);

                // then
                then(maintenanceConfigRepository).should(times(1)).save(argThat(savedConfig ->
                        savedConfig.getStatus().equals("normal") &&
                                savedConfig.getMessage().equals("서버가 정상적으로 운영중입니다.") &&
                                savedConfig.getStartTime() == null &&
                                savedConfig.getExpectedEndTime() == null
                ));
            }
        }
    }
}