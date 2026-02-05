package com.example.tomyongji.receipt;

import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_USER;
import static com.example.tomyongji.global.error.ErrorMsg.NO_AUTHORIZATION_BELONGING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.tomyongji.domain.admin.dto.PresidentDto;
import com.example.tomyongji.domain.admin.service.AdminService;
import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.auth.service.UserService;
import com.example.tomyongji.domain.receipt.dto.ClubDto;
import com.example.tomyongji.domain.receipt.dto.TransferDto;
import com.example.tomyongji.domain.receipt.entity.College;
import com.example.tomyongji.domain.receipt.entity.Receipt;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.mapper.StudentClubMapper;
import com.example.tomyongji.domain.receipt.repository.ReceiptRepository;
import com.example.tomyongji.domain.receipt.repository.StudentClubRepository;
import com.example.tomyongji.domain.receipt.service.StudentClubService;
import com.example.tomyongji.global.error.CustomException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class StudentClubServiceTest {

    @Mock
    private StudentClubRepository studentClubRepository;
    @Mock
    private StudentClubMapper studentClubMapper;
    @Mock
    private ReceiptRepository receiptRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private AdminService adminService;

    @InjectMocks
    private StudentClubService studentClubService;

    private College college;
    private StudentClub convergenceSoftware;
    private StudentClub digitalContentsDesign;
    private StudentClub business;
    private User president;
    private User student1;
    private User nextPresident;
    private Receipt receipt1;
    private Receipt receipt2;
    private UserDetails currentUser;

    @BeforeEach
    void setUp() {
        college = createCollege(1L, "ICT 융합대학");

        convergenceSoftware = createStudentClub(1L, "융합소프트웨어학부 학생회", 1000, college);
        digitalContentsDesign = createStudentClub(2L, "디지털콘텐츠디자인전공 학생회", 1000, college);
        business = createStudentClub(3L, "경영전공 학생회", 1000, null);

        president = createUser(
                1L, "president123", "정우주", "60221317",
                convergenceSoftware, "ICT 융합대학",
                "president@mju.ac.kr", "password", "PRESIDENT"
        );

        student1 = createUser(
                2L, "student1", "홍길동", "60221111",
                convergenceSoftware, "ICT 융합대학",
                "student1@mju.ac.kr", "password", "STU"
        );

        nextPresident = createUser(
                3L, "nextPresident", "박진형", "60221318",
                convergenceSoftware, "ICT 융합대학",
                "np@mju.ac.kr", "password", "STU"
        );

        receipt1 = createReceipt(1L, 5000, 0, convergenceSoftware);
        receipt2 = createReceipt(2L, 0, 2000, convergenceSoftware);

        currentUser = createUserDetails("president123", "password", "PRESIDENT");
    }

    // ==================== Fixture Methods ====================

    private College createCollege(Long id, String name) {
        return College.builder()
                .id(id)
                .collegeName(name)
                .build();
    }

    private StudentClub createStudentClub(Long id, String name, int balance, College college) {
        return StudentClub.builder()
                .id(id)
                .studentClubName(name)
                .Balance(balance)
                .college(college)
                .build();
    }

    private User createUser(Long id, String userId, String name, String studentNum,
                            StudentClub studentClub, String collegeName, String email,
                            String password, String role) {
        return User.builder()
                .id(id)
                .userId(userId)
                .name(name)
                .studentNum(studentNum)
                .studentClub(studentClub)
                .collegeName(collegeName)
                .email(email)
                .password(password)
                .role(role)
                .build();
    }

    private Receipt createReceipt(Long id, int deposit, int withdrawal, StudentClub studentClub) {
        return Receipt.builder()
                .id(id)
                .deposit(deposit)
                .withdrawal(withdrawal)
                .studentClub(studentClub)
                .build();
    }

    private ClubDto createClubDto(Long id, String name) {
        return ClubDto.builder()
                .studentClubId(id)
                .studentClubName(name)
                .build();
    }

    private UserDetails createUserDetails(String username, String password, String role) {
        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(password)
                .roles(role)
                .build();
    }

    private PresidentDto createPresidentDto(Long clubId, String studentNum, String name) {
        return PresidentDto.builder()
                .clubId(clubId)
                .studentNum(studentNum)
                .name(name)
                .build();
    }

    // ==================== getAllStudentClub 테스트 ====================

    @Nested
    @DisplayName("getAllStudentClub 메서드는")
    class Describe_getAllStudentClub {

        @Nested
        @DisplayName("학생회가 존재하면")
        class Context_with_student_clubs {

            @Test
            @DisplayName("모든 학생회 목록을 반환한다")
            void it_returns_all_student_clubs() {
                // given
                List<StudentClub> studentClubList = List.of(convergenceSoftware, digitalContentsDesign, business);
                ClubDto convergenceSoftwareDto = createClubDto(convergenceSoftware.getId(), convergenceSoftware.getStudentClubName());
                ClubDto digitalContentsDesignDto = createClubDto(digitalContentsDesign.getId(), digitalContentsDesign.getStudentClubName());
                ClubDto businessDto = createClubDto(business.getId(), business.getStudentClubName());

                given(studentClubRepository.findAll()).willReturn(studentClubList);
                given(studentClubMapper.toClubDto(convergenceSoftware)).willReturn(convergenceSoftwareDto);
                given(studentClubMapper.toClubDto(digitalContentsDesign)).willReturn(digitalContentsDesignDto);
                given(studentClubMapper.toClubDto(business)).willReturn(businessDto);

                // when
                List<ClubDto> result = studentClubService.getAllStudentClub();

                // then
                assertThat(result).isNotNull()
                        .hasSize(3);
                assertThat(result.get(0)).isEqualTo(convergenceSoftwareDto);
                assertThat(result.get(1)).isEqualTo(digitalContentsDesignDto);
                assertThat(result.get(2)).isEqualTo(businessDto);

                then(studentClubRepository).should().findAll();
            }
        }
    }

    // ==================== getStudentClubById 테스트 ====================

    @Nested
    @DisplayName("getStudentClubById 메서드는")
    class Describe_getStudentClubById {

        @Nested
        @DisplayName("유효한 단과대 ID가 주어지면")
        class Context_with_valid_college_id {

            @Test
            @DisplayName("해당 단과대의 모든 학생회를 반환한다")
            void it_returns_student_clubs_of_college() {
                // given
                Long collegeId = college.getId();
                List<StudentClub> ictStudentClubList = List.of(convergenceSoftware, digitalContentsDesign);
                ClubDto convergenceSoftwareDto = createClubDto(convergenceSoftware.getId(), convergenceSoftware.getStudentClubName());
                ClubDto digitalContentsDesignDto = createClubDto(digitalContentsDesign.getId(), digitalContentsDesign.getStudentClubName());

                given(studentClubRepository.findAllByCollege_Id(collegeId)).willReturn(ictStudentClubList);
                given(studentClubMapper.toClubDto(convergenceSoftware)).willReturn(convergenceSoftwareDto);
                given(studentClubMapper.toClubDto(digitalContentsDesign)).willReturn(digitalContentsDesignDto);

                // when
                List<ClubDto> result = studentClubService.getStudentClubById(collegeId);

                // then
                assertThat(result).isNotNull()
                        .hasSize(2);
                assertThat(result.get(0)).isEqualTo(convergenceSoftwareDto);
                assertThat(result.get(1)).isEqualTo(digitalContentsDesignDto);

                then(studentClubRepository).should().findAllByCollege_Id(collegeId);
            }
        }
    }

    // ==================== transferStudentClub 테스트 ====================

    @Nested
    @DisplayName("transferStudentClub 메서드는")
    class Describe_transferStudentClub {

        @Nested
        @DisplayName("다음 회장이 확정되지 않은 경우")
        class Context_without_next_president {

            @Test
            @DisplayName("학생회를 성공적으로 이전한다")
            void it_transfers_successfully() {
                // given
                List<Receipt> receipts = List.of(receipt1, receipt2);

                given(userRepository.findByUserId("president123")).willReturn(Optional.of(president));
                given(receiptRepository.findAllByStudentClubOrderByIdDesc(convergenceSoftware)).willReturn(receipts);
                given(userRepository.findFirstByStudentClubAndRole(convergenceSoftware, "PRESIDENT")).willReturn(president);
                given(userRepository.findByStudentClubAndRole(convergenceSoftware, "STU")).willReturn(List.of(student1));

                // when
                TransferDto result = studentClubService.transferStudentClub(null, currentUser);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getStudentClubName()).isEqualTo("융합소프트웨어학부 학생회");
                assertThat(result.getTotalDeposit()).isEqualTo(3000);
                assertThat(result.getNetAmount()).isEqualTo(3000);

                then(userRepository).should().findByUserId("president123");
                then(receiptRepository).should().findAllByStudentClubOrderByIdDesc(convergenceSoftware);
                then(receiptRepository).should().deleteAll(receipts);
                then(receiptRepository).should().save(any(Receipt.class));
                then(studentClubRepository).should().save(convergenceSoftware);
                then(userService).should().deleteUser("president123");
                then(userService).should().deleteUser("student1");
            }
        }

        @Nested
        @DisplayName("다음 회장이 존재하는 경우")
        class Context_with_next_president {

            @Test
            @DisplayName("다음 회장을 등록하고 학생회를 이전한다")
            void it_transfers_with_new_president() {
                // given
                Receipt depositReceipt = createReceipt(1L, 10000, 0, convergenceSoftware);
                List<Receipt> receipts = List.of(depositReceipt);

                PresidentDto nextPresidentDto = createPresidentDto(0L, "60221318", "박진형");

                given(userRepository.findByUserId("president123")).willReturn(Optional.of(president));
                given(receiptRepository.findAllByStudentClubOrderByIdDesc(convergenceSoftware)).willReturn(receipts);
                given(userRepository.findFirstByStudentClubAndRole(convergenceSoftware, "PRESIDENT")).willReturn(president);
                given(userRepository.findByStudentClubAndRole(convergenceSoftware, "STU")).willReturn(Collections.emptyList());

                // when
                TransferDto result = studentClubService.transferStudentClub(nextPresidentDto, currentUser);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getStudentClubName()).isEqualTo("융합소프트웨어학부 학생회");
                assertThat(result.getTotalDeposit()).isEqualTo(10000);
                assertThat(result.getNetAmount()).isEqualTo(10000);

                then(userRepository).should().findByUserId("president123");
                then(receiptRepository).should().findAllByStudentClubOrderByIdDesc(convergenceSoftware);
                then(receiptRepository).should().deleteAll(receipts);
                then(receiptRepository).should().save(any(Receipt.class));
                then(studentClubRepository).should().save(convergenceSoftware);
                then(userService).should().deleteUser("president123");
                then(adminService).should().savePresident(any(PresidentDto.class));
            }
        }

        @Nested
        @DisplayName("영수증이 0개인 경우")
        class Context_with_empty_receipts {

            @Test
            @DisplayName("이월할 영수증이 없다는 예외를 던진다")
            void it_throws_empty_receipts_exception() {
                // given
                List<Receipt> receipts = Collections.emptyList();

                given(userRepository.findByUserId("president123")).willReturn(Optional.of(president));
                given(receiptRepository.findAllByStudentClubOrderByIdDesc(convergenceSoftware)).willReturn(receipts);

                // when & then
                assertThatThrownBy(() -> studentClubService.transferStudentClub(null, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasMessage("이월할 영수증이 없습니다.");

                then(userRepository).should().findByUserId("president123");
                then(receiptRepository).should().findAllByStudentClubOrderByIdDesc(convergenceSoftware);
            }
        }
    }
}