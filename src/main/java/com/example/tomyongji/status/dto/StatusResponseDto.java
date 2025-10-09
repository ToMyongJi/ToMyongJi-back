package com.example.tomyongji.status.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StatusResponseDto {
    private String status;
    private String message;
    private LocalDateTime startTime;
    private LocalDateTime expectedEndTime;

}
