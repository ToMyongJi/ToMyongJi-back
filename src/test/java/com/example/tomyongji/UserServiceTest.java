package com.example.tomyongji;

import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.dto.UserRequestDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.auth.service.UserService;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.CollegeRepository;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "spring.main.web-application-type=none")
public class UserServiceTest {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CollegeRepository collegeRepository;
    @Autowired
    StudentClubRepository studentClubRepository;
    @Autowired
    ReceiptRepository receiptRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PresidentRepository presidentRepository;
    @Autowired
    PasswordEncoder encoder;
    UserRequestDto userRequestDto;
    StudentClub studentClub;


    @BeforeEach
    void beforeEach(){

        // Save College
        College college = new College();
        college.setCollegeName("ICT융합대학");
        College savedCollege = collegeRepository.save(college);

        // Save StudentClub
        studentClub = new StudentClub();
        studentClub.setStudentClubName("ICT융합대학 학생회");
        studentClub.setBalance(0);
        studentClub.setCollege(savedCollege);
        StudentClub savedStudentClub = studentClubRepository.save(studentClub);

        // Initialize UserRequestDto
        userRequestDto = new UserRequestDto();
        userRequestDto.setUserId("tomyongji2024");
        userRequestDto.setName("투명지");
        userRequestDto.setPassword("*Tomyongji2024");
        userRequestDto.setRole("STU");
        userRequestDto.setEmail("eeeseohyun615@gmail.com");
        userRequestDto.setCollegeName(savedCollege.getCollegeName());
        userRequestDto.setStudentClubId(savedStudentClub.getId());
        userRequestDto.setStudentNum("60222024");
    }
    @AfterEach
    void afterEach(){
        userRepository.deleteAll();
        memberRepository.deleteAll();
        receiptRepository.deleteAll();
        studentClubRepository.deleteAll();
        presidentRepository.deleteAll();
        collegeRepository.deleteAll();
    }
    @Test
    @DisplayName("회원가입 테스트")
    void signUpTest(){
        //given
        //when
        long returnId = userService.signUp(userRequestDto);
        User user = userRepository.findById(returnId).orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다. 사용자 index id: " + returnId));
        //then
        assertThat(user.getStudentNum()).isEqualTo(userRequestDto.getStudentNum());
    }
    @Test
    @DisplayName("사용자 아이디 중복 테스트")
    void checkUserEmailDuplicateTest(){
        //given
        User user = new User();
        user.setUserId(userRequestDto.getUserId());
        user.setName(userRequestDto.getName());
        user.setPassword(encoder.encode(userRequestDto.getPassword()));
        user.setRole(userRequestDto.getRole());
        user.setEmail(userRequestDto.getEmail());
        user.setCollegeName(userRequestDto.getCollegeName());
        user.setStudentClub(studentClub);
        user.setStudentNum(userRequestDto.getStudentNum());
        userRepository.save(user);
        //when
        Boolean EmailDuplicate = userService.checkUserIdDuplicate(userRequestDto.getUserId());
        //then
        assertThat(EmailDuplicate).isEqualTo(true);
    }
    @Test
    @DisplayName("부원 소속 인증 테스트")
    void verifyClubMemberTest(){
        //given
        User user = new User();
        user.setUserId(userRequestDto.getUserId());
        user.setName(userRequestDto.getName());
        user.setPassword(encoder.encode(userRequestDto.getPassword()));
        user.setRole(userRequestDto.getRole());
        user.setEmail(userRequestDto.getEmail());
        user.setCollegeName(userRequestDto.getCollegeName());
        user.setStudentClub(studentClub);
        user.setStudentNum(userRequestDto.getStudentNum());
        userRepository.save(user);

        Member member = new Member();
        member.setName(userRequestDto.getName());
        member.setStudentNum(userRequestDto.getStudentNum());
        member.setStudentClub(studentClub);
        memberRepository.save(member);
        //when
        Boolean isVerify = userService.verifyClub(studentClub.getId(),userRequestDto.getStudentNum());
        //then
        assertThat(isVerify).isEqualTo(true);
    }
    @Test
    @DisplayName("회장 소속 인증 테스트")
    void verifyClubPresidentTest(){
        //given
        President president = new President();
        president.setName(userRequestDto.getName());
        president.setStudentNum(userRequestDto.getStudentNum());
        presidentRepository.save(president);
        studentClub.setPresident(president);
        studentClubRepository.save(studentClub);

        User user = new User();
        user.setUserId(userRequestDto.getUserId());
        user.setName(userRequestDto.getName());
        user.setPassword(encoder.encode(userRequestDto.getPassword()));
        user.setRole("PRESIDENT");
        user.setEmail(userRequestDto.getEmail());
        user.setCollegeName(userRequestDto.getCollegeName());
        user.setStudentClub(studentClub);
        user.setStudentNum(userRequestDto.getStudentNum());
        userRepository.save(user);

        //when
        Boolean isVerify = userService.verifyClub(studentClub.getId(),userRequestDto.getStudentNum());
        //then
        assertThat(isVerify).isEqualTo(true);
    }
    @Test
    @DisplayName("로그인 테스트")
    void loginTest(){
        //given
        User user = new User();
        user.setUserId(userRequestDto.getUserId());
        user.setName(userRequestDto.getName());
        user.setPassword(encoder.encode(userRequestDto.getPassword()));
        user.setRole(userRequestDto.getRole());
        user.setEmail(userRequestDto.getEmail());
        user.setCollegeName(userRequestDto.getCollegeName());
        user.setStudentClub(studentClub);
        user.setStudentNum(userRequestDto.getStudentNum());
        userRepository.save(user);

        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUserId(userRequestDto.getUserId());
        loginRequestDto.setPassword(userRequestDto.getPassword());
        //when
        JwtToken token = userService.login(loginRequestDto);
        //then
        assertThat(token).isNotEqualTo(null);
    }
}
