package com.example.tomyongji;

import static com.example.tomyongji.validation.ErrorMsg.EXISTING_USER;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_MEMBER;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.my.dto.MyDto;
import com.example.tomyongji.my.dto.SaveMemberDto;
import com.example.tomyongji.my.mapper.MyMapper;
import com.example.tomyongji.my.service.MyService;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.validation.CustomException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private MyMapper myMapper;

    @InjectMocks
    private MyService myService;

    private User user;
    private StudentClub studentClub;
    private MyDto myDto;
    private SaveMemberDto saveMemberDto;
    private User presidentUser;
    private Member member;

    //각 테스트 메서드 실행 전에 @Mock 과 @InjectMock 필드 초기화
    @BeforeEach
    void setUp() {

        Long userId = 1L;
        studentClub = StudentClub.builder()
            .id(3L)
            .studentClubName("융합소프트웨어학부")
            .build();

        user = User.builder()
            .id(userId)
            .userId("testUser")
            .name("test name")
            .studentNum("60000000")
            .college("ICT 융합대학")
            .email("test@example.com")
            .password("password123")
            .role("USER")
            .studentClub(studentClub)
            .build();

        myDto = MyDto.builder()
            .name("test name")
            .studentNum("60000000")
            .college("ICT 융합대학")
            .studentClubId(3L)
            .build();

        saveMemberDto = SaveMemberDto.builder()
            .presidentUserId(1L)
            .studentNum("600000")
            .name("test name")
            .build();

        presidentUser = User.builder()
            .id(1L)
            .name("test president name")
            .studentClub(StudentClub.builder().id(100L).studentClubName("융합소프트웨어학부").build())
            .build();

        member = Member.builder()
            .name("test name")
            .studentNum("600000")
            .build();

    }

    @Test
    @DisplayName("유저 정보 조회 성공")
    void getMyInfo_UserExists() {
        //Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(myMapper.toMyDto(user)).thenReturn(myDto);

        //When
        MyDto result = myService.getMyInfo(1L);

        //Then
        assertNotNull(result); //반환된 DTO가 null이 아닌지 검증
        assertEquals("test name", result.getName());
        assertEquals("60000000", result.getStudentNum());
        assertEquals("ICT 융합대학", result.getCollege());
        assertEquals(3L, result.getStudentClubId());
        //userRepository 에서 findById(1L)을 사용했는지
        verify(userRepository).findById(1L);
        //myMapper 에서 toMyDto(user)를 사용했는지
        verify(myMapper).toMyDto(user);
    }

    @Test
    @DisplayName("존재하지 않는 유저 조회 시 예외 발생")
    void getMyInfo_UserNotFound() {
        //Given
        Long invalidUserId = 999L;
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.getMyInfo(invalidUserId));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_USER, exception.getMessage());
        verify(userRepository).findById(invalidUserId);
        //userRepository.findById(userId); 에서 오류가 발생해야 하기 때문에
        //그 다음 로직인 mapper 가 작동이 되지 않은 것을 확인
        verify(myMapper, never()).toMyDto(any());
    }

    @Test
    @DisplayName("존재하지 않는 학생회 조회 시 예외 발생")
    void getMyInfo_NoFoundStudentClub() {
        //Given
        user.setStudentClub(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        //When, Then
        CustomException exception  = assertThrows(CustomException.class,
            () -> myService.getMyInfo(1L));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_STUDENT_CLUB, exception.getMessage());
        verify(userRepository).findById(1L);
        verify(myMapper, never()).toMyDto(any());
    }

    @Test
    @DisplayName("멤버 정보 조회 성공")
    void getMembers_Success() {
        //Given
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

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(memberRepository.findByStudentClub(studentClub)).thenReturn(memberList);
        when(myMapper.toMemberDto(memberList.get(0))).thenReturn(expectedDtos.get(0));
        when(myMapper.toMemberDto(memberList.get(1))).thenReturn(expectedDtos.get(1));

        //When
        List<MemberDto> result = myService.getMembers(1L);

        //Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("member1", result.get(0).getName());
        assertEquals("member2", result.get(1).getName());

        verify(userRepository).findById(1L);
        verify(memberRepository).findByStudentClub(studentClub);
        verify(myMapper, times(2)).toMemberDto(any(Member.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저로 회원 목록 조회 시 예외 발생")
    void getMembers_UserNotFound() {
        //Given
        Long invalidUserId = 999L;
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.getMembers(invalidUserId));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_USER, exception.getMessage());

        verify(userRepository).findById(invalidUserId);
        verify(memberRepository, never()).findByStudentClub(any());
        verify(myMapper, never()).toMemberDto((Member) any());
    }
    @Test
    @DisplayName("빈 회원 목록 조회")
    void getMembers_EmptyList() {
        //Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(memberRepository.findByStudentClub(studentClub)).thenReturn(Collections.emptyList());

        //When
        List<MemberDto> result = myService.getMembers(1L);

        //Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findById(1L);
        verify(memberRepository).findByStudentClub(studentClub);
        verify(myMapper, never()).toMemberDto((Member) any());
    }

    //회장의 유저 아이디를 통해 저장
    @Test
    @DisplayName("멤버 저장 성공")
    void saveMember_Success() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(presidentUser));
        when(memberRepository.existsByStudentNum(saveMemberDto.getStudentNum())).thenReturn(false);
        when(myMapper.toMemberEntity(saveMemberDto)).thenReturn(member);

        //When
        myService.saveMember(saveMemberDto);

        //Then
        verify(userRepository).findById(1L);
        verify(memberRepository).existsByStudentNum("600000");
        verify(myMapper).toMemberEntity(saveMemberDto);
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("존재하지 않는 유저로 멤버 저장 실패")
    void saveMember_UserNotFound() {
        //Given
        long invalidUserId = 999L;
        saveMemberDto.setPresidentUserId(invalidUserId);

        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.saveMember(saveMemberDto));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_USER, exception.getMessage());
        verify(userRepository).findById(invalidUserId);
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 존재하는 학번으로 인한 멤버 저장 실패")
    void saveMember_ExistingUser() {
        //Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(memberRepository.existsByStudentNum("600000")).thenReturn(true);

        //WHen, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.saveMember(saveMemberDto));
        assertEquals(400, exception.getErrorCode());
        assertEquals(EXISTING_USER, exception.getMessage());
        verify(userRepository).findById(1L);
        verify(memberRepository).existsByStudentNum("600000");
        verify(myMapper, never()).toMemberEntity((SaveMemberDto) any());
    }

    @Test
    @DisplayName("존재하지 않는 학생회로 인한 멤버 저장 실패")
    void saveMember_NotFoundStudentClub() {
        //Given
        presidentUser.setStudentClub(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(presidentUser));
        when(memberRepository.existsByStudentNum("600000")).thenReturn(false);

        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> myService.saveMember(saveMemberDto));
        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_STUDENT_CLUB, exception.getMessage());
        verify(userRepository).findById(1L);
        verify(memberRepository).existsByStudentNum("600000");
        verify(myMapper, never()).toMemberEntity((SaveMemberDto) any());
    }

    @Test
    @DisplayName("멤버 삭제 성공")
    void deleteMember_Success() {
        //Given
        Long deleteId = 1L;
        MemberDto memberDto = MemberDto.builder()
            .memberId(deleteId)
            .name("test name")
            .studentNum("600000")
            .build();

        when(memberRepository.findById(deleteId)).thenReturn(Optional.of(member));
        when(userRepository.findByStudentNum("600000")).thenReturn(user);
        when(myMapper.toMemberDto(member)).thenReturn(memberDto);

        //When
        MemberDto result = myService.deleteMember(deleteId);

        //Then
        assertNotNull(result);
        assertEquals("test name", result.getName());
        verify(memberRepository).findById(deleteId);
        verify(userRepository).findByStudentNum("600000");
        verify(emailVerificationRepository).deleteByEmail("test@example.com");
        verify(userRepository).delete(user);
        verify(memberRepository).deleteById(deleteId);
    }

    @Test
    @DisplayName("멤버 조회 실패로 인한 멤버 삭제 실패")
    void deleteMember_NotFoundMember() {
        //Given
        Long deleteId = 1L;
        when(memberRepository.findById(deleteId)).thenReturn(Optional.empty());

        //When, Then
        CustomException exception = assertThrows(CustomException.class, () -> myService.deleteMember(deleteId));
        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_MEMBER, exception.getMessage());
        verify(memberRepository).findById(deleteId);
        verify(userRepository, never()).findByStudentNum(any());
        verify(emailVerificationRepository, never()).deleteByEmail(any());

    }
}
