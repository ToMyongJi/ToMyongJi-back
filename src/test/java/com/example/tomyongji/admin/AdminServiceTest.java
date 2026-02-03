package com.example.tomyongji.admin;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.mapper.AdminMapper;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.admin.service.AdminService;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.ClubVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.my.dto.AdminSaveMemberDto;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.tomyongji.validation.ErrorMsg.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock private MemberRepository memberRepository;
    @Mock private UserRepository userRepository;
    @Mock private PresidentRepository presidentRepository;
    @Mock private StudentClubRepository studentClubRepository;
    @Mock private ClubVerificationRepository clubVerificationRepository;
    @Mock private AdminMapper adminMapper;
    @Mock private PasswordEncoder encoder;

    private College college;
    private StudentClub studentClub;
    private President president;

    @BeforeEach
    void setUp() {
        president = President.builder()
                .name("투명지")
                .studentNum("60222024")
                .build();

        college = College.builder()
                .id(1L)
                .collegeName("ICT융합대학")
                .build();

        studentClub = StudentClub.builder()
                .id(1L)
                .studentClubName("ICT융합대학 학생회")
                .Balance(0)
                .college(college)
                .president(president)
                .build();
    }

    @Nested
    @DisplayName("getPresident 메서드는")
    class Describe_getPresident {

        @Nested
        @DisplayName("유효한 학생회 ID가 주어지면")
        class Context_with_valid_club_id {

            private PresidentDto presidentDto;

            @BeforeEach
            void setUp() {
                presidentDto = PresidentDto.builder()
                        .name("투명지")
                        .studentNum("60222024")
                        .clubId(1L)
                        .build();

                given(studentClubRepository.findById(studentClub.getId()))
                        .willReturn(Optional.of(studentClub));
                given(adminMapper.toPresidentDto(president))
                        .willReturn(presidentDto);
            }

            @Test
            @DisplayName("학생회장 정보를 반환한다")
            void it_returns_president_info() {
                // when
                PresidentDto response = adminService.getPresident(1L);

                // then
                assertThat(response)
                        .extracting(
                                PresidentDto::getStudentNum,
                                PresidentDto::getClubId
                        )
                        .containsExactly(
                                presidentDto.getStudentNum(),
                                presidentDto.getClubId()
                        );
            }
        }

        @Nested
        @DisplayName("존재하지 않는 학생회 ID가 주어지면")
        class Context_with_non_existent_club {

            private final Long clubId = 2L;

            @BeforeEach
            void setUp() {
                given(studentClubRepository.findById(clubId))
                        .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("NOT_FOUND_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_found_student_club_exception() {
                // when & then
                assertThatThrownBy(() -> adminService.getPresident(clubId))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(NOT_FOUND_STUDENT_CLUB);
            }

            @Test
            @DisplayName("매퍼 변환을 수행하지 않는다")
            void it_does_not_call_mapper() {
                // when & then
                assertThatThrownBy(() -> adminService.getPresident(clubId))
                        .isInstanceOf(CustomException.class);

                verify(adminMapper, never()).toPresidentDto(any());
            }
        }
    }

    @Nested
    @DisplayName("savePresident 메서드는")
    class Describe_savePresident {

        @Nested
        @DisplayName("유효한 회장 정보가 주어지면")
        class Context_with_valid_request {

            private PresidentDto presidentDto;

            @BeforeEach
            void setUp() {
                presidentDto = PresidentDto.builder()
                        .name("투명지")
                        .studentNum("60222024")
                        .clubId(1L)
                        .build();

                given(presidentRepository.existsByStudentNum(presidentDto.getStudentNum()))
                        .willReturn(false);
                given(studentClubRepository.findById(presidentDto.getClubId()))
                        .willReturn(Optional.of(studentClub));
                given(adminMapper.toPresidentEntity(presidentDto))
                        .willReturn(president);
                given(adminMapper.toPresidentDto(president))
                        .willReturn(presidentDto);

                doAnswer(invocation -> invocation.getArgument(0))
                        .when(presidentRepository).save(any(President.class));
                doAnswer(invocation -> invocation.getArgument(0))
                        .when(studentClubRepository).save(any(StudentClub.class));
            }

            @Test
            @DisplayName("저장된 회장 정보를 반환한다")
            void it_returns_saved_president() {
                // when
                PresidentDto response = adminService.savePresident(presidentDto);

                // then
                assertThat(response)
                        .extracting(
                                PresidentDto::getStudentNum,
                                PresidentDto::getClubId
                        )
                        .containsExactly(
                                presidentDto.getStudentNum(),
                                presidentDto.getClubId()
                        );
            }
        }

        @Nested
        @DisplayName("이미 존재하는 학번의 회장이 주어지면")
        class Context_with_existing_president {

            private final PresidentDto presidentDto = PresidentDto.builder()
                    .name("투명지")
                    .studentNum("60222024")
                    .clubId(1L)
                    .build();

            @BeforeEach
            void setUp() {
                given(presidentRepository.existsByStudentNum(presidentDto.getStudentNum()))
                        .willReturn(true);
            }

            @Test
            @DisplayName("EXISTING_USER 예외를 던진다")
            void it_throws_existing_user_exception() {
                // when & then
                assertThatThrownBy(() -> adminService.savePresident(presidentDto))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(EXISTING_USER);
            }

            @Test
            @DisplayName("엔티티 변환을 수행하지 않는다")
            void it_does_not_call_mapper() {
                // when & then
                assertThatThrownBy(() -> adminService.savePresident(presidentDto))
                        .isInstanceOf(CustomException.class);

                verify(adminMapper, never()).toPresidentEntity(any());
            }
        }

        @Nested
        @DisplayName("존재하지 않는 학생회가 주어지면")
        class Context_with_non_existent_club {

            private final PresidentDto presidentDto = PresidentDto.builder()
                    .name("투명지")
                    .studentNum("60222024")
                    .clubId(1L)
                    .build();

            @BeforeEach
            void setUp() {
                given(presidentRepository.existsByStudentNum(presidentDto.getStudentNum()))
                        .willReturn(false);
                given(adminMapper.toPresidentEntity(presidentDto))
                        .willReturn(president);

                doAnswer(invocation -> invocation.getArgument(0))
                        .when(presidentRepository).save(any(President.class));

                given(studentClubRepository.findById(presidentDto.getClubId()))
                        .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("NOT_FOUND_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_found_student_club_exception() {
                // when & then
                assertThatThrownBy(() -> adminService.savePresident(presidentDto))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(NOT_FOUND_STUDENT_CLUB);
            }

            @Test
            @DisplayName("학생회 저장을 수행하지 않는다")
            void it_does_not_save_student_club() {
                // when & then
                assertThatThrownBy(() -> adminService.savePresident(presidentDto))
                        .isInstanceOf(CustomException.class);

                verify(studentClubRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("updatePresident 메서드는")
    class Describe_updatePresident {

        @Nested
        @DisplayName("유효한 신임 회장 정보가 주어지면")
        class Context_with_valid_request {

            private PresidentDto newPresidentDto;
            private President newPresident;
            private User user;
            private Member member;

            @BeforeEach
            void setUp() {
                newPresidentDto = PresidentDto.builder()
                        .name("투명지2025")
                        .studentNum("60222025")
                        .clubId(1L)
                        .build();

                newPresident = President.builder()
                        .name("투명지2025")
                        .studentNum("60222025")
                        .build();

                user = User.builder()
                        .id(1L)
                        .userId("tomyongji2024")
                        .name("투명지")
                        .password(encoder.encode("*Tomyongji2024"))
                        .role("PRESIDENT")
                        .email("eeeseohyun615@gmail.com")
                        .collegeName(college.getCollegeName())
                        .studentClub(studentClub)
                        .studentNum("60222024")
                        .build();

                member = Member.builder()
                        .name("투명지")
                        .studentNum("60222024")
                        .studentClub(studentClub)
                        .build();

                given(studentClubRepository.findById(newPresidentDto.getClubId()))
                        .willReturn(Optional.of(studentClub));
                given(userRepository.findFirstByStudentClubAndRole(studentClub, "PRESIDENT"))
                        .willReturn(user);
                given(adminMapper.toMemberEntity(user))
                        .willReturn(member);
                given(presidentRepository.findByStudentNum(user.getStudentNum()))
                        .willReturn(president);
                given(adminMapper.toPresidentDto(newPresident))
                        .willReturn(newPresidentDto);

                doAnswer(invocation -> invocation.getArgument(0))
                        .when(userRepository).save(any(User.class));
                doAnswer(invocation -> invocation.getArgument(0))
                        .when(memberRepository).save(any(Member.class));
                doAnswer(invocation -> invocation.getArgument(0))
                        .when(studentClubRepository).save(any(StudentClub.class));
                doAnswer(invocation -> invocation.getArgument(0))
                        .when(presidentRepository).save(any(President.class));
            }

            @Test
            @DisplayName("수정된 회장 정보를 반환한다")
            void it_returns_updated_president() {
                // when
                PresidentDto response = adminService.updatePresident(newPresidentDto);

                // then
                assertThat(response)
                        .extracting(
                                PresidentDto::getStudentNum,
                                PresidentDto::getName,
                                PresidentDto::getClubId
                        )
                        .containsExactly(
                                newPresident.getStudentNum(),
                                newPresident.getName(),
                                newPresidentDto.getClubId()
                        );
            }
        }

        @Nested
        @DisplayName("존재하지 않는 학생회가 주어지면")
        class Context_with_non_existent_club {

            private final PresidentDto newPresidentDto = PresidentDto.builder()
                    .name("투명지2025")
                    .studentNum("60222025")
                    .clubId(1L)
                    .build();

            @BeforeEach
            void setUp() {
                given(studentClubRepository.findById(newPresidentDto.getClubId()))
                        .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("NOT_FOUND_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_found_student_club_exception() {
                // when & then
                assertThatThrownBy(() -> adminService.updatePresident(newPresidentDto))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(NOT_FOUND_STUDENT_CLUB);
            }

            @Test
            @DisplayName("기존 회장 유저 조회를 수행하지 않는다")
            void it_does_not_query_president_user() {
                // when & then
                assertThatThrownBy(() -> adminService.updatePresident(newPresidentDto))
                        .isInstanceOf(CustomException.class);

                verify(userRepository, never()).findFirstByStudentClubAndRole(any(), any());
            }
        }
    }

    @Nested
    @DisplayName("getMembers 메서드는")
    class Describe_getMembers {

        @Nested
        @DisplayName("유효한 학생회 ID가 주어지면")
        class Context_with_valid_club_id {

            private Member member;
            private MemberDto memberDto;

            @BeforeEach
            void setUp() {
                member = Member.builder()
                        .name("투명지")
                        .studentNum("60222024")
                        .studentClub(studentClub)
                        .build();

                memberDto = MemberDto.builder()
                        .name("투명지")
                        .studentNum("60222024")
                        .build();

                given(studentClubRepository.findById(studentClub.getId()))
                        .willReturn(Optional.of(studentClub));
                given(memberRepository.findByStudentClub(studentClub))
                        .willReturn(List.of(member));
                given(adminMapper.toMemberDto(member))
                        .willReturn(memberDto);
            }

            @Test
            @DisplayName("부원 목록을 반환한다")
            void it_returns_member_list() {
                // when
                List<MemberDto> response = adminService.getMembers(studentClub.getId());

                // then
                assertThat(response)
                        .hasSize(1)
                        .first()
                        .extracting(
                                MemberDto::getName,
                                MemberDto::getStudentNum
                        )
                        .containsExactly(
                                memberDto.getName(),
                                memberDto.getStudentNum()
                        );
            }
        }

        @Nested
        @DisplayName("존재하지 않는 학생회 ID가 주어지면")
        class Context_with_non_existent_club {

            @BeforeEach
            void setUp() {
                given(studentClubRepository.findById(studentClub.getId()))
                        .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("NOT_FOUND_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_found_student_club_exception() {
                // when & then
                assertThatThrownBy(() -> adminService.getMembers(studentClub.getId()))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(NOT_FOUND_STUDENT_CLUB);
            }

            @Test
            @DisplayName("부원 조회를 수행하지 않는다")
            void it_does_not_query_members() {
                // when & then
                assertThatThrownBy(() -> adminService.getMembers(studentClub.getId()))
                        .isInstanceOf(CustomException.class);

                verify(memberRepository, never()).findByStudentClub(any());
            }
        }
    }

    @Nested
    @DisplayName("saveMember 메서드는")
    class Describe_saveMember {

        @Nested
        @DisplayName("유효한 부원 정보가 주어지면")
        class Context_with_valid_request {

            private Member member;
            private AdminSaveMemberDto savedMemberDto;
            private MemberDto memberDto;

            @BeforeEach
            void setUp() {
                member = Member.builder()
                        .name("투명지")
                        .studentNum("60222024")
                        .studentClub(studentClub)
                        .build();

                savedMemberDto = AdminSaveMemberDto.builder()
                        .name("투명지")
                        .studentNum("60222024")
                        .clubId(1L)
                        .build();

                memberDto = MemberDto.builder()
                        .name("투명지")
                        .studentNum("60222024")
                        .build();

                given(memberRepository.existsByStudentNum(savedMemberDto.getStudentNum()))
                        .willReturn(false);
                given(studentClubRepository.findById(savedMemberDto.getClubId()))
                        .willReturn(Optional.of(studentClub));
                given(adminMapper.toMemberEntity(savedMemberDto))
                        .willReturn(member);
                given(adminMapper.toMemberDto(member))
                        .willReturn(memberDto);

                doAnswer(invocation -> invocation.getArgument(0))
                        .when(memberRepository).save(any(Member.class));
            }

            @Test
            @DisplayName("저장된 부원 정보를 반환한다")
            void it_returns_saved_member() {
                // when
                MemberDto response = adminService.saveMember(savedMemberDto);

                // then
                assertThat(response)
                        .extracting(
                                MemberDto::getName,
                                MemberDto::getStudentNum
                        )
                        .containsExactly(
                                savedMemberDto.getName(),
                                savedMemberDto.getStudentNum()
                        );
            }
        }

        @Nested
        @DisplayName("이미 존재하는 학번의 부원이 주어지면")
        class Context_with_existing_member {

            private final AdminSaveMemberDto savedMemberDto = AdminSaveMemberDto.builder()
                    .name("투명지")
                    .studentNum("60222024")
                    .clubId(1L)
                    .build();

            @BeforeEach
            void setUp() {
                given(memberRepository.existsByStudentNum(savedMemberDto.getStudentNum()))
                        .willReturn(true);
            }

            @Test
            @DisplayName("EXISTING_USER 예외를 던진다")
            void it_throws_existing_user_exception() {
                // when & then
                assertThatThrownBy(() -> adminService.saveMember(savedMemberDto))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(EXISTING_USER);
            }

            @Test
            @DisplayName("학생회 조회를 수행하지 않는다")
            void it_does_not_query_student_club() {
                // when & then
                assertThatThrownBy(() -> adminService.saveMember(savedMemberDto))
                        .isInstanceOf(CustomException.class);

                verify(studentClubRepository, never()).findById(any());
            }
        }

        @Nested
        @DisplayName("존재하지 않는 학생회가 주어지면")
        class Context_with_non_existent_club {

            private final AdminSaveMemberDto savedMemberDto = AdminSaveMemberDto.builder()
                    .name("투명지")
                    .studentNum("60222024")
                    .clubId(1L)
                    .build();

            @BeforeEach
            void setUp() {
                given(memberRepository.existsByStudentNum(savedMemberDto.getStudentNum()))
                        .willReturn(false);
                given(studentClubRepository.findById(savedMemberDto.getClubId()))
                        .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("NOT_FOUND_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_found_student_club_exception() {
                // when & then
                assertThatThrownBy(() -> adminService.saveMember(savedMemberDto))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(NOT_FOUND_STUDENT_CLUB);
            }

            @Test
            @DisplayName("엔티티 변환을 수행하지 않는다")
            void it_does_not_call_mapper() {
                // when & then
                assertThatThrownBy(() -> adminService.saveMember(savedMemberDto))
                        .isInstanceOf(CustomException.class);

                verify(adminMapper, never()).toMemberEntity(any(AdminSaveMemberDto.class));
            }
        }
    }

    @Nested
    @DisplayName("deleteMember 메서드는")
    class Describe_deleteMember {

        @Nested
        @DisplayName("유효한 부원 ID가 주어지면")
        class Context_with_valid_member_id {

            private final long memberId = 1L;
            private Member member;
            private MemberDto memberDto;

            @BeforeEach
            void setUp() {
                member = Member.builder()
                        .id(1L)
                        .name("투명지")
                        .studentNum("60222024")
                        .studentClub(studentClub)
                        .build();

                memberDto = MemberDto.builder()
                        .memberId(1L)
                        .name("투명지")
                        .studentNum("60222024")
                        .build();

                given(memberRepository.findById(memberId))
                        .willReturn(Optional.of(member));
                given(adminMapper.toMemberDto(member))
                        .willReturn(memberDto);
                given(clubVerificationRepository.findByStudentNum(member.getStudentNum()))
                        .willReturn(Collections.emptyList());
                given(userRepository.findByStudentNum(member.getStudentNum()))
                        .willReturn(null);
            }

            @Test
            @DisplayName("삭제된 부원 정보를 반환한다")
            void it_returns_deleted_member() {
                // when
                MemberDto response = adminService.deleteMember(memberId);

                // then
                assertThat(response)
                        .extracting(
                                MemberDto::getMemberId,
                                MemberDto::getStudentNum
                        )
                        .containsExactly(
                                memberDto.getMemberId(),
                                memberDto.getStudentNum()
                        );
            }
        }

        @Nested
        @DisplayName("존재하지 않는 부원 ID가 주어지면")
        class Context_with_non_existent_member {

            private final long memberId = 1L;

            @BeforeEach
            void setUp() {
                given(memberRepository.findById(memberId))
                        .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("NOT_FOUND_MEMBER 예외를 던진다")
            void it_throws_not_found_member_exception() {
                // when & then
                assertThatThrownBy(() -> adminService.deleteMember(memberId))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(NOT_FOUND_MEMBER);
            }

            @Test
            @DisplayName("매퍼 변환을 수행하지 않는다")
            void it_does_not_call_mapper() {
                // when & then
                assertThatThrownBy(() -> adminService.deleteMember(memberId))
                        .isInstanceOf(CustomException.class);

                verify(adminMapper, never()).toMemberDto(any(Member.class));
            }
        }
    }
}