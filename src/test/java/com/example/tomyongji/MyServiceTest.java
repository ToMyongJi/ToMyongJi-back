package com.example.tomyongji;

import static com.example.tomyongji.validation.ErrorMsg.EXISTING_USER;
import static com.example.tomyongji.validation.ErrorMsg.MISMATCHED_USER;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_MEMBER;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_ROLE;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.auth.entity.ClubVerification;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.ClubVerificationRepository;
import com.example.tomyongji.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.auth.service.CustomUserDetails;
import com.example.tomyongji.my.dto.MyDto;
import com.example.tomyongji.my.dto.SaveMemberDto;
import com.example.tomyongji.my.mapper.MyMapper;
import com.example.tomyongji.my.service.MyService;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
public class MyServiceTest {

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

    //각 테스트 메서드 실행 전에 @Mock 과 @InjectMock 필드 초기화
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
        currentUser = (UserDetails) new org.springframework.security.core.userdetails.User("testUser","password123", Collections.emptyList());
        anotherCurrentUser = (UserDetails) new org.springframework.security.core.userdetails.User("anotherUser","password123", Collections.emptyList());
    }

//    @AfterEach
//    void refresh() {
//        userRepository.de
//    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_UserExists() {
        //Given
        Long id = user.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(
            Optional.of(user));
        when(myMapper.toMyDto(user)).thenReturn(myDto);

        //When
        MyDto result = myService.getMyInfo(id, currentUser);

        //Then
        assertNotNull(result); //반환된 DTO가 null이 아닌지 검증
        assertEquals("test name", result.getName());
        assertEquals("60000000", result.getStudentNum());
        assertEquals("스마트시스템공과대학", result.getCollege());
        assertEquals(30L, result.getStudentClubId());
        //userRepository 에서 findById()을 사용했는지
        verify(userRepository).findById(id);
        //myMapper 에서 toMyDto(user)를 사용했는지
        verify(myMapper).toMyDto(user);
    }

    @Test
    @DisplayName("유저 정보 조회 실패로 인한 내 정보 조회 실패")
    void getMyInfo_UserNotFound() {
        //Given
        Long invalidUserId = 999L;
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.getMyInfo(invalidUserId, currentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_USER, exception.getMessage());
        verify(userRepository).findById(invalidUserId);
        //userRepository.findById(userId); 에서 오류가 발생해야 하기 때문에
        //그 다음 로직인 mapper 가 작동이 되지 않은 것을 확인
        verify(myMapper, never()).toMyDto(any());
    }

    @Test
    @DisplayName("접속 정보 오류로 인한 내 정보 조회 실패")
    void getMyInfo_NoUser() {
        //Given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.getMyInfo(user.getId(), anotherCurrentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NO_AUTHORIZATION_USER, exception.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 학생회로 인한 내 정보 조회 실패")
    void getMyInfo_NoFoundStudentClub() {
        //Given
        Long id = user.getId();
        user.setStudentClub(null);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(
            Optional.of(user));

        //When, Then
        CustomException exception  = assertThrows(CustomException.class,
            () -> myService.getMyInfo(id, currentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_STUDENT_CLUB, exception.getMessage());
        verify(userRepository).findById(id);
        verify(myMapper, never()).toMyDto(any());
    }

    @Test
    @DisplayName("접속 정보 불일치로 인한 내 정보 조회 실패")
    void getMyInfo_MismatchedUser() {
        //Given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(anotherCurrentUser.getUsername())).thenReturn(
            Optional.of(anotherUser));

        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.getMyInfo(user.getId(), anotherCurrentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(MISMATCHED_USER, exception.getMessage());
    }

    @Test
    @DisplayName("멤버 정보 조회 성공")
    void getMembers_Success() {
        //Given
        Long id = user.getId();
        List<Member> memberList = Arrays.asList(
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

        List<MemberDto> expectedDtos = Arrays.asList(
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

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(
            Optional.of(user));
        when(memberRepository.findByStudentClub(studentClub)).thenReturn(memberList);
        when(myMapper.toMemberDto(memberList.get(0))).thenReturn(expectedDtos.get(0));
        when(myMapper.toMemberDto(memberList.get(1))).thenReturn(expectedDtos.get(1));

        //When
        List<MemberDto> result = myService.getMembers(id, currentUser);

        //Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("member1", result.get(0).getName());
        assertEquals("member2", result.get(1).getName());

        verify(userRepository).findById(id);
        verify(memberRepository).findByStudentClub(studentClub);
        verify(myMapper, times(2)).toMemberDto(any(Member.class));
    }
        @Test
    @DisplayName("유저 조회 실패로 인한 회원 목록 조회 실패")
    void getMembers_UserNotFound() {
        //Given
        Long invalidUserId = 999L;
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.getMembers(invalidUserId, currentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_USER, exception.getMessage());

        verify(userRepository).findById(invalidUserId);
        verify(memberRepository, never()).findByStudentClub(any());
        verify(myMapper, never()).toMemberDto((Member) any());
    }
    @Test
    @DisplayName("접속 정보 오류로 인한 회원 목록 조회 실패")
    void getMembers_NoUser() {
        //Given
        Long id = user.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.getMembers(id, anotherCurrentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NO_AUTHORIZATION_USER, exception.getMessage());
    }

    @Test
    @DisplayName("접속 정보 불일치로 인한 회원 목록 조회 실패")
    void getMembers_MismatchedUser() {
        //Given
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.ofNullable(user));
        when(userRepository.findByUserId(anotherCurrentUser.getUsername())).thenReturn(
            Optional.of(anotherUser));

        //When
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.getMembers(id, anotherCurrentUser));

        //Then
        assertEquals(400, exception.getErrorCode());
        assertEquals(MISMATCHED_USER, exception.getMessage());
        verify(userRepository).findById(id);
    }

    //회장의 유저 아이디를 통해 저장
    @Test
    @DisplayName("멤버 저장 성공")
    void saveMember_Success() {

        when(userRepository.findById(saveMemberDto.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(
            Optional.of(user));
        when(memberRepository.existsByStudentNum(saveMemberDto.getStudentNum())).thenReturn(false);
        when(myMapper.toMemberEntity(saveMemberDto)).thenReturn(member);

        //When
        myService.saveMember(saveMemberDto, currentUser);

        //Then
        verify(userRepository).findById(1L);
        verify(memberRepository).existsByStudentNum("60000001");
        verify(myMapper).toMemberEntity(saveMemberDto);
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("유저 조회 실패로 인한 멤버 저장 실패")
    void saveMember_UserNotFound() {
        //Given
        long invalidUserId = 999L;
        saveMemberDto.setId(invalidUserId);

        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.saveMember(saveMemberDto, currentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_USER, exception.getMessage());
        verify(userRepository).findById(invalidUserId);
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("접속 정보 오류로 인한 멤버 저장 실패")
    void saveMember_NoUser() {
        //Given
        when(userRepository.findById(saveMemberDto.getId())).thenReturn(Optional.of(user));

        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.saveMember(saveMemberDto, anotherCurrentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NO_AUTHORIZATION_USER, exception.getMessage());
    }

    @Test
    @DisplayName("접속 정보 불일치로 인한 멤버 저장 실패")
    void saveMember_MismatchedUser() {
        //Given
        when(userRepository.findById(saveMemberDto.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(anotherCurrentUser.getUsername())).thenReturn(Optional.of(anotherUser));
        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.saveMember(saveMemberDto, anotherCurrentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(MISMATCHED_USER, exception.getMessage());
        verify(userRepository).findById(1L);
    }


    @Test
    @DisplayName("이미 존재하는 학번으로 인한 멤버 저장 실패")
    void saveMember_ExistingUser() {
        //Given
        when(userRepository.findById(saveMemberDto.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(memberRepository.existsByStudentNum("60000001")).thenReturn(true);

        //WHen, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.saveMember(saveMemberDto, currentUser));
        assertEquals(400, exception.getErrorCode());
        assertEquals(EXISTING_USER, exception.getMessage());
        verify(userRepository).findById(1L);
        verify(memberRepository).existsByStudentNum("60000001");
        verify(myMapper, never()).toMemberEntity((SaveMemberDto) any());
    }

    @Test
    @DisplayName("존재하지 않는 학생회로 인한 멤버 저장 실패")
    void saveMember_NotFoundStudentClub() {
        //Given
        user.setStudentClub(null);
        when(userRepository.findById(saveMemberDto.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(memberRepository.existsByStudentNum("60000001")).thenReturn(false);

        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.saveMember(saveMemberDto, currentUser));
        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_STUDENT_CLUB, exception.getMessage());
        verify(userRepository).findById(1L);
        verify(memberRepository).existsByStudentNum("60000001");
        verify(myMapper, never()).toMemberEntity((SaveMemberDto) any());
    }

    @Test
    @DisplayName("멤버 삭제 성공")
    void deleteMember_Success() {
        //Given
        String deletedStudentNum = "60000001";
        Long deleteId = 1L;
        MemberDto memberDto = MemberDto.builder()
            .memberId(deleteId)
            .name("test name")
            .studentNum("60000001")
            .build();

        when(memberRepository.findByStudentNum(deletedStudentNum)).thenReturn(Optional.of(member));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(myMapper.toMemberDto(member)).thenReturn(memberDto);
        when(clubVerificationRepository.findByStudentNum(deletedStudentNum)).thenReturn(Collections.emptyList());
        when(userRepository.findByStudentNum(deletedStudentNum)).thenReturn(null);

        //When
        MemberDto result = myService.deleteMember(deletedStudentNum, currentUser);

        //Then
        assertNotNull(result);
        assertEquals("test name", result.getName());
        verify(memberRepository).findByStudentNum(deletedStudentNum);
        //verify(userRepository).findByStudentNum(deletedStudentNum);
        //verify(emailVerificationRepository).deleteByEmail("test@example.com");
        //verify(userRepository).delete(user);
        verify(memberRepository).delete(member);
    }

    @Test
    @DisplayName("존재하지 않는 멤버로 인한 멤버 삭제 실패")
    void deleteMember_NoMember() {
        //Given
        String deletedStudentNum = "60000002";

        //Then, When
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.deleteMember(deletedStudentNum, currentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_MEMBER, exception.getMessage());
    }

    @Test
    @DisplayName("접속 정보 오류로 인한 멤버 삭제 실패")
    void deleteMember_NoUser() {
        //Given
        String deletedStudentNum = "60000001";
        when(memberRepository.findByStudentNum("60000001")).thenReturn(Optional.of(member));

        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.deleteMember(
                deletedStudentNum, anotherCurrentUser));
        assertEquals(NOT_FOUND_USER, exception.getMessage());
        assertEquals(400, exception.getErrorCode());
    }

    @Test
    @DisplayName("소속 불일치로 인한 회원 삭제 실패")
    void deleteMembers_MismatchedRole() {
        //Given
        String deletedStudentNum = "60000001";
        when(memberRepository.findByStudentNum("60000001")).thenReturn(Optional.of(member));
        when(userRepository.findByUserId(anotherCurrentUser.getUsername())).thenReturn(Optional.of(anotherUser));

        //When
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.deleteMember(deletedStudentNum, anotherCurrentUser));

        //Then
        assertEquals(400, exception.getErrorCode());
        assertEquals(NO_AUTHORIZATION_ROLE, exception.getMessage());
    }
}
