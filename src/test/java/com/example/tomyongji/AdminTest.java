package com.example.tomyongji;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.my.dto.AdminSaveMemberDto;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private PresidentRepository presidentRepository;
    @Autowired
    private StudentClubRepository studentClubRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @AfterEach
    void clear() {
        new TransactionTemplate(transactionManager).execute(status -> {
            StudentClub studentClub = studentClubRepository.findByStudentClubName("건축대학 학생회");
            studentClub.setPresident(null);
            studentClubRepository.save(studentClub);
            presidentRepository.deleteByStudentNum("60222024");
            memberRepository.deleteAllByStudentNum("60222024");
            return null;
        });
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

    @Test
    @DisplayName("학생회장 조회 테스트")
    void getPresident(){
        //Given
        Long clubId = 33L;
        String token = getAdminToken();

        President idEmptyPresident = President.builder()
                .name("투명지")
                .studentNum("60222024")
                .build();
        President president = presidentRepository.save(idEmptyPresident);
        StudentClub studentClub =studentClubRepository.findById(33L).get();
        studentClub.setPresident(president);
        studentClubRepository.save(studentClub);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("clubId", clubId);
        //when
        ResponseEntity<ApiResponse<PresidentDto>> response = restTemplate.exchange(
                "/api/admin/president/{clubId}",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<PresidentDto>>() {},
                uriVariables
        );
        //then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData().getClubId()).isEqualTo(33L);
        assertThat(response.getBody().getData().getStudentNum()).isEqualTo("60222024");
    }
    @Test
    @DisplayName("학생회장 저장 테스트")
    void savePresident(){
        //Given
        StudentClub digital = studentClubRepository.findByStudentClubName("건축대학 학생회");
        PresidentDto presidentDto = PresidentDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .clubId(digital.getId())
                .build();

        String token = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<PresidentDto> entity = new HttpEntity<>(presidentDto, headers);
        //When
        ResponseEntity<ApiResponse<PresidentDto>> response = restTemplate.exchange(
                "/api/admin/president",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<PresidentDto>>() {}
        );

        //then
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData().getStudentNum()).isEqualTo(presidentDto.getStudentNum());
    }

    @Test
    @DisplayName("소속 부원 조회 테스트")
    void getMembers(){
        //Given
        StudentClub studentClub = studentClubRepository.findByStudentClubName("건축대학 학생회");
        Member member = Member.builder()
                .studentNum("60222024")
                .name("투명지")
                .studentClub(studentClub)
                .build();
        memberRepository.save(member);
        Long clubId = studentClub.getId();
        //When
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("clubId", clubId);

        String token = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<AdminSaveMemberDto> entity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse<List<MemberDto>>> response = restTemplate.exchange(
                "/api/admin/member/{clubId}",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<List<MemberDto>>>() {},
                uriVariables
        );
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData().get(0).getStudentNum()).isEqualTo(member.getStudentNum());
    }

    @Test
    @DisplayName("소속 부원 저장 테스트")
    void saveMember(){
        //Given
        StudentClub aisoftware = studentClubRepository.findByStudentClubName("건축대학 학생회");
        AdminSaveMemberDto adminSaveMemberDto = AdminSaveMemberDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .clubId(aisoftware.getId())
                .build();

        //When
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<AdminSaveMemberDto> entity = new HttpEntity<>(adminSaveMemberDto, headers);

        //When
        ResponseEntity<ApiResponse<MemberDto>> response = restTemplate.exchange(
                "/api/admin/member",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<MemberDto>>() {}
        );
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData().getStudentNum()).isEqualTo(adminSaveMemberDto.getStudentNum());
    }
    @Test
    @DisplayName("소속 부원 삭제 테스트")
    void deleteMember(){
        //Given
        StudentClub aisoftware = studentClubRepository.findByStudentClubName("건축대학 학생회");
        Member member = Member.builder()
                .studentNum("60222024")
                .name("투명지")
                .studentClub(aisoftware)
                .build();
        Long memberId = memberRepository.save(member).getId();
        //When
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("memberId", memberId);

        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<AdminSaveMemberDto> entity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse<MemberDto>> response = restTemplate.exchange(
                "/api/admin/member/{memberId}",
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ApiResponse<MemberDto>>() {},
                uriVariables
        );
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData().getStudentNum()).isEqualTo(member.getStudentNum());
    }
}
