package com.example.tomyongji;

import static com.example.tomyongji.validation.ErrorMsg.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.auth.dto.UserRequestDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.ClubVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.my.dto.MyDto;
import com.example.tomyongji.my.dto.SaveMemberDto;
import com.example.tomyongji.my.service.MyService;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Transactional
public class MyTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MyService myService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StudentClubRepository studentClubRepository;

    @Autowired
    ClubVerificationRepository clubVerificationRepository;
    private User user;


    @BeforeEach
    void setup() {
        StudentClub studentClub = studentClubRepository.findById(26L).orElseThrow(()-> new CustomException(NOT_FOUND_STUDENT_CLUB, 400));

        //User 저장: 융합소프트웨어학부 학생회장
        user = User.builder()
            .userId("testUser")
            .name("test name")
            .studentNum("60000000")
            .collegeName("인공지능소프트웨어융합대학")
            .email("test@example.com")
            .password("password123")
            .role("PRESIDENT")
            .studentClub(studentClub) //저장된 StudentClub 설정
            .build();
        userRepository.saveAndFlush(user);
        
        //테스트
        System.out.println("유저 ID: " + user.getId());
        userRepository.findAll().forEach(u -> System.out.println("저장된 유저: " + u));
        studentClubRepository.findAll().forEach(sc -> System.out.println("저장된 학생회: " + sc));
    }

    @AfterEach
    void reset() {
        userRepository.delete(user);
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("유저 정보 조회 흐름 테스트")
    void testGetMyInfoFlow() throws Exception {
        //Given
        Long id = user.getId();
        assertThat(id).isNotNull();
        System.out.println("테스트 로그 - 유저명: " + user.getName());
        System.out.println("테스트 로그 - 유저 학생회: " + user.getStudentClub().getStudentClubName());


        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("id", id);

        ResponseEntity<ApiResponse<MyDto>> response = restTemplate.exchange(
            "/api/my/{id}",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiResponse<MyDto>>() {},
            uriVariables
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<MyDto> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("내 정보 조회에 성공했습니다.");
        assertThat(body.getData().getStudentNum()).isEqualTo("60000000");
    }

    @Test
    @DisplayName("멤버 정보 조회 흐름 테스트")
    void testGetMembersFlow() throws Exception {
        //Given
        Long id = user.getId();

        Member member1 = Member.builder()
            .studentNum("60000001")
            .name("test member1")
            .studentClub(user.getStudentClub())
            .build();
        Member member2 = Member.builder()
            .studentNum("60000002")
            .name("test member2")
            .studentClub(user.getStudentClub())
            .build();

        memberRepository.saveAndFlush(member1);
        memberRepository.saveAndFlush(member2);

        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("id", id);

        ResponseEntity<ApiResponse<List<MemberDto>>> response = restTemplate.exchange(
            "/api/my/members/{id}",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<ApiResponse<List<MemberDto>>>() {},
            uriVariables
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<List<MemberDto>> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("소속 부원 조회에 성공했습니다.");
        assertThat(body.getData().get(0).getStudentNum()).isEqualTo("60000001");
        assertThat(body.getData().get(1).getStudentNum()).isEqualTo("60000002");

        //테스트 완료 후 삭제
        memberRepository.delete(member1);
        memberRepository.delete(member2);
    }

    @Test
    @DisplayName("멤버 저장 흐름 테스트")
    void testSaveMemberFlow() throws Exception {
        //Given
        SaveMemberDto saveMemberDto = SaveMemberDto.builder()
            .id(user.getId()) //회장의 인덱스 id
            .studentNum("60000003")
            .name("test name3")
            .build();
        //When, Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SaveMemberDto> entity = new HttpEntity<>(saveMemberDto, headers);
        //When
        ResponseEntity<ApiResponse<MemberDto>> response = restTemplate.exchange(
            "/api/my/members",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<ApiResponse<MemberDto>>() {}
        );
        //Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ApiResponse<MemberDto> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(201);
        assertThat(body.getStatusMessage()).isEqualTo("소속 부원 정보 저장에 성공했습니다.");
    }

    @Test
    @DisplayName("멤버 삭제 흐름 테스트")
    void testDeleteMemberFlow() throws Exception {
        // Given: 삭제할 멤버 데이터 준비
        Member deleteMember = Member.builder()
            .studentNum("60000004")
            .name("test name4")
            .studentClub(user.getStudentClub())
            .build();
        memberRepository.saveAndFlush(deleteMember);

        String deletedStudentNum = deleteMember.getStudentNum();

        //When & Then
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(null, headers);

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("deletedStudentNum", deletedStudentNum);

        ResponseEntity<ApiResponse<MemberDto>> response = restTemplate.exchange(
            "/api/my/members/{deletedStudentNum}",
            HttpMethod.DELETE,
            entity,
            new ParameterizedTypeReference<ApiResponse<MemberDto>>() {},
            uriVariables
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<MemberDto> body = response.getBody();
        assertNotNull(body);
        assertThat(body.getStatusCode()).isEqualTo(200);
        assertThat(body.getStatusMessage()).isEqualTo("소속 부원 삭제에 성공했습니다.");
        assertThat(body.getData().getStudentNum()).isEqualTo("60000004");
    }

}
