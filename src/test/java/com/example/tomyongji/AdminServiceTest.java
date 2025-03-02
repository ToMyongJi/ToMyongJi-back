package com.example.tomyongji;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.mapper.AdminMapper;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.admin.service.AdminService;
import com.example.tomyongji.auth.dto.LoginRequestDto;
import com.example.tomyongji.auth.dto.UserRequestDto;
import com.example.tomyongji.auth.entity.ClubVerification;
import com.example.tomyongji.auth.entity.EmailVerification;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.jwt.JwtProvider;
import com.example.tomyongji.auth.jwt.JwtToken;
import com.example.tomyongji.auth.mapper.UserMapper;
import com.example.tomyongji.auth.repository.ClubVerificationRepository;
import com.example.tomyongji.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.auth.service.UserServiceImpl;
import com.example.tomyongji.my.dto.AdminSaveMemberDto;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.CollegeRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.tomyongji.validation.ErrorMsg.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @InjectMocks
    AdminService adminService;
    @Mock
    MemberRepository memberRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    PresidentRepository presidentRepository;
    @Mock
    StudentClubRepository studentClubRepository;
    @Mock
    ClubVerificationRepository clubVerificationRepository;
    @Mock
    AdminMapper adminMapper;
    @Mock
    PasswordEncoder encoder;
    User user;
    College college;
    StudentClub studentClub;
    President president;
    @BeforeEach
    void beforeEach(){
        president = President.builder()
                .name("투명지")
                .studentNum("60222024")
                .build();
        college =College.builder()
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
    @Test
    @DisplayName("정상적인 학생회장 조회 테스트")
    void signUpTest(){
        //given
        PresidentDto presidentDto = PresidentDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .clubId(1L)
                .build();

        when(studentClubRepository.findById(studentClub.getId())).thenReturn(Optional.of(studentClub));
        when(adminMapper.toPresidentDto(president)).thenReturn(presidentDto);
        //when
        PresidentDto response = adminService.getPresident(1L);
        //then
        assertThat(presidentDto.getStudentNum()).isEqualTo(response.getStudentNum());
        assertThat(presidentDto.getClubId()).isEqualTo(response.getClubId());
    }
    @Test
    @DisplayName("존재하지 않을 학생회를 조회시 예외 발생")
    void getNotFoundStudentClub(){
        //given
        PresidentDto presidentDto = PresidentDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .clubId(2L)
                .build();

        when(studentClubRepository.findById(presidentDto.getClubId())).thenReturn(Optional.empty());
        //when
        CustomException exception = assertThrows(CustomException.class,()->adminService.getPresident(presidentDto.getClubId()));
        //then
        assertEquals(400,exception.getErrorCode());
        assertEquals(NOT_FOUND_STUDENT_CLUB,exception.getMessage());
        verify(studentClubRepository).findById(presidentDto.getClubId());
        verify(adminMapper, never()).toPresidentDto(any());
    }
    @Test
    @DisplayName("학생회장 저장 테스트")
    void savePresident(){
        //given
        PresidentDto presidentDto = PresidentDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .clubId(1L)
                .build();

        when(studentClubRepository.findById(presidentDto.getClubId())).thenReturn(Optional.of(studentClub));
        when(adminMapper.toPresidentEntity(presidentDto)).thenReturn(president);
        when(adminMapper.toPresidentDto(president)).thenReturn(presidentDto);
        when(presidentRepository.existsByStudentNum(presidentDto.getStudentNum())).thenReturn(false);
        doAnswer(invocation -> {
            President savedPresident = invocation.getArgument(0);
            return savedPresident;
        }).when(presidentRepository).save(any(President.class));
        doAnswer(invocation -> {
            StudentClub savedStudentClub = invocation.getArgument(0);
            return savedStudentClub;
        }).when(studentClubRepository).save(any(StudentClub.class));
        //when
        PresidentDto response = adminService.savePresident(presidentDto);
        //then
        assertThat(presidentDto.getStudentNum()).isEqualTo(response.getStudentNum());
        assertThat(presidentDto.getClubId()).isEqualTo(response.getClubId());
    }
    @Test
    @DisplayName("이미 같은학번의 회장이 존재할시 예외 발생")
    void existPresident(){
        //given
        PresidentDto presidentDto = PresidentDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .clubId(1L)
                .build();

        when(presidentRepository.existsByStudentNum(presidentDto.getStudentNum())).thenReturn(true);
        //when
        CustomException exception = assertThrows(CustomException.class,()->adminService.savePresident(presidentDto));
        //then
        assertEquals(400,exception.getErrorCode());
        assertEquals(EXISTING_USER,exception.getMessage());
        verify(presidentRepository).existsByStudentNum(presidentDto.getStudentNum());
        verify(adminMapper, never()).toPresidentEntity(any());
    }
    @Test
    @DisplayName("존재하지 않는 학생회를 저장시 예외 발생")
    void savedNotFoundStudentClub(){
        //given
        PresidentDto presidentDto = PresidentDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .clubId(1L)
                .build();
        when(presidentRepository.existsByStudentNum(presidentDto.getStudentNum())).thenReturn(false);
        when(adminMapper.toPresidentEntity(presidentDto)).thenReturn(president);
        doAnswer(invocation -> {
            President savedPresident = invocation.getArgument(0);
            return savedPresident;
        }).when(presidentRepository).save(any(President.class));
        when(studentClubRepository.findById(presidentDto.getClubId())).thenReturn(Optional.empty());
        //when
        CustomException exception = assertThrows(CustomException.class,()->adminService.savePresident(presidentDto));
        //then
        assertEquals(400,exception.getErrorCode());
        assertEquals(NOT_FOUND_STUDENT_CLUB,exception.getMessage());
        verify(studentClubRepository).findById(presidentDto.getClubId());
        verify(studentClubRepository,never()).save(any());
    }
    @Test
    @DisplayName("학생회장 수정 테스트")
    void updatePresident(){
        //given
        PresidentDto newPresidentDto = PresidentDto.builder()
                .name("투명지2025")
                .studentNum("60222025")
                .clubId(1L)
                .build();
        President newPresident = President.builder()
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
        Member member =Member.builder()
                        .name("투명지")
                        .studentNum("60222024")
                        .studentClub(studentClub)
                        .build();

        when(studentClubRepository.findById(newPresidentDto.getClubId())).thenReturn(Optional.of(studentClub));
        when(userRepository.findFirstByStudentClubAndRole(studentClub,"PRESIDENT")).thenReturn(user);
        doAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        }).when(userRepository).save(any(User.class));

        when(adminMapper.toMemberEntity(user)).thenReturn(member);
        doAnswer(invocation -> {
            Member savedMember = invocation.getArgument(0);
            return savedMember;
        }).when(memberRepository).save(any(Member.class));

        when(presidentRepository.findByStudentNum(user.getStudentNum())).thenReturn(president);
        doAnswer(invocation -> {
            StudentClub savedStudentClub = invocation.getArgument(0);
            return savedStudentClub;
        }).when(studentClubRepository).save(any(StudentClub.class));
        doAnswer(invocation -> {
            President savedPresident = invocation.getArgument(0);
            return savedPresident;
        }).when(presidentRepository).save(any(President.class));
        when(adminMapper.toPresidentDto(newPresident)).thenReturn(newPresidentDto);
        //when
        PresidentDto response = adminService.updatePresident(newPresidentDto);
        //then
        assertThat(newPresident.getStudentNum()).isEqualTo(response.getStudentNum());
        assertThat(newPresident.getName()).isEqualTo(response.getName());
        assertThat(newPresidentDto.getClubId()).isEqualTo(response.getClubId());
    }
    @Test
    @DisplayName("존재하지 않는 학생회의 학생회장 수정시 오류 발생")
    void updateNotFoundStudentClub(){
        //given
        PresidentDto newPresidentDto = PresidentDto.builder()
                .name("투명지2025")
                .studentNum("60222025")
                .clubId(1L)
                .build();
        President newPresident = President.builder()
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
        Member member =Member.builder()
                .name("투명지")
                .studentNum("60222024")
                .studentClub(studentClub)
                .build();
        when(studentClubRepository.findById(newPresidentDto.getClubId())).thenReturn(Optional.empty());
        //when
        CustomException exception = assertThrows(CustomException.class,()->adminService.updatePresident(newPresidentDto));
        //then
        assertEquals(400,exception.getErrorCode());
        assertEquals(NOT_FOUND_STUDENT_CLUB,exception.getMessage());
        verify(studentClubRepository).findById(newPresidentDto.getClubId());
        verify(userRepository,never()).findFirstByStudentClubAndRole(any(),any());
    }
    @Test
    @DisplayName("소속 부원 조회 테스트")
    void getMembers(){
        //given
        Member member =Member.builder()
                .name("투명지")
                .studentNum("60222024")
                .studentClub(studentClub)
                .build();
        MemberDto memberDto = MemberDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .build();
        List<Member> list = new ArrayList<>();
        list.add(member);

        when(studentClubRepository.findById(studentClub.getId())).thenReturn(Optional.of(studentClub));
        when(memberRepository.findByStudentClub(studentClub)).thenReturn(list);
        when(adminMapper.toMemberDto(member)).thenReturn(memberDto);

        //when
        List<MemberDto> response = adminService.getMembers(studentClub.getId());
        //then
        assertThat(memberDto.getName()).isEqualTo(response.get(0).getName());
        assertThat(memberDto.getStudentNum()).isEqualTo(response.get(0).getStudentNum());
    }
    @Test
    @DisplayName("존재하지 않는 학생회의 멤버에 대해 조회할 시 오류 발생")
    void getMembersOfNotFoundStudentClub(){
        //given
        Member member =Member.builder()
                .name("투명지")
                .studentNum("60222024")
                .studentClub(studentClub)
                .build();
        MemberDto memberDto = MemberDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .build();
        List<Member> list = new ArrayList<>();
        list.add(member);

        when(studentClubRepository.findById(studentClub.getId())).thenReturn(Optional.empty());
        //when
        CustomException exception = assertThrows(CustomException.class,()->adminService.getMembers(studentClub.getId()));
        //then
        assertEquals(400,exception.getErrorCode());
        assertEquals(NOT_FOUND_STUDENT_CLUB,exception.getMessage());
        verify(studentClubRepository).findById(studentClub.getId());
        verify(memberRepository,never()).findByStudentClub(any());
    }
    @Test
    @DisplayName("소속 부원 저장 테스트")
    void saveMember(){
        //given
        Member member =Member.builder()
                .name("투명지")
                .studentNum("60222024")
                .studentClub(studentClub)
                .build();
        AdminSaveMemberDto savedMemberDto = AdminSaveMemberDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .clubId(1L)
                .build();
        MemberDto memberDto = MemberDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .build();
        List<Member> list = new ArrayList<>();
        list.add(member);

        when(memberRepository.existsByStudentNum(savedMemberDto.getStudentNum())).thenReturn(false);
        when(studentClubRepository.findById(savedMemberDto.getClubId())).thenReturn(Optional.of(studentClub));
        when(adminMapper.toMemberEntity(savedMemberDto)).thenReturn(member);
        doAnswer(invocation -> {
            Member savedMember = invocation.getArgument(0);
            return savedMember;
        }).when(memberRepository).save(any(Member.class));
        when(adminMapper.toMemberDto(member)).thenReturn(memberDto);

        //when
        MemberDto response = adminService.saveMember(savedMemberDto);
        //then
        assertThat(savedMemberDto.getName()).isEqualTo(response.getName());
        assertThat(savedMemberDto.getStudentNum()).isEqualTo(response.getStudentNum());
    }
    @Test
    @DisplayName("존재하는 소속 부원에 대해 저장할 시 오류 발생")
    void saveExistingMember(){
        //given
        AdminSaveMemberDto savedMemberDto = AdminSaveMemberDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .clubId(1L)
                .build();

        when(memberRepository.existsByStudentNum(savedMemberDto.getStudentNum())).thenReturn(true);

        //when
        CustomException exception = assertThrows(CustomException.class,()->adminService.saveMember(savedMemberDto));
        //then
        assertEquals(400,exception.getErrorCode());
        assertEquals(EXISTING_USER,exception.getMessage());
        verify(memberRepository).existsByStudentNum(savedMemberDto.getStudentNum());
        verify(studentClubRepository,never()).findById(any());
    }
    @Test
    @DisplayName("존재하지 않는 학생회에 대해 부원을 저장할 시 오류 발생")
    void saveMemberOfNotFoundStudentClub(){
        //given
        AdminSaveMemberDto savedMemberDto = AdminSaveMemberDto.builder()
                .name("투명지")
                .studentNum("60222024")
                .clubId(1L)
                .build();

        when(memberRepository.existsByStudentNum(savedMemberDto.getStudentNum())).thenReturn(false);
        when(studentClubRepository.findById(savedMemberDto.getClubId())).thenReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,()->adminService.saveMember(savedMemberDto));
        //then
        assertEquals(400,exception.getErrorCode());
        assertEquals(NOT_FOUND_STUDENT_CLUB,exception.getMessage());
        verify(studentClubRepository).findById(savedMemberDto.getClubId());
        verify(adminMapper,never()).toMemberEntity(savedMemberDto);
    }
    @Test
    @DisplayName("소속 부원 삭제 테스트")
    void deleteMember(){
        //given
        long memberId = 1L;
        Member member =Member.builder()
                .id(1L)
                .name("투명지")
                .studentNum("60222024")
                .studentClub(studentClub)
                .build();
        MemberDto memberDto = MemberDto.builder()
                .memberId(1L)
                .name("투명지")
                .studentNum("60222024")
                .build();
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(adminMapper.toMemberDto(member)).thenReturn(memberDto);
        when(clubVerificationRepository.findByStudentNum(member.getStudentNum())).thenReturn(
            Collections.emptyList());
        when(userRepository.findByStudentNum(member.getStudentNum())).thenReturn(null);

        //when
        MemberDto response = adminService.deleteMember(memberId);
        //then
        assertThat(memberDto.getMemberId()).isEqualTo(response.getMemberId());
        assertThat(memberDto.getStudentNum()).isEqualTo(response.getStudentNum());
    }
    @Test
    @DisplayName("존재하지 않는 소속 부원 삭제시 오류 발생")
    void deleteNotFoundMember(){
        //given
        long memberId = 1L;
        Member member =Member.builder()
                .id(1L)
                .name("투명지")
                .studentNum("60222024")
                .studentClub(studentClub)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());
        //when
        CustomException exception = assertThrows(CustomException.class,()->adminService.deleteMember(memberId));
        //then
        assertEquals(400,exception.getErrorCode());
        assertEquals(NOT_FOUND_MEMBER,exception.getMessage());
        verify(memberRepository).findById(memberId);
        verify(adminMapper,never()).toMemberDto(member);
    }
}
