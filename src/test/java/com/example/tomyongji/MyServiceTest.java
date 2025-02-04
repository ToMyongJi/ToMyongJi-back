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

@ExtendWith(MockitoExtension.class)
public class MyServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ClubVerificationRepository clubVerificationRepository;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private MyMapper myMapper;

    @InjectMocks
    private MyService myService;

    private User user;
    private StudentClub studentClub;
    private MyDto myDto;
    private SaveMemberDto saveMemberDto;
    private Member member;
    //private CustomUserDetails currentUser;

    //각 테스트 메서드 실행 전에 @Mock 과 @InjectMock 필드 초기화
    @BeforeEach
    void setUp() {

        studentClub = StudentClub.builder()
            .id(3L)
            .studentClubName("융합소프트웨어학부")
            .build();

        user = User.builder()
            .id(1L)
            .userId("testUser")
            .name("test name")
            .studentNum("60000000")
            .collegeName("ICT 융합대학")
            .email("test@example.com")
            .password("password123")
            .role("PRESIDENT")
            .studentClub(studentClub)
            .build();

        myDto = MyDto.builder()
            .name("test name")
            .studentNum("60000000")
            .college("ICT 융합대학")
            .studentClubId(3L)
            .build();

        saveMemberDto = SaveMemberDto.builder()
            .id(1L)
            .studentNum("600000")
            .name("test name")
            .build();

        member = Member.builder()
            .name("test name")
            .studentNum("600000")
            .build();
        //currentUser = new CustomUserDetails(user);

    }

//    @AfterEach
//    void refresh() {
//        userRepository.de
//    }

//    @Test
//    @DisplayName("내 정보 조회 성공")
//    void getMyInfo_UserExists() {
//        //Given
//        Long id = 1L;
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//        when(myMapper.toMyDto(user)).thenReturn(myDto);
//
//        //When
//        MyDto result = myService.getMyInfo(id, currentUser);
//
//        //Then
//        assertNotNull(result); //반환된 DTO가 null이 아닌지 검증
//        assertEquals("test name", result.getName());
//        assertEquals("60000000", result.getStudentNum());
//        assertEquals("ICT 융합대학", result.getCollege());
//        assertEquals(3L, result.getStudentClubId());
//        //userRepository 에서 findById(1L)을 사용했는지
//        verify(userRepository).findById(id);
//        //myMapper 에서 toMyDto(user)를 사용했는지
//        verify(myMapper).toMyDto(user);
//    }
//
//    @Test
//    @DisplayName("유저 정보 조회 실패로 인한 내 정보 조회 실패")
//    void getMyInfo_UserNotFound() {
//        //Given
//        Long invalidUserId = 999L;
//        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());
//
//        //When, Then
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.getMyInfo(invalidUserId, currentUser));
//
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(NOT_FOUND_USER, exception.getMessage());
//        verify(userRepository).findById(invalidUserId);
//        //userRepository.findById(userId); 에서 오류가 발생해야 하기 때문에
//        //그 다음 로직인 mapper 가 작동이 되지 않은 것을 확인
//        verify(myMapper, never()).toMyDto(any());
//    }
//
//    @Test
//    @DisplayName("접속 정보 오류로 인한 내 정보 조회 실패")
//    void getMyInfo_NoUser() {
//        //Given
//        Long id = 1L;
//        CustomUserDetails emptyCurrentUser = new CustomUserDetails(null);
//
//        //When, Then
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.getMyInfo(id, emptyCurrentUser));
//
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(NO_AUTHORIZATION_USER, exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 학생회로 인한 내 정보 조회 실패")
//    void getMyInfo_NoFoundStudentClub() {
//        //Given
//        Long id = 1L;
//        user.setStudentClub(null);
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//
//        //When, Then
//        CustomException exception  = assertThrows(CustomException.class,
//            () -> myService.getMyInfo(id, currentUser));
//
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(NOT_FOUND_STUDENT_CLUB, exception.getMessage());
//        verify(userRepository).findById(id);
//        verify(myMapper, never()).toMyDto(any());
//    }
//
//    @Test
//    @DisplayName("접속 정보 불일치로 인한 내 정보 조회 실패")
//    void getMyInfo_MismatchedUser() {
//        //Given
//        Long id = 1L;
//        User user2 = User.builder()
//            .id(2L)
//            .userId("testUser2")
//            .name("test name2")
//            .studentNum("60000001")
//            .collegeName("ICT 융합대학")
//            .email("test@example2.com")
//            .password("password123")
//            .role("USER")
//            .studentClub(studentClub)
//            .build();
//        CustomUserDetails currentUser = new CustomUserDetails(user2);
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//
//        //When, Then
//        CustomException exception  = assertThrows(CustomException.class,
//            () -> myService.getMyInfo(id, currentUser));
//
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(MISMATCHED_USER, exception.getMessage());
//        verify(userRepository).findById(id);
//    }
//
//    @Test
//    @DisplayName("멤버 정보 조회 성공")
//    void getMembers_Success() {
//        //Given
//        Long id = 1L;
//        List<Member> memberList = Arrays.asList(
//            Member.builder()
//                .id(1L)
//                .name("member1")
//                .studentNum("600001")
//                .studentClub(studentClub)
//                .build(),
//            Member.builder()
//                .id(2L)
//                .name("member2")
//                .studentNum("600002")
//                .studentClub(studentClub)
//                .build()
//        );
//
//        List<MemberDto> expectedDtos = Arrays.asList(
//            MemberDto.builder()
//                .memberId(1L)
//                .name("member1")
//                .studentNum("600001")
//                .build(),
//            MemberDto.builder()
//                .memberId(2L)
//                .name("member2")
//                .studentNum("600002")
//                .build()
//        );
//
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//        when(memberRepository.findByStudentClub(studentClub)).thenReturn(memberList);
//        when(myMapper.toMemberDto(memberList.get(0))).thenReturn(expectedDtos.get(0));
//        when(myMapper.toMemberDto(memberList.get(1))).thenReturn(expectedDtos.get(1));
//
//        //When
//        List<MemberDto> result = myService.getMembers(id, currentUser);
//
//        //Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals("member1", result.get(0).getName());
//        assertEquals("member2", result.get(1).getName());
//
//        verify(userRepository).findById(id);
//        verify(memberRepository).findByStudentClub(studentClub);
//        verify(myMapper, times(2)).toMemberDto(any(Member.class));
//    }
//
//    @Test
//    @DisplayName("접속 정보 오류로 인한 회원 목록 조회 실패")
//    void getMembers_NoUser() {
//        //Given
//        Long id = 1L;
//        CustomUserDetails emptyCurrentUser = new CustomUserDetails(null);
//
//        //When, Then
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.getMembers(id, emptyCurrentUser));
//
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(NO_AUTHORIZATION_USER, exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("접속 정보 불일치로 인한 회원 목록 조회 실패")
//    void getMembers_MismatchedUser() {
//        //Given
//        Long id = 1L;
//        User user2 = User.builder()
//            .id(2L)
//            .userId("testUser2")
//            .name("test name2")
//            .studentNum("60000001")
//            .collegeName("ICT 융합대학")
//            .email("test@example2.com")
//            .password("password123")
//            .role("USER")
//            .studentClub(studentClub)
//            .build();
//        CustomUserDetails currentUser2 = new CustomUserDetails(user2);
//        when(userRepository.findById(id)).thenReturn(Optional.ofNullable(user));
//
//        //When
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.getMembers(id, currentUser2));
//
//        //Then
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(MISMATCHED_USER, exception.getMessage());
//        verify(userRepository).findById(id);
//    }
//    @Test
//    @DisplayName("권한 불일치로 인한 회원 목록 조회 실패")
//    void getMembers_MismatchedRole() {
//        //Given
//        User user2 = User.builder()
//            .id(2L)
//            .userId("testUser2")
//            .name("test name2")
//            .studentNum("60000001")
//            .collegeName("ICT 융합대학")
//            .email("test@example2.com")
//            .password("password123")
//            .role("USER")
//            .studentClub(studentClub)
//            .build();
//        CustomUserDetails currentUser2 = new CustomUserDetails(user2);
//
//        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
//
//        //When
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.getMembers(user2.getId(), currentUser2));
//
//        //Then
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(NO_AUTHORIZATION_ROLE, exception.getMessage());
//        verify(userRepository).findById(user2.getId());
//    }
//
//    @Test
//    @DisplayName("유저 조회 실패로 인한 회원 목록 조회 실패")
//    void getMembers_UserNotFound() {
//        //Given
//        Long invalidUserId = 999L;
//        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());
//
//        //When, Then
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.getMembers(invalidUserId, currentUser));
//
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(NOT_FOUND_USER, exception.getMessage());
//
//        verify(userRepository).findById(invalidUserId);
//        verify(memberRepository, never()).findByStudentClub(any());
//        verify(myMapper, never()).toMemberDto((Member) any());
//    }
//    @Test
//    @DisplayName("빈 회원 목록 조회")
//    void getMembers_EmptyList() {
//        //Given
//        Long id = 1L;
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//        when(memberRepository.findByStudentClub(studentClub)).thenReturn(Collections.emptyList());
//
//        //When
//        List<MemberDto> result = myService.getMembers(id, currentUser);
//
//        //Then
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//
//        verify(userRepository).findById(id);
//        verify(memberRepository).findByStudentClub(studentClub);
//        verify(myMapper, never()).toMemberDto((Member) any());
//    }
//
//    //회장의 유저 아이디를 통해 저장
//    @Test
//    @DisplayName("멤버 저장 성공")
//    void saveMember_Success() {
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(memberRepository.existsByStudentNum(saveMemberDto.getStudentNum())).thenReturn(false);
//        when(myMapper.toMemberEntity(saveMemberDto)).thenReturn(member);
//
//        //When
//        myService.saveMember(saveMemberDto, currentUser);
//
//        //Then
//        verify(userRepository).findById(1L);
//        verify(memberRepository).existsByStudentNum("600000");
//        verify(myMapper).toMemberEntity(saveMemberDto);
//        verify(memberRepository).save(member);
//    }
//
//    @Test
//    @DisplayName("유저 조회 실패로 인한 멤버 저장 실패")
//    void saveMember_UserNotFound() {
//        //Given
//        long invalidUserId = 999L;
//        saveMemberDto.setId(invalidUserId);
//
//        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());
//
//        //When, Then
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.saveMember(saveMemberDto, currentUser));
//
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(NOT_FOUND_USER, exception.getMessage());
//        verify(userRepository).findById(invalidUserId);
//        verify(memberRepository, never()).save(any());
//    }
//    @Test
//    @DisplayName("접속 정보 오류로 인한 멤버 저장 실패")
//    void saveMember_NoUser() {
//        //Given
//        CustomUserDetails emptyCurrentUser = new CustomUserDetails(null);
//
//        //When, Then
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.saveMember(saveMemberDto, emptyCurrentUser));
//
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(NO_AUTHORIZATION_USER, exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("접속 정보 불일치로 인한 멤버 저장 실패")
//    void saveMember_MismatchedUser() {
//        //Given
//        User user2 = User.builder()
//            .id(2L)
//            .userId("testUser2")
//            .name("test name2")
//            .studentNum("60000001")
//            .collegeName("ICT 융합대학")
//            .email("test@example2.com")
//            .password("password123")
//            .role("USER")
//            .studentClub(studentClub)
//            .build();
//        CustomUserDetails currentUser2 = new CustomUserDetails(user2);
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        //When, Then
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.saveMember(saveMemberDto, currentUser2));
//
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(MISMATCHED_USER, exception.getMessage());
//        verify(userRepository).findById(1L);
//    }
//
//    @Test
//    @DisplayName("권한 불일치로 인한 회원 저장 실패")
//    void saveMembers_MismatchedRole() {
//        //Given
//        User user2 = User.builder()
//            .id(2L)
//            .userId("testUser2")
//            .name("test name2")
//            .studentNum("60000001")
//            .collegeName("ICT 융합대학")
//            .email("test@example2.com")
//            .password("password123")
//            .role("USER")
//            .studentClub(studentClub)
//            .build();
//        saveMemberDto.setId(user2.getId());
//        CustomUserDetails currentUser2 = new CustomUserDetails(user2);
//
//        when(userRepository.findById(saveMemberDto.getId())).thenReturn(Optional.of(user2));
//
//        //When
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.saveMember(saveMemberDto, currentUser2));
//
//        //Then
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(NO_AUTHORIZATION_ROLE, exception.getMessage());
//        verify(userRepository).findById(saveMemberDto.getId());
//    }
//
//    @Test
//    @DisplayName("이미 존재하는 학번으로 인한 멤버 저장 실패")
//    void saveMember_ExistingUser() {
//        //Given
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(memberRepository.existsByStudentNum("600000")).thenReturn(true);
//
//        //WHen, Then
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.saveMember(saveMemberDto, currentUser));
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(EXISTING_USER, exception.getMessage());
//        verify(userRepository).findById(1L);
//        verify(memberRepository).existsByStudentNum("600000");
//        verify(myMapper, never()).toMemberEntity((SaveMemberDto) any());
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 학생회로 인한 멤버 저장 실패")
//    void saveMember_NotFoundStudentClub() {
//        //Given
//        user.setStudentClub(null);
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(memberRepository.existsByStudentNum("600000")).thenReturn(false);
//
//        //When, Then
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.saveMember(saveMemberDto, currentUser));
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(NOT_FOUND_STUDENT_CLUB, exception.getMessage());
//        verify(userRepository).findById(1L);
//        verify(memberRepository).existsByStudentNum("600000");
//        verify(myMapper, never()).toMemberEntity((SaveMemberDto) any());
//    }
//
//    @Test
//    @DisplayName("멤버 삭제 성공")
//    void deleteMember_Success() {
//        //Given
//        String deletedStudentNum = "600000";
//        Long deleteId = 1L;
//        MemberDto memberDto = MemberDto.builder()
//            .memberId(deleteId)
//            .name("test name")
//            .studentNum("600000")
//            .build();
//
//        when(memberRepository.findByStudentNum(deletedStudentNum)).thenReturn(Optional.of(member));
//        when(userRepository.findByStudentNum(deletedStudentNum)).thenReturn(user);
//        when(myMapper.toMemberDto(member)).thenReturn(memberDto);
//
//        //When
//        MemberDto result = myService.deleteMember(deletedStudentNum, currentUser);
//
//        //Then
//        assertNotNull(result);
//        assertEquals("test name", result.getName());
//        verify(memberRepository).findByStudentNum(deletedStudentNum);
//        verify(userRepository).findByStudentNum(deletedStudentNum);
//        verify(emailVerificationRepository).deleteByEmail("test@example.com");
//        verify(userRepository).delete(user);
//        verify(memberRepository).delete(member);
//    }
//
//    @Test
//    @DisplayName("접속 정보 오류로 인한 멤버 삭제 실패")
//    void deleteMember_NoUser() {
//        //Given
//        String deletedStudentNum = "600000";
//        CustomUserDetails emptyCurrentUser = new CustomUserDetails(null);
//
//        //When, Then
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.deleteMember(
//                deletedStudentNum, emptyCurrentUser));
//        assertEquals(NO_AUTHORIZATION_USER, exception.getMessage());
//        assertEquals(400, exception.getErrorCode());
//    }
//
//    @Test
//    @DisplayName("권한 불일치로 인한 회원 삭제 실패")
//    void deleteMembers_MismatchedRole() {
//        //Given
//        String deletedStudentNum = "600000";
//        User user2 = User.builder()
//            .id(2L)
//            .userId("testUser2")
//            .name("test name2")
//            .studentNum("60000001")
//            .collegeName("ICT 융합대학")
//            .email("test@example2.com")
//            .password("password123")
//            .role("USER")
//            .studentClub(studentClub)
//            .build();
//
//        CustomUserDetails currentUser2 = new CustomUserDetails(user2);
//
//        //When
//        CustomException exception = assertThrows(CustomException.class,
//            () -> myService.deleteMember(deletedStudentNum, currentUser2));
//
//        //Then
//        assertEquals(400, exception.getErrorCode());
//        assertEquals(NO_AUTHORIZATION_ROLE, exception.getMessage());
//    }
//
//        @Test
//    @DisplayName("멤버 조회 실패로 인한 멤버 삭제 실패")
//    void deleteMember_NotFoundMember() {
//        //Given
//        String deletedStudentNum = "999";
//        when(memberRepository.findByStudentNum(deletedStudentNum)).thenReturn(Optional.empty());
//
//        //When, Then
//        CustomException exception = assertThrows(CustomException.class, () -> myService.deleteMember(
//            deletedStudentNum, currentUser));
//        assertEquals(NOT_FOUND_MEMBER, exception.getMessage());
//        assertEquals(400, exception.getErrorCode());
//        verify(memberRepository).findByStudentNum(deletedStudentNum);
//        verify(userRepository, never()).findByStudentNum(any());
//        verify(emailVerificationRepository, never()).deleteByEmail(any());
//
//    }
}
