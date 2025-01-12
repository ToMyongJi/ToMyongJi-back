package com.example.tomyongji;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.auth.dto.FindIdRequestDto;
import com.example.tomyongji.auth.dto.UserRequestDto;
import com.example.tomyongji.my.dto.AdminSaveMemberDto;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import jakarta.transaction.Transactional;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    @DisplayName("학생회장 조회 테스트")
    void getPresident(){
        //Given
        President president = President.builder()
                .studentNum("60222024")
                .name("투명지")
                .build();
        presidentRepository.saveAndFlush(president);
        StudentClub club = studentClubRepository.findById(26L).get();
        club.setPresident(president);
        studentClubRepository.save(club);

        Long clubId = 26L;
        //When
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("clubId", clubId);

        ResponseEntity<ApiResponse<PresidentDto>> response = restTemplate.exchange(
                "/api/admin/president/{clubId}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<PresidentDto>>() {},
                uriVariables
        );
        //then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData().getClubId()).isEqualTo(26L);
        assertThat(response.getBody().getData().getStudentNum()).isEqualTo(president.getStudentNum());
    }
    @Test
    @DisplayName("학생회장 저장 테스트")
    void savePresident(){
        //Given
        PresidentDto presidentDto = PresidentDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .clubId(26L)
                .build();

        //When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

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
        assertThat(response.getBody().getData().getClubId()).isEqualTo(26L);
        assertThat(response.getBody().getData().getStudentNum()).isEqualTo(presidentDto.getStudentNum());
    }

    @Test
    @DisplayName("소속 부원 조회 테스트")
    void getMembers(){
        //Given
        StudentClub studentClub = studentClubRepository.findById(26L).get();
        Member member = Member.builder()
                .studentNum("60222024")
                .name("투명지")
                .studentClub(studentClub)
                .build();
        memberRepository.save(member);
        Long clubId = 26L;
        //When
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("clubId", clubId);

        ResponseEntity<ApiResponse<List<MemberDto>>> response = restTemplate.exchange(
                "/api/admin/member/{clubId}",
                HttpMethod.GET,
                null,
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
        AdminSaveMemberDto adminSaveMemberDto = AdminSaveMemberDto.builder()
                .name("뉴투명지")
                .studentNum("60222025")
                .clubId(26L)
                .build();
        //When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
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
        StudentClub studentClub = studentClubRepository.findById(26L).get();
        Member member = Member.builder()
                .studentNum("60222024")
                .name("투명지")
                .studentClub(studentClub)
                .build();
        Long memberId = memberRepository.save(member).getId();
        //When
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("memberId", memberId);

        ResponseEntity<ApiResponse<MemberDto>> response = restTemplate.exchange(
                "/api/admin/member/{memberId}",
                HttpMethod.DELETE,
                null,
                new ParameterizedTypeReference<ApiResponse<MemberDto>>() {},
                uriVariables
        );
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatusMessage()).isNotEmpty();
        assertThat(response.getBody().getData().getStudentNum()).isEqualTo(member.getStudentNum());
    }
}
