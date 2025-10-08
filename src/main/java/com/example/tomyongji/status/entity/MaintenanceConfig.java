package com.example.tomyongji.status.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "maintenance_config")
@Getter
@Setter // 관리자가 수정해야 하므로 Setter를 열어둡니다.
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceConfig {

    @Id
    private Long id = 1L;

    private String status;
    private String message;
    private LocalDateTime startTime;
    private LocalDateTime expectedEndTime;


}
