package com.example.tomyongji;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.admin.service.AdminService;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.auth.service.UserService;
import com.example.tomyongji.receipt.dto.ClubDto;
import com.example.tomyongji.receipt.dto.TransferDto;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.mapper.StudentClubMapper;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.receipt.service.StudentClubService;
import com.example.tomyongji.validation.CustomException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
public class StudentClubServiceTest {

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
    StudentClubService studentClubService;

    private College college;
    private StudentClub convergenceSoftware;
    private StudentClub digitalContentsDesign;
    private StudentClub business;
    private ClubDto convergenceSoftwareDto;
    private ClubDto digitalContentsDesignDto;
    private ClubDto businessDto;

    // 학생회 이전 기능 테스트 사용 필드
    private User president;
    private User student1;
    private User nextPresident;
    private Receipt receipt1;
    private Receipt receipt2;
    private UserDetails currentUser;

    @BeforeEach
    void setUp() {
        college = College.builder()
            .id(1L)
            .collegeName("ICT 융합대학")
            .build();
        convergenceSoftware = StudentClub.builder()
            .id(1L)
            .studentClubName("융합소프트웨어학부 학생회")
            .Balance(1000)
            .college(college)
            .build();
        digitalContentsDesign = StudentClub.builder()
            .id(2L)
            .studentClubName("디지털콘텐츠디자인전공 학생회")
            .Balance(1000)
            .college(college)
            .build();
        business = StudentClub.builder()
            .id(3L)
            .studentClubName("경영전공 학생회")
            .Balance(1000)
            .build();
        convergenceSoftwareDto = ClubDto.builder()
            .studentClubId(convergenceSoftware.getId())
            .studentClubName(convergenceSoftware.getStudentClubName())
            .build();
        digitalContentsDesignDto = ClubDto.builder()
            .studentClubId(digitalContentsDesign.getId())
            .studentClubName(digitalContentsDesign.getStudentClubName())
            .build();
        businessDto = ClubDto.builder()
            .studentClubId(business.getId())
            .studentClubName(business.getStudentClubName())
            .build();


        president = User.builder()
                .id(1L)
                .userId("president123")
                .name("정우주")
                .studentNum("60221317")
                .collegeName("ICT 융합대학")
                .email("president@mju.ac.kr")
                .password("password")
                .role("PRESIDENT")
                .studentClub(convergenceSoftware)
                .build();

        student1 = User.builder()
                .id(2L)
                .userId("student1")
                .name("홍길동")
                .studentNum("60221111")
                .collegeName("ICT 융합대학")
                .email("student1@mju.ac.kr")
                .password("password")
                .role("STU")
                .studentClub(convergenceSoftware)
                .build();

        nextPresident = User.builder()
                .id(3L)
                .userId("nextPresident")
                .name("박진형")
                .studentNum("60221318")
                .collegeName("ICT 융합대학")
                .email("np@mju.ac.kr")
                .password("password")
                .role("STU")
                .studentClub(convergenceSoftware)
                .build();

        receipt1 = Receipt.builder()
                .id(1L)
                .deposit(5000)
                .withdrawal(0)
                .studentClub(convergenceSoftware)
                .build();

        receipt2 = Receipt.builder()
                .id(2L)
                .deposit(0)
                .withdrawal(2000)
                .studentClub(convergenceSoftware)
                .build();

        currentUser = org.springframework.security.core.userdetails.User
                .withUsername("president123")
                .password("password")
                .roles("PRESIDENT")
                .build();
    }

    @Test
    @DisplayName("모든 학생회 조회 성공")
    void getAllStudentClub_Success() {
        //Given
        List<StudentClub> studentClubList = List.of(convergenceSoftware, digitalContentsDesign, business);
        when(studentClubRepository.findAll()).thenReturn(studentClubList);
        when(studentClubMapper.toClubDto(convergenceSoftware)).thenReturn(convergenceSoftwareDto);
        when(studentClubMapper.toClubDto(digitalContentsDesign)).thenReturn(digitalContentsDesignDto);
        when(studentClubMapper.toClubDto(business)).thenReturn(businessDto);

        //When
        List<ClubDto> result = studentClubService.getAllStudentClub();
        //Then
        assertNotNull(result);
        assertEquals(result.size(), 3);
        assertEquals(result.get(0), convergenceSoftwareDto);
        assertEquals(result.get(1), digitalContentsDesignDto);
        assertEquals(result.get(2), businessDto);
        verify(studentClubRepository).findAll();
    }

    @Test
    @DisplayName("대학에 맞는 학생회 조회 성공")
    void getStudentClubById_Success() {
        //Given
        Long collegeId = college.getId();
        List<StudentClub> ictStudentClubList = List.of(convergenceSoftware, digitalContentsDesign);
        when(studentClubRepository.findAllByCollege_Id(collegeId)).thenReturn(ictStudentClubList);
        when(studentClubMapper.toClubDto(convergenceSoftware)).thenReturn(convergenceSoftwareDto);
        when(studentClubMapper.toClubDto(digitalContentsDesign)).thenReturn(digitalContentsDesignDto);
        //When
        List<ClubDto> result = studentClubService.getStudentClubById(collegeId);
        //Then
        assertNotNull(result);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0), convergenceSoftwareDto);
        assertEquals(result.get(1), digitalContentsDesignDto);
        verify(studentClubRepository).findAllByCollege_Id(collegeId);
    }

    @Test
    @DisplayName("다음 회장이 확정되지 않았을 경우 학생회 이전 성공")
    void transferStudentClub_Success() {
        //Given
        List<Receipt> receipts = List.of(receipt1, receipt2);

        when(userRepository.findByUserId("president123")).thenReturn(Optional.of(president));
        when(receiptRepository.findAllByStudentClubOrderByIdDesc(convergenceSoftware)).thenReturn(receipts);
        when(userRepository.findFirstByStudentClubAndRole(convergenceSoftware, "PRESIDENT")).thenReturn(president);
        when(userRepository.findByStudentClubAndRole(convergenceSoftware, "STU")).thenReturn(List.of(student1));

        //When
        TransferDto result = studentClubService.transferStudentClub(null, currentUser);


        //Then
        assertNotNull(result);
        assertEquals("융합소프트웨어학부 학생회", result.getStudentClubName());
        assertEquals(3000, result.getTotalDeposit());
        assertEquals(3000, result.getNetAmount());

        verify(userRepository).findByUserId("president123");
        verify(receiptRepository).findAllByStudentClubOrderByIdDesc(convergenceSoftware);

        verify(receiptRepository).deleteAll(receipts);
        verify(receiptRepository).save(any(Receipt.class));
        verify(studentClubRepository).save(convergenceSoftware);
        verify(userService).deleteUser("president123");
        verify(userService).deleteUser("student1");
    }

    @Test
    @DisplayName("다음 회장이 존재하는 경우 학생회 이전 성공")
    void transferStudentClub_WithNextPresident_Success() {
        //Given
        Receipt depositReceipt = Receipt.builder()
                .id(1L)
                .deposit(10000)
                .withdrawal(0)
                .studentClub(convergenceSoftware)
                .build();
        List<Receipt> receipts = List.of(depositReceipt);

        PresidentDto nextPresidentDto = PresidentDto.builder()
                .clubId(0L)
                .studentNum("60221318")
                .name("박진형")
                .build();

        when(userRepository.findByUserId("president123")).thenReturn(Optional.of(president));
        when(receiptRepository.findAllByStudentClubOrderByIdDesc(convergenceSoftware)).thenReturn(receipts);
        when(userRepository.findFirstByStudentClubAndRole(convergenceSoftware, "PRESIDENT")).thenReturn(president);
        when(userRepository.findByStudentClubAndRole(convergenceSoftware, "STU")).thenReturn(List.of());

        //When
        TransferDto result = studentClubService.transferStudentClub(nextPresidentDto, currentUser);


        //Then
        assertNotNull(result);
        assertEquals("융합소프트웨어학부 학생회", result.getStudentClubName());
        assertEquals(10000, result.getTotalDeposit());
        assertEquals(10000, result.getNetAmount());

        verify(userRepository).findByUserId("president123");
        verify(receiptRepository).findAllByStudentClubOrderByIdDesc(convergenceSoftware);
        verify(receiptRepository).deleteAll(receipts);
        verify(receiptRepository).save(any(Receipt.class));
        verify(studentClubRepository).save(convergenceSoftware);
        verify(userService).deleteUser("president123");
        verify(adminService).savePresident(any(PresidentDto.class));
    }

    @Test
    @DisplayName("영수증이 0개일 경우 학생회 이전 실패")
    void transferStudentClub_EmptyReceipts_Failure() {
        //Given
        List<Receipt> receipts = List.of();

        when(userRepository.findByUserId("president123")).thenReturn(Optional.of(president));
        when(receiptRepository.findAllByStudentClubOrderByIdDesc(convergenceSoftware)).thenReturn(receipts);

        //When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            studentClubService.transferStudentClub(null, currentUser);
        });

        assertEquals("이월할 영수증이 없습니다.", exception.getMessage());
        assertEquals(400, exception.getErrorCode());

        verify(userRepository).findByUserId("president123");
        verify(receiptRepository).findAllByStudentClubOrderByIdDesc(convergenceSoftware);
    }
}
