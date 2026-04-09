package com.example.tomyongji.my;

import static com.example.tomyongji.global.error.ErrorMsg.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import com.example.tomyongji.domain.admin.dto.MemberDto;
import com.example.tomyongji.domain.admin.entity.Member;
import com.example.tomyongji.domain.admin.repository.MemberRepository;
import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.ClubVerificationRepository;
import com.example.tomyongji.domain.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.my.dto.MyDto;
import com.example.tomyongji.domain.my.dto.SaveMemberDto;
import com.example.tomyongji.domain.my.mapper.MyMapper;
import com.example.tomyongji.domain.my.service.MyService;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.repository.StudentClubRepository;
import com.example.tomyongji.global.error.CustomException;

import java.util.Arrays;
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
@DisplayName("MyService 클래스")
class MyServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private StudentClubRepository studentClubRepository;
    @Mock
    private ClubVerificationRepository clubVerificationRepository;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private MyMapper myMapper;

    @InjectMocks
    private MyService myService;

    private User user;
    private User anotherUser;
    private StudentClub studentClub;
    private StudentClub anotherStudentClub;
    private MyDto myDto;
    private SaveMemberDto saveMemberDto;
    private Member member;
    private UserDetails currentUser;
    private UserDetails anotherCurrentUser;

    @BeforeEach
    void setUp() {
        studentClub = StudentClub.builder()
                .id(30L)
                .studentClubName("스마트시스템공과대학 학생회")
                .build();

        anotherStudentClub = StudentClub.builder()
                .id(35L)
                .studentClubName("아너칼리지(자연)")
                .build();

        user = User.builder()
                .id(1L)
                .userId("testUser")
                .name("test name")
                .studentNum("60000000")
                .collegeName("스마트시스템공과대학")
                .email("test@example.com")
                .password("password123")
                .role("PRESIDENT")
                .studentClub(studentClub)
                .build();

        anotherUser = User.builder()
                .id(2L)
                .userId("anotherUser")
                .name("test name2")
                .studentNum("60000001")
                .collegeName("아너칼리지")
                .email("test2@example.com")
                .password("password123")
                .role("PRESIDENT")
                .studentClub(anotherStudentClub)
                .build();

        myDto = MyDto.builder()
                .name("test name")
                .studentNum("60000000")
                .college("스마트시스템공과대학")
                .studentClubId(30L)
                .build();

        saveMemberDto = SaveMemberDto.builder()
                .id(user.getId())
                .studentNum("60000001")
                .name("test name")
                .build();

        member = Member.builder()
                .name("test name")
                .studentNum("60000001")
                .studentClub(studentClub)
                .build();

        currentUser = new org.springframework.security.core.userdetails.User(
                "testUser",
                "password123",
                Collections.emptyList()
        );

        anotherCurrentUser = new org.springframework.security.core.userdetails.User(
                "anotherUser",
                "password123",
                Collections.emptyList()
        );
    }

    @Nested
    @DisplayName("getMyInfo 메서드는")
    class Describe_getMyInfo {

        @Nested
        @DisplayName("유효한 사용자 ID와 인증 정보가 주어지면")
        class Context_with_valid_user_and_authentication {

            @BeforeEach
            void setUp() {
                // given
                given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername()))
                        .willReturn(Optional.of(user));
                given(myMapper.toMyDto(user)).willReturn(myDto);
            }

            @Test
            @DisplayName("사용자 정보를 반환한다")
            void it_returns_user_info() {
                // when
                MyDto result = myService.getMyInfo(user.getId(), currentUser);

                // then
                assertThat(result).isNotNull();
                assertThat(result)
                        .extracting(
                                MyDto::getName,
                                MyDto::getStudentNum,
                                MyDto::getCollege,
                                MyDto::getStudentClubId
                        )
                        .containsExactly(
                                "test name",
                                "60000000",
                                "스마트시스템공과대학",
                                30L
                        );

                then(userRepository).should().findById(user.getId());
                then(myMapper).should().toMyDto(user);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 사용자 ID가 주어지면")
        class Context_with_nonexistent_user_id {

            Long invalidUserId = 999L;

            @BeforeEach
            void setUp() {
                // given
                given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());
            }

            @Test
            @DisplayName("NOT_FOUND_USER 예외를 던진다")
            void it_throws_not_found_user_exception() {
                // when & then
                assertThatThrownBy(() -> myService.getMyInfo(invalidUserId, currentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NOT_FOUND_USER);
                        });

                then(userRepository).should().findById(invalidUserId);
                then(myMapper).should(never()).toMyDto(any());
            }
        }

        @Nested
        @DisplayName("잘못된 인증 정보가 주어지면")
        class Context_with_invalid_authentication {

            @BeforeEach
            void setUp() {
                // given
                given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            }

            @Test
            @DisplayName("NO_AUTHORIZATION_USER 예외를 던진다")
            void it_throws_no_authorization_user_exception() {
                // when & then
                assertThatThrownBy(() -> myService.getMyInfo(user.getId(), anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NO_AUTHORIZATION_USER);
                        });
            }
        }

        @Nested
        @DisplayName("사용자의 학생회 정보가 없으면")
        class Context_with_no_student_club {

            @BeforeEach
            void setUp() {
                user.setStudentClub(null);

                // given
                given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername()))
                        .willReturn(Optional.of(user));
            }

            @Test
            @DisplayName("NOT_FOUND_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_found_student_club_exception() {
                // when & then
                assertThatThrownBy(() -> myService.getMyInfo(user.getId(), currentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NOT_FOUND_STUDENT_CLUB);
                        });

                then(userRepository).should().findById(user.getId());
                then(myMapper).should(never()).toMyDto(any());
            }
        }

        @Nested
        @DisplayName("인증 정보와 사용자 정보가 일치하지 않으면")
        class Context_with_mismatched_user {

            @BeforeEach
            void setUp() {
                // given
                given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
                given(userRepository.findByUserId(anotherCurrentUser.getUsername()))
                        .willReturn(Optional.of(anotherUser));
            }

            @Test
            @DisplayName("MISMATCHED_USER 예외를 던진다")
            void it_throws_mismatched_user_exception() {
                // when & then
                assertThatThrownBy(() -> myService.getMyInfo(user.getId(), anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(MISMATCHED_USER);
                        });
            }
        }
    }

    @Nested
    @DisplayName("getMembers 메서드는")
    class Describe_getMembers {

        @Nested
        @DisplayName("유효한 사용자 ID와 인증 정보가 주어지면")
        class Context_with_valid_user_and_authentication {

            List<Member> memberList;
            List<MemberDto> expectedDtos;

            @BeforeEach
            void setUp() {
                memberList = Arrays.asList(
                        Member.builder()
                                .id(1L)
                                .name("member1")
                                .studentNum("600001")
                                .studentClub(studentClub)
                                .build(),
                        Member.builder()
                                .id(2L)
                                .name("member2")
                                .studentNum("600002")
                                .studentClub(studentClub)
                                .build()
                );

                expectedDtos = Arrays.asList(
                        MemberDto.builder()
                                .memberId(1L)
                                .name("member1")
                                .studentNum("600001")
                                .build(),
                        MemberDto.builder()
                                .memberId(2L)
                                .name("member2")
                                .studentNum("600002")
                                .build()
                );

                // given
                given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername()))
                        .willReturn(Optional.of(user));
                given(memberRepository.findByStudentClub(studentClub)).willReturn(memberList);
                given(myMapper.toMemberDto(memberList.get(0))).willReturn(expectedDtos.get(0));
                given(myMapper.toMemberDto(memberList.get(1))).willReturn(expectedDtos.get(1));
            }

            @Test
            @DisplayName("멤버 목록을 반환한다")
            void it_returns_member_list() {
                // when
                List<MemberDto> result = myService.getMembers(user.getId(), currentUser);

                // then
                assertThat(result).isNotNull();
                assertThat(result).hasSize(2);
                assertThat(result)
                        .extracting(MemberDto::getName)
                        .containsExactly("member1", "member2");

                then(userRepository).should().findById(user.getId());
                then(memberRepository).should().findByStudentClub(studentClub);
                then(myMapper).should(times(2)).toMemberDto(any(Member.class));
            }
        }

        @Nested
        @DisplayName("존재하지 않는 사용자 ID가 주어지면")
        class Context_with_nonexistent_user_id {

            Long invalidUserId = 999L;

            @BeforeEach
            void setUp() {
                // given
                given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());
            }

            @Test
            @DisplayName("NOT_FOUND_USER 예외를 던진다")
            void it_throws_not_found_user_exception() {
                // when & then
                assertThatThrownBy(() -> myService.getMembers(invalidUserId, currentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NOT_FOUND_USER);
                        });

                then(userRepository).should().findById(invalidUserId);
                then(memberRepository).should(never()).findByStudentClub(any());
                then(myMapper).should(never()).toMemberDto(any(Member.class));
            }
        }

        @Nested
        @DisplayName("잘못된 인증 정보가 주어지면")
        class Context_with_invalid_authentication {

            @BeforeEach
            void setUp() {
                // given
                given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            }

            @Test
            @DisplayName("NO_AUTHORIZATION_USER 예외를 던진다")
            void it_throws_no_authorization_user_exception() {
                // when & then
                assertThatThrownBy(() -> myService.getMembers(user.getId(), anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NO_AUTHORIZATION_USER);
                        });
            }
        }

        @Nested
        @DisplayName("인증 정보와 사용자 정보가 일치하지 않으면")
        class Context_with_mismatched_user {

            @BeforeEach
            void setUp() {
                // given
                given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
                given(userRepository.findByUserId(anotherCurrentUser.getUsername()))
                        .willReturn(Optional.of(anotherUser));
            }

            @Test
            @DisplayName("MISMATCHED_USER 예외를 던진다")
            void it_throws_mismatched_user_exception() {
                // when & then
                assertThatThrownBy(() -> myService.getMembers(user.getId(), anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(MISMATCHED_USER);
                        });

                then(userRepository).should().findById(user.getId());
            }
        }
    }

    @Nested
    @DisplayName("saveMember 메서드는")
    class Describe_saveMember {

        @Nested
        @DisplayName("유효한 멤버 저장 정보가 주어지면")
        class Context_with_valid_member_info {

            @BeforeEach
            void setUp() {
                // given
                doReturn(Optional.of(user)).when(userRepository).findById(saveMemberDto.getId());
                doReturn(Optional.of(user)).when(userRepository).findByUserId(currentUser.getUsername());
                doReturn(false).when(memberRepository).existsByStudentNum(saveMemberDto.getStudentNum());
                doReturn(member).when(myMapper).toMemberEntity(saveMemberDto);
            }

            @Test
            @DisplayName("멤버를 저장한다")
            void it_saves_member() {
                // when
                myService.saveMember(saveMemberDto, currentUser);

                // then
                then(memberRepository).should().existsByStudentNum(saveMemberDto.getStudentNum());
                then(myMapper).should().toMemberEntity(saveMemberDto);
                then(memberRepository).should().save(member);
            }
        }

        @Nested
        @DisplayName("공백이 포함된 학번이 주어지면")
        class Context_with_whitespace_student_num {

            @Test
            @DisplayName("학번을 trim 처리하여 저장한다")
            void it_trims_student_num_and_saves_member() {
                // given
                SaveMemberDto dtoWithWhitespace = SaveMemberDto.builder()
                        .id(user.getId())
                        .studentNum("  60000001  ")
                        .name("test name")
                        .build();

                doReturn(Optional.of(user)).when(userRepository).findById(dtoWithWhitespace.getId());
                doReturn(Optional.of(user)).when(userRepository).findByUserId(currentUser.getUsername());
                doReturn(false).when(memberRepository).existsByStudentNum("60000001");
                doReturn(member).when(myMapper).toMemberEntity(dtoWithWhitespace);

                // when
                myService.saveMember(dtoWithWhitespace, currentUser);

                // then
                assertThat(dtoWithWhitespace.getStudentNum()).isEqualTo("60000001");
                then(memberRepository).should().existsByStudentNum("60000001");
                then(memberRepository).should().save(member);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 사용자 ID가 주어지면")
        class Context_with_nonexistent_user_id {

            @Test
            @DisplayName("NOT_FOUND_USER 예외를 던진다")
            void it_throws_not_found_user_exception() {
                // given
                Long invalidUserId = 999L;
                SaveMemberDto invalidDto = SaveMemberDto.builder()
                        .id(invalidUserId)
                        .studentNum("60000001")
                        .name("test name")
                        .build();

                lenient().doReturn(Optional.empty()).when(userRepository).findById(invalidUserId);

                // when & then
                assertThatThrownBy(() -> myService.saveMember(invalidDto, currentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NOT_FOUND_USER);
                        });

                then(memberRepository).should(never()).save(any());
            }
        }

        @Nested
        @DisplayName("잘못된 인증 정보가 주어지면")
        class Context_with_invalid_authentication {

            @BeforeEach
            void setUp() {
                // given
                doReturn(Optional.of(user)).when(userRepository).findById(saveMemberDto.getId());
            }

            @Test
            @DisplayName("NO_AUTHORIZATION_USER 예외를 던진다")
            void it_throws_no_authorization_user_exception() {
                // when & then
                assertThatThrownBy(() -> myService.saveMember(saveMemberDto, anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NO_AUTHORIZATION_USER);
                        });
            }
        }

        @Nested
        @DisplayName("인증 정보와 사용자 정보가 일치하지 않으면")
        class Context_with_mismatched_user {

            @BeforeEach
            void setUp() {
                // given
                doReturn(Optional.of(user)).when(userRepository).findById(saveMemberDto.getId());
                doReturn(Optional.of(anotherUser)).when(userRepository).findByUserId(anotherCurrentUser.getUsername());
            }

            @Test
            @DisplayName("MISMATCHED_USER 예외를 던진다")
            void it_throws_mismatched_user_exception() {
                // when & then
                assertThatThrownBy(() -> myService.saveMember(saveMemberDto, anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(MISMATCHED_USER);
                        });
            }
        }

        @Nested
        @DisplayName("이미 존재하는 학번이 주어지면")
        class Context_with_existing_student_number {

            @BeforeEach
            void setUp() {
                // given
                doReturn(Optional.of(user)).when(userRepository).findById(saveMemberDto.getId());
                doReturn(Optional.of(user)).when(userRepository).findByUserId(currentUser.getUsername());
                doReturn(true).when(memberRepository).existsByStudentNum(saveMemberDto.getStudentNum());
            }

            @Test
            @DisplayName("EXISTING_USER 예외를 던진다")
            void it_throws_existing_user_exception() {
                // when & then
                assertThatThrownBy(() -> myService.saveMember(saveMemberDto, currentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(EXISTING_USER);
                        });

                then(memberRepository).should().existsByStudentNum(saveMemberDto.getStudentNum());
                then(myMapper).should(never()).toMemberEntity(any(SaveMemberDto.class));
            }
        }

        @Nested
        @DisplayName("사용자의 학생회 정보가 없으면")
        class Context_with_no_student_club {

            User userWithoutClub;

            @BeforeEach
            void setUp() {
                userWithoutClub = User.builder()
                        .id(1L)
                        .userId("testUser")
                        .name("test name")
                        .studentNum("60000000")
                        .collegeName("스마트시스템공과대학")
                        .email("test@example.com")
                        .password("password123")
                        .role("PRESIDENT")
                        .studentClub(null)  // 학생회 없음
                        .build();

                // given
                doReturn(Optional.of(userWithoutClub)).when(userRepository).findById(saveMemberDto.getId());
                doReturn(Optional.of(userWithoutClub)).when(userRepository).findByUserId(currentUser.getUsername());
                doReturn(false).when(memberRepository).existsByStudentNum(saveMemberDto.getStudentNum());
            }

            @Test
            @DisplayName("NOT_FOUND_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_found_student_club_exception() {
                // when & then
                assertThatThrownBy(() -> myService.saveMember(saveMemberDto, currentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NOT_FOUND_STUDENT_CLUB);
                        });

                then(memberRepository).should().existsByStudentNum(saveMemberDto.getStudentNum());
                then(myMapper).should(never()).toMemberEntity(any(SaveMemberDto.class));
            }
        }
    }

    @Nested
    @DisplayName("deleteMember 메서드는")
    class Describe_deleteMember {

        @Nested
        @DisplayName("유효한 학번과 인증 정보가 주어지면")
        class Context_with_valid_student_number_and_authentication {

            String deletedStudentNum = "60000001";
            MemberDto memberDto;

            @BeforeEach
            void setUp() {
                memberDto = MemberDto.builder()
                        .memberId(1L)
                        .name("test name")
                        .studentNum("60000001")
                        .build();

                // given
                given(memberRepository.findByStudentNum(deletedStudentNum))
                        .willReturn(Optional.of(member));
                given(userRepository.findByUserId(currentUser.getUsername()))
                        .willReturn(Optional.of(user));
                given(myMapper.toMemberDto(member)).willReturn(memberDto);
                given(clubVerificationRepository.findByStudentNum(deletedStudentNum))
                        .willReturn(Collections.emptyList());
                given(userRepository.findByStudentNum(deletedStudentNum)).willReturn(null);
            }

            @Test
            @DisplayName("멤버를 삭제하고 삭제된 멤버 정보를 반환한다")
            void it_deletes_member_and_returns_deleted_info() {
                // when
                MemberDto result = myService.deleteMember(deletedStudentNum, currentUser);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getName()).isEqualTo("test name");

                then(memberRepository).should().findByStudentNum(deletedStudentNum);
                then(memberRepository).should().delete(member);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 멤버 학번이 주어지면")
        class Context_with_nonexistent_member {

            String deletedStudentNum = "60000002";

            @Test
            @DisplayName("NOT_FOUND_MEMBER 예외를 던진다")
            void it_throws_not_found_member_exception() {
                // when & then
                assertThatThrownBy(() -> myService.deleteMember(deletedStudentNum, currentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NOT_FOUND_MEMBER);
                        });
            }
        }

        @Nested
        @DisplayName("잘못된 인증 정보가 주어지면")
        class Context_with_invalid_authentication {

            String deletedStudentNum = "60000001";

            @BeforeEach
            void setUp() {
                // given
                given(memberRepository.findByStudentNum(deletedStudentNum))
                        .willReturn(Optional.of(member));
            }

            @Test
            @DisplayName("NOT_FOUND_USER 예외를 던진다")
            void it_throws_not_found_user_exception() {
                // when & then
                assertThatThrownBy(() -> myService.deleteMember(deletedStudentNum, anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NOT_FOUND_USER);
                        });
            }
        }

        @Nested
        @DisplayName("다른 학생회의 멤버를 삭제하려고 하면")
        class Context_with_member_from_different_club {

            String deletedStudentNum = "60000001";

            @BeforeEach
            void setUp() {
                // given
                given(memberRepository.findByStudentNum(deletedStudentNum))
                        .willReturn(Optional.of(member));
                given(userRepository.findByUserId(anotherCurrentUser.getUsername()))
                        .willReturn(Optional.of(anotherUser));
            }

            @Test
            @DisplayName("NO_AUTHORIZATION_ROLE 예외를 던진다")
            void it_throws_no_authorization_role_exception() {
                // when & then
                assertThatThrownBy(() -> myService.deleteMember(deletedStudentNum, anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .satisfies(exception -> {
                            CustomException customException = (CustomException) exception;
                            assertThat(customException.getErrorCode()).isEqualTo(400);
                            assertThat(customException.getMessage()).isEqualTo(NO_AUTHORIZATION_ROLE);
                        });
            }
        }
    }
}