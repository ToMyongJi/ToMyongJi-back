package com.example.tomyongji;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.status.dto.MaintenanceUpdateRequestDto;
import com.example.tomyongji.status.dto.StatusResponseDto;
import com.example.tomyongji.status.entity.MaintenanceConfig;
import com.example.tomyongji.status.repository.MaintenanceConfigRepository;
import java.time.LocalDateTime;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StatusTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MaintenanceConfigRepository maintenanceConfigRepository;

    @AfterEach
    @Transactional
    void tearDown() {
        MaintenanceConfig config = maintenanceConfigRepository.findById(1L)
            .orElseGet(MaintenanceConfig::new);

        config.setStatus("normal");
        config.setMessage("서버가 정상적으로 운영중입니다.");
        config.setStartTime(null);
        config.setExpectedEndTime(null);

        maintenanceConfigRepository.save(config);
    }

    @Test
    @DisplayName("점검상태 조회 테스트")
    void testGetMaintenanceFlow() throws Exception {

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        ResponseEntity<ApiResponse<StatusResponseDto>> response = restTemplate.exchange(
            "/api/status",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiResponse<StatusResponseDto>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<StatusResponseDto> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("점검상태 호출에 성공했습니다.");
    }

    @Test
    @DisplayName("점검상태 변경 테스트")
    void testUpdateMaintenanceFlow(){
        // given
        String token = getAdminToken();
        LocalDateTime startTime = LocalDateTime.of(2025, 10, 12, 2, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 10, 12, 4, 0);
        MaintenanceUpdateRequestDto updateRequestDto = new MaintenanceUpdateRequestDto(
            "maintenance",
            "긴급 점검입니다.",
            startTime,
            endTime
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<MaintenanceUpdateRequestDto> entity = new HttpEntity<>(updateRequestDto, headers);
        // when
        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
            "/api/admin/status",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<ApiResponse<Void>>() {}
        );

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(Objects.requireNonNull(response.getBody()).getStatusMessage()).isEqualTo("점검상태 변경에 성공했습니다.");
    }

    private String getAdminToken() {
        LoginRequestDto loginRequest = new LoginRequestDto("admin", "Admin123!");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginRequestDto> entity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<ApiResponse<JwtToken>> response = restTemplate.exchange(
            "/api/users/login",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<ApiResponse<JwtToken>>() {}
        );
        System.out.println(response);
        return response.getBody().getData().getAccessToken(); // JWT 토큰 반환
    }
}
