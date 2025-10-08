package com.example.tomyongji.status.dto;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class MaintenanceUpdateRequestDto {
    private String status;
    private String message;
    private LocalDateTime startTime;
    private LocalDateTime expectedEndTime;

}
