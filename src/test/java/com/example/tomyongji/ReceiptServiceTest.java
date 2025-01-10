package com.example.tomyongji;

import static com.example.tomyongji.validation.ErrorMsg.DUPLICATED_FLOW;
import static com.example.tomyongji.validation.ErrorMsg.EMPTY_CONTENT;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_RECEIPT;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
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
    private User user;
    private ReceiptCreateDto receiptCreateDto;
    private Receipt receipt;

    @BeforeEach
    void setUp() {
        studentClub = StudentClub.builder()
            .id(3L)
            .studentClubName("융합소프트웨어학부")
            .Balance(1000)
            .build();
        user = User.builder()
            .id(1L)
            .userId("testUser")
            .name("test name")
            .studentNum("60000000")
            .collegeName("ICT 융합대학")
            .email("test@example.com")
            .password("password123")
            .role("USER")
            .studentClub(studentClub)
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
        when(receiptMapper.toReceiptEntity(receiptCreateDto)).thenReturn(receipt);
        when(receiptMapper.toReceiptDto(receipt)).thenReturn(receiptDto);

        //When
        ReceiptDto result = receiptService.createReceipt(receiptCreateDto);

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
            receiptCreateDtoWithWrongUserId));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_USER, exception.getMessage());
        verify(userRepository).findByUserId(receiptCreateDtoWithWrongUserId.getUserId());
    }
    @Test
    @DisplayName("입출금 모두 작성으로 인한 영수증 생성 실패")
    void createReceipt_DuplicatedFlow() {
        //Given
        ReceiptCreateDto receiptCreateDtoWithDuplicatedFlow = ReceiptCreateDto.builder()
            .userId("testUser")
            .content("영수증 테스트")
            .deposit(1000)
            .withdrawal(1000)
            .build();
        when(userRepository.findByUserId(receiptCreateDtoWithDuplicatedFlow.getUserId())).thenReturn(Optional.of(user));
        //When, Then
        CustomException exception = assertThrows(CustomException.class, () -> receiptService.createReceipt(
            receiptCreateDtoWithDuplicatedFlow));

        assertEquals(400, exception.getErrorCode());
        assertEquals(DUPLICATED_FLOW, exception.getMessage());
        verify(userRepository).findByUserId(receiptCreateDtoWithDuplicatedFlow.getUserId());
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
        //When, Then
        CustomException exception = assertThrows(CustomException.class, () -> receiptService.createReceipt(
            receiptCreateDtoWithEmptyFlow));

        assertEquals(400, exception.getErrorCode());
        assertEquals(DUPLICATED_FLOW, exception.getMessage());
        verify(userRepository).findByUserId(receiptCreateDtoWithEmptyFlow.getUserId());
    }
    @Test
    @DisplayName("입출금 모두 공백으로 인한 영수증 생성 실패")
    void createReceipt_EmptyContent() {
        //Given
        ReceiptCreateDto receiptCreateDtoWithEmptyContent = ReceiptCreateDto.builder()
            .userId("testUser")
            .deposit(1000)
            .build();
        when(userRepository.findByUserId(receiptCreateDtoWithEmptyContent.getUserId())).thenReturn(Optional.of(user));
        //When, Then
        CustomException exception = assertThrows(CustomException.class, () -> receiptService.createReceipt(
            receiptCreateDtoWithEmptyContent));

        assertEquals(400, exception.getErrorCode());
        assertEquals(EMPTY_CONTENT, exception.getMessage());
        verify(userRepository).findByUserId(receiptCreateDtoWithEmptyContent.getUserId());
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
        ReceiptByStudentClubDto result = receiptService.getReceiptsByClub(clubId);
        //Then
        assertNotNull(result);
        assertEquals(result.getBalance(), 1000);
        assertEquals(result.getReceiptList().get(0), receiptDto1);
        assertEquals(result.getReceiptList().get(1), receiptDto2);
        verify(studentClubRepository).findById(clubId);
        verify(receiptRepository).findAllByStudentClub(studentClub);
    }

    @Test
    @DisplayName("학생회 조회 실패로 인한 특정 학생회 영수증 조회 실패")
    void getReceiptsByClub_NotFoundStudentClub() {
        //Given
        Long wrongClubId = 999L;

        when(studentClubRepository.findById(wrongClubId)).thenReturn(Optional.empty());
        //When, Then
        CustomException exception = assertThrows(CustomException.class,
            () -> receiptService.getReceiptsByClub(wrongClubId));

        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_STUDENT_CLUB, exception.getMessage());
        verify(studentClubRepository).findById(wrongClubId);
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
        when(receiptMapper.toReceiptDto(receipt)).thenReturn(receiptDto);
        //When
        ReceiptDto result = receiptService.deleteReceipt(receiptId);
        //Then
        assertNotNull(result);
        assertEquals(result, receiptDto);
        verify(receiptRepository).findById(receiptId);
        verify(receiptRepository).delete(receipt);
        verify(studentClubRepository).save(studentClub);
        verify(receiptMapper).toReceiptDto(receipt);
    }

    @Test
    @DisplayName("영수증 조회 실패로 인한 특정 영수증 삭제 성공")
    void deleteReceipt_NotFoundReceipt() {
        //Given
        Long wrongReceiptId = 999L;

        when(receiptRepository.findById(wrongReceiptId)).thenReturn(Optional.empty());
        //When
        CustomException exception = assertThrows(CustomException.class,
            () -> receiptService.deleteReceipt(wrongReceiptId));
        //Then
        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_RECEIPT, exception.getMessage());
        verify(receiptRepository).findById(wrongReceiptId);
    }

    @Test
    @DisplayName("영수증 수정 성공")
    void updateReceipt_Success() {
        //Given
        Long existingId = receipt.getId();
        ReceiptDto updateDto = ReceiptDto.builder()
            .receiptId(existingId)
            .date(receipt.getDate())
            .content("수정된 내용")
            .deposit(4500)
            .build();

        when(receiptRepository.findById(existingId)).thenReturn(Optional.of(receipt));
        when(receiptMapper.toReceiptDto(receipt)).thenReturn(updateDto);
        //When
        ReceiptDto result = receiptService.updateReceipt(updateDto);
        //Then
        assertNotNull(result);
        assertEquals(result.getContent(), "수정된 내용");
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
            () -> receiptService.updateReceipt(receiptDtoWithWrongId));
        //Then
        assertEquals(400, exception.getErrorCode());
        assertEquals(NOT_FOUND_RECEIPT, exception.getMessage());
        verify(receiptRepository).findById(wrongReceiptId);
    }


    //Given
    //When
    //Then
}
