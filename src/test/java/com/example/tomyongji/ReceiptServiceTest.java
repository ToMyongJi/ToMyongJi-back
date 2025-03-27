package com.example.tomyongji;

import static com.example.tomyongji.validation.ErrorMsg.DUPLICATED_FLOW;
import static com.example.tomyongji.validation.ErrorMsg.EMPTY_CONTENT;
import static com.example.tomyongji.validation.ErrorMsg.MISMATCHED_USER;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_RECEIPT;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_BELONGING;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.auth.service.CustomUserDetails;
import com.example.tomyongji.receipt.dto.ReceiptByStudentClubDto;
import com.example.tomyongji.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.mapper.ReceiptMapper;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.receipt.service.ReceiptService;
import com.example.tomyongji.validation.CustomException;
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
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
public class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;
    @Mock
    private StudentClubRepository studentClubRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private ReceiptMapper receiptMapper;
    @InjectMocks
    private ReceiptService receiptService;

    private StudentClub studentClub;
    private StudentClub anotherStudentClub;
    private User user;
    private User anotherUser;
    private ReceiptCreateDto receiptCreateDto;
    private Receipt receipt;
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

        receiptCreateDto = ReceiptCreateDto.builder()
            .userId("testUser")
            .content("영수증 테스트")
            .deposit(1000)
            .build();

        receipt = Receipt.builder()
            .id(1L) // ID 설정
            .content("영수증 테스트")
            .deposit(1000)
            .studentClub(studentClub)
            .build();

        currentUser = (UserDetails) new org.springframework.security.core.userdetails.User("testUser","password123", Collections.emptyList());
        anotherCurrentUser = (UserDetails) new org.springframework.security.core.userdetails.User("anotherUser","password123", Collections.emptyList());

    }

    @Test
    @DisplayName("영수증 생성 성공")
    void createReceipt_Success() {
        //Given
        ReceiptDto receiptDto = ReceiptDto.builder()
                .receiptId(receipt.getId())
                .content(receipt.getContent())
                .deposit(receipt.getDeposit())
                .build();
        when(userRepository.findByUserId(receiptCreateDto.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(receiptMapper.toReceiptEntity(receiptCreateDto)).thenReturn(receipt);
        when(receiptMapper.toReceiptDto(receipt)).thenReturn(receiptDto);

        //When
        ReceiptDto result = receiptService.createReceipt(receiptCreateDto, currentUser);

        //Then
        assertNotNull(result);
        assertEquals("영수증 테스트", result.getContent());
        assertEquals(1000, result.getDeposit());
        verify(receiptRepository).save(receipt);
        verify(studentClubRepository).save(studentClub);
    }

    @Test
    @DisplayName("유저 조회 실패로 인한 영수증 생성 실패")
    void createReceipt_NotFoundUser() {
        //Given
        ReceiptCreateDto receiptCreateDtoWithWrongUserId = ReceiptCreateDto.builder()
            .userId("wrongtUser")
            .content("영수증 테스트")
            .deposit(1000)
            .build();
        when(userRepository.findByUserId(receiptCreateDtoWithWrongUserId.getUserId())).thenReturn(Optional.empty());

        //When, Then
        CustomException exception = assertThrows(CustomException.class, () -> receiptService.createReceipt(
            receiptCreateDtoWithWrongUserId, currentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_USER, exception.getMessage());
        verify(userRepository).findByUserId(receiptCreateDtoWithWrongUserId.getUserId());
    }

    @Test
    @DisplayName("타소속의 접근으로 인한 특정 영수증 생성 실패")
    void savaReceipt_NoAuthorizationBelonging() {
        //Given
        when(userRepository.findByUserId(receiptCreateDto.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(anotherUser.getUserId())).thenReturn(Optional.of(anotherUser));
        //When
        CustomException exception = assertThrows(CustomException.class,
            () -> receiptService.createReceipt(receiptCreateDto, anotherCurrentUser));
        //Then
        assertEquals(400, exception.getErrorCode());
        assertEquals(NO_AUTHORIZATION_BELONGING, exception.getMessage());
    }

    @Test
    @DisplayName("입출금 모두 작성으로 인한 영수증 생성 실패")
    void createReceipt_DuplicatedFlow() {
        //Given
        ReceiptCreateDto receiptCreateDtoWithDuplicatedFlow = ReceiptCreateDto.builder()
            .userId(user.getUserId())
            .content("영수증 테스트")
            .deposit(1000)
            .withdrawal(1000)
            .build();
        when(userRepository.findByUserId(receiptCreateDto.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        //When, Then
        CustomException exception = assertThrows(CustomException.class, () -> receiptService.createReceipt(
            receiptCreateDtoWithDuplicatedFlow, currentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(DUPLICATED_FLOW, exception.getMessage());
        verify(userRepository, times(2)).findByUserId(receiptCreateDtoWithDuplicatedFlow.getUserId());

    }

    @Test
    @DisplayName("입출금 모두 공백으로 인한 영수증 생성 실패")
    void createReceipt_EmptyFlow() {
        //Given
        ReceiptCreateDto receiptCreateDtoWithEmptyFlow = ReceiptCreateDto.builder()
            .userId("testUser")
            .content("영수증 테스트")
            .deposit(0)
            .withdrawal(0)
            .build();
        when(userRepository.findByUserId(receiptCreateDtoWithEmptyFlow.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        //When, Then
        CustomException exception = assertThrows(CustomException.class, () -> receiptService.createReceipt(
            receiptCreateDtoWithEmptyFlow, currentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(DUPLICATED_FLOW, exception.getMessage());
        verify(userRepository, times(2)).findByUserId(receiptCreateDtoWithEmptyFlow.getUserId());
    }

    @Test
    @DisplayName("영수증 내용 공백으로 인한 영수증 생성 실패")
    void createReceipt_EmptyContent() {
        //Given
        ReceiptCreateDto receiptCreateDtoWithEmptyContent = ReceiptCreateDto.builder()
            .userId("testUser")
            .deposit(1000)
            .build();
        when(userRepository.findByUserId(receiptCreateDtoWithEmptyContent.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        //When, Then
        CustomException exception = assertThrows(CustomException.class, () -> receiptService.createReceipt(
            receiptCreateDtoWithEmptyContent, currentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(EMPTY_CONTENT, exception.getMessage());
        verify(userRepository, times(2)).findByUserId(receiptCreateDtoWithEmptyContent.getUserId());
    }
    @Test
    @DisplayName("모든 영수증 불러오기 성공")
    void getAllReceipts_Success() {
        //Given
        Receipt receipt1 = Receipt.builder()
            .id(2L)
            .content("영수증 테스트1")
            .deposit(2000)
            .studentClub(studentClub)
            .build();
        Receipt receipt2 = Receipt.builder()
            .id(3L)
            .content("영수증 테스트2")
            .deposit(3000)
            .studentClub(studentClub)
            .build();

        List<Receipt> receiptList = List.of(receipt1, receipt2);

        ReceiptDto receiptDto1 = ReceiptDto.builder()
            .receiptId(2L)
            .content("영수증 테스트1")
            .deposit(2000)
            .build();
        ReceiptDto receiptDto2 = ReceiptDto.builder()
            .receiptId(3L)
            .content("영수증 테스트2")
            .deposit(3000)
            .build();

        when(receiptRepository.findAll()).thenReturn(receiptList);
        when(receiptMapper.toReceiptDto(receipt1)).thenReturn(receiptDto1);
        when(receiptMapper.toReceiptDto(receipt2)).thenReturn(receiptDto2);

        //When
        List<ReceiptDto> result = receiptService.getAllReceipts();

        //Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(receiptDto1, result.get(0));
        assertEquals(receiptDto2, result.get(1));
        verify(receiptRepository).findAll();
    }

    @Test
    @DisplayName("특정 학생회 영수증 조회 성공")
    void getReceiptsByClub_Success() {
        //Given
        String userId = user.getUserId();
        Receipt receipt1 = Receipt.builder()
            .id(2L)
            .content("영수증 테스트1")
            .deposit(2000)
            .studentClub(studentClub)
            .build();
        Receipt receipt2 = Receipt.builder()
            .id(3L)
            .content("영수증 테스트2")
            .deposit(3000)
            .studentClub(studentClub)
            .build();
        List<Receipt> receiptList = List.of(receipt1, receipt2);
        ReceiptDto receiptDto1 = ReceiptDto.builder()
            .receiptId(2L)
            .content("영수증 테스트1")
            .deposit(2000)
            .build();
        ReceiptDto receiptDto2 = ReceiptDto.builder()
            .receiptId(3L)
            .content("영수증 테스트2")
            .deposit(3000)
            .build();
        List<ReceiptDto> receiptDtoList = List.of(receiptDto1, receiptDto2);

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(
            Optional.of(user));
        when(receiptRepository.findAllByStudentClub(studentClub)).thenReturn(receiptList);
        when(receiptMapper.toReceiptDto(receipt1)).thenReturn(receiptDto1);
        when(receiptMapper.toReceiptDto(receipt2)).thenReturn(receiptDto2);
        //When
        ReceiptByStudentClubDto result = receiptService.getReceiptsByClub(userId, currentUser);
        //Then
        assertNotNull(result);
        assertEquals(result.getReceiptList().get(0), receiptDto1);
        assertEquals(result.getReceiptList().get(1), receiptDto2);
        verify(receiptRepository).findAllByStudentClub(studentClub);
    }

    @Test
    @DisplayName("유저 조회 실패로 인한 특정 학생회 영수증 조회 실패")
    void getReceiptsByClub_NotFoundStudentClub() {
        //Given
        String wrongUserId= "wrongUserId";

        when(userRepository.findByUserId(wrongUserId)).thenReturn(Optional.empty());
        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> receiptService.getReceiptsByClub(wrongUserId, currentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_USER, exception.getMessage());
        verify(userRepository).findByUserId(wrongUserId);
    }

    @Test
    @DisplayName("타소속 접근으로 인한 특정 학생회 영수증 조회 실패")
    void getReceiptsByClub_NoAuthorizationBelonging() {
        //Given
        String userId = user.getUserId();

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(anotherUser.getUserId())).thenReturn(Optional.of(anotherUser));
        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> receiptService.getReceiptsByClub(userId, anotherCurrentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NO_AUTHORIZATION_BELONGING, exception.getMessage());
        verify(userRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("특정 학생회 영수증 조회 학생용 성공")
    void getReceiptsByClubForStudent_Success() {
        //Given
        Long clubId = studentClub.getId();
        Receipt receipt1 = Receipt.builder()
            .id(2L)
            .content("영수증 테스트1")
            .deposit(2000)
            .studentClub(studentClub)
            .build();
        Receipt receipt2 = Receipt.builder()
            .id(3L)
            .content("영수증 테스트2")
            .deposit(3000)
            .studentClub(studentClub)
            .build();
        List<Receipt> receiptList = List.of(receipt1, receipt2);
        ReceiptDto receiptDto1 = ReceiptDto.builder()
            .receiptId(2L)
            .content("영수증 테스트1")
            .deposit(2000)
            .build();
        ReceiptDto receiptDto2 = ReceiptDto.builder()
            .receiptId(3L)
            .content("영수증 테스트2")
            .deposit(3000)
            .build();
        List<ReceiptDto> receiptDtoList = List.of(receiptDto1, receiptDto2);

        when(studentClubRepository.findById(clubId)).thenReturn(Optional.of(studentClub));
        when(receiptRepository.findAllByStudentClub(studentClub)).thenReturn(receiptList);
        when(receiptMapper.toReceiptDto(receipt1)).thenReturn(receiptDto1);
        when(receiptMapper.toReceiptDto(receipt2)).thenReturn(receiptDto2);
        //When
        List<ReceiptDto> result = receiptService.getReceiptsByClubForStudent(clubId);
        //Then
        assertNotNull(result);
        assertEquals(result.get(0), receiptDto1);
        assertEquals(result.get(1), receiptDto2);
        verify(studentClubRepository).findById(clubId);
        verify(receiptRepository).findAllByStudentClub(studentClub);
    }

    @Test
    @DisplayName("학생회 조회 실패로 인한 특정 학생회 영수증 조회 학생용 실패")
    void getReceiptsByClubForStudent_NotFoundStudentClub() {
        //Given
        Long clubId = 999L;

        when(studentClubRepository.findById(clubId)).thenReturn(Optional.empty());
        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> receiptService.getReceiptsByClubForStudent(clubId));
        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_STUDENT_CLUB, exception.getMessage());
        verify(studentClubRepository).findById(clubId);
    }

    @Test
    @DisplayName("특정 영수증 조회 성공")
    void getReceiptById_Success() {
        //Given
        Long receiptId = receipt.getId();
        ReceiptDto receiptDto = ReceiptDto.builder()
                .receiptId(receipt.getId())
                    .date(receipt.getDate())
                        .content(receipt.getContent())
                            .deposit(receipt.getDeposit())
                                .build();

        when(receiptRepository.findById(receiptId)).thenReturn(Optional.of(receipt));
        when(receiptMapper.toReceiptDto(receipt)).thenReturn(receiptDto);
        //When
        ReceiptDto result = receiptService.getReceiptById(receiptId);
        //Then
        assertNotNull(result);
        assertEquals(result, receiptDto);
        verify(receiptRepository).findById(receiptId);
        verify(receiptMapper).toReceiptDto(receipt);
    }
    @Test
    @DisplayName("학생회 조회 실패로 인한 특정 영수증 조회 실패")
    void getReceiptById_NotFoundReceipt() {
        //Given
        Long wrongReceiptId = 999L;

        when(receiptRepository.findById(wrongReceiptId)).thenReturn(Optional.empty());
        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> receiptService.getReceiptById(wrongReceiptId));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_RECEIPT, exception.getMessage());
        verify(receiptRepository).findById(wrongReceiptId);
    }

    @Test
    @DisplayName("특정 영수증 삭제 성공")
    void deleteReceipt_Success() {
        //Given
        Long receiptId = receipt.getId();
        ReceiptDto receiptDto = ReceiptDto.builder()
            .receiptId(receipt.getId())
            .date(receipt.getDate())
            .content(receipt.getContent())
            .deposit(receipt.getDeposit())
            .build();

        when(receiptRepository.findById(receiptId)).thenReturn(Optional.of(receipt));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(receiptMapper.toReceiptDto(receipt)).thenReturn(receiptDto);
        //When
        ReceiptDto result = receiptService.deleteReceipt(receiptId, currentUser);
        //Then
        assertNotNull(result);
        assertEquals(result, receiptDto);
        assertEquals(user.getUserId(), currentUser.getUsername());
        verify(receiptRepository).findById(receiptId);
        verify(receiptRepository).delete(receipt);
        verify(studentClubRepository).save(studentClub);
        verify(receiptMapper).toReceiptDto(receipt);
    }

    @Test
    @DisplayName("영수증 조회 실패로 인한 특정 영수증 삭제 실패")
    void deleteReceipt_NotFoundReceipt() {
        //Given
        Long wrongReceiptId = 999L;

        when(receiptRepository.findById(wrongReceiptId)).thenReturn(Optional.empty());
        //When
        CustomException exception = assertThrows(CustomException.class,
            () -> receiptService.deleteReceipt(wrongReceiptId, currentUser));
        //Then
        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_RECEIPT, exception.getMessage());
        verify(receiptRepository).findById(wrongReceiptId);
    }

    @Test
    @DisplayName("타소속의 접근으로 인한 특정 영수증 삭제 실패")
    void deleteReceipt_NoAuthorizationBelonging() {
        //Given
        Long receiptId = receipt.getId();

        when(receiptRepository.findById(receiptId)).thenReturn(Optional.of(receipt));
        when(userRepository.findByUserId(anotherCurrentUser.getUsername())).thenReturn(Optional.of(anotherUser));
        //When
        CustomException exception = assertThrows(CustomException.class,
            () -> receiptService.deleteReceipt(receiptId, anotherCurrentUser));
        //Then
        assertEquals(400, exception.getErrorCode());
        assertEquals(NO_AUTHORIZATION_BELONGING, exception.getMessage());
        verify(receiptRepository).findById(receiptId);
    }

    @Test
    @DisplayName("특정 영수증 내역 수정 성공")
    void updateReceipt_SuccessForUpdatingContent() {
        //Given
        Long existingId = receipt.getId();
        ReceiptDto updateDto = ReceiptDto.builder()
            .receiptId(receipt.getId())
            .date(receipt.getDate())
            .content("수정된 내용")
            .deposit(receipt.getDeposit())
            .build();

        when(receiptRepository.findById(existingId)).thenReturn(Optional.of(receipt));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(receiptMapper.toReceiptDto(receipt)).thenReturn(updateDto);
        //When
        ReceiptDto result = receiptService.updateReceipt(updateDto, currentUser);
        //Then
        assertNotNull(result);
        assertEquals(result.getContent(), "수정된 내용");
        assertEquals(result.getDeposit(), 1000);
        verify(receiptRepository).findById(existingId);
        verify(studentClubRepository).save(studentClub);
        verify(receiptRepository).save(receipt);
    }
    @Test
    @DisplayName("특정 영수증 금액 수정 성공")
    void updateReceipt_SuccessForUpdatingFlow() {
        //Given
        Long existingId = receipt.getId();
        ReceiptDto updateDto = ReceiptDto.builder()
            .receiptId(receipt.getId())
            .date(receipt.getDate())
            .content(receipt.getContent())
            .deposit(4500)
            .build();

        when(receiptRepository.findById(existingId)).thenReturn(Optional.of(receipt));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        when(receiptMapper.toReceiptDto(receipt)).thenReturn(updateDto);
        //When
        ReceiptDto result = receiptService.updateReceipt(updateDto, currentUser);
        //Then
        assertNotNull(result);
        assertEquals(result.getContent(), receipt.getContent());
        assertEquals(result.getDeposit(), 4500);
        verify(receiptRepository).findById(existingId);
        verify(studentClubRepository).save(studentClub);
        verify(receiptRepository).save(receipt);
    }

    @Test
    @DisplayName("영수증 조회 실패로 인한 영수증 수정 실패")
    void updateReceipt_NotFoundReceipt() {
        Long wrongReceiptId = 999L;
        ReceiptDto receiptDtoWithWrongId = ReceiptDto.builder()
                .receiptId(wrongReceiptId)
                    .content("테스트")
                        .deposit(1000)
                            .build();
        when(receiptRepository.findById(wrongReceiptId)).thenReturn(Optional.empty());
        //When
        CustomException exception = assertThrows(CustomException.class,
            () -> receiptService.updateReceipt(receiptDtoWithWrongId, currentUser));
        //Then
        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_RECEIPT, exception.getMessage());
        verify(receiptRepository).findById(wrongReceiptId);
    }

    @Test
    @DisplayName("타소속의 접근으로 인한 특정 영수증 생성 실패")
    void updateReceipt_NoAuthorizationBelonging() {
        //Given
        Long existingId = receipt.getId();
        ReceiptDto updateDto = ReceiptDto.builder()
            .receiptId(receipt.getId())
            .date(receipt.getDate())
            .content("수정된 내용")
            .deposit(receipt.getDeposit())
            .build();
        when(receiptRepository.findById(existingId)).thenReturn(Optional.of(receipt));
        when(userRepository.findByUserId(anotherCurrentUser.getUsername())).thenReturn(Optional.of(anotherUser));
        //When
        CustomException exception = assertThrows(CustomException.class,
            () -> receiptService.updateReceipt(updateDto, anotherCurrentUser));
        //Then
        assertEquals(400, exception.getErrorCode());
        assertEquals(NO_AUTHORIZATION_BELONGING, exception.getMessage());
        verify(receiptRepository).findById(existingId);
    }

    @Test
    @DisplayName("입출금 모두 작성으로 인한 영수증 생성 실패")
    void updateReceipt_DuplicatedFlow() {
        //Given
        Long existingId = receipt.getId();
        ReceiptDto receiptDtoWithDuplicatedFlow = ReceiptDto.builder()
            .receiptId(receipt.getId())
            .content(receipt.getContent())
            .deposit(1000)
            .withdrawal(1000)
            .build();
        when(receiptRepository.findById(existingId)).thenReturn(Optional.of(receipt));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        //When, Then
        CustomException exception = assertThrows(CustomException.class, () -> receiptService.updateReceipt(
            receiptDtoWithDuplicatedFlow, currentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(DUPLICATED_FLOW, exception.getMessage());
    }

    @Test
    @DisplayName("입출금 모두 공백으로 인한 영수증 생성 실패")
    void updateReceipt_EmptyFlow() {
        //Given
        Long existingId = receipt.getId();
        ReceiptDto receiptDtoWithEmptyFlow = ReceiptDto.builder()
            .receiptId(receipt.getId())
            .content(receipt.getContent())
            .deposit(0)
            .withdrawal(0)
            .build();
        when(receiptRepository.findById(existingId)).thenReturn(Optional.of(receipt));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        //When, Then
        CustomException exception = assertThrows(CustomException.class, () -> receiptService.updateReceipt(
            receiptDtoWithEmptyFlow, currentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(DUPLICATED_FLOW, exception.getMessage());
    }

    @Test
    @DisplayName("영수증 내용 공백으로 인한 영수증 생성 실패")
    void updateReceipt_EmptyContent() {
        //Given
        Long existingId = receipt.getId();
        ReceiptDto updateDto = ReceiptDto.builder()
            .receiptId(receipt.getId())
            .date(receipt.getDate())
            .content(" ")
            .deposit(receipt.getDeposit())
            .build();
        when(receiptRepository.findById(existingId)).thenReturn(Optional.of(receipt));
        when(userRepository.findByUserId(currentUser.getUsername())).thenReturn(Optional.of(user));
        //When, Then
        CustomException exception = assertThrows(CustomException.class, () -> receiptService.updateReceipt(
            updateDto, currentUser));

        assertEquals(400, exception.getErrorCode());
        assertEquals(EMPTY_CONTENT, exception.getMessage());
    }
}
