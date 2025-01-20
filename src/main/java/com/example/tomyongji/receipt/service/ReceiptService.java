package com.example.tomyongji.receipt.service;

import static com.example.tomyongji.validation.ErrorMsg.DUPLICATED_FLOW;
import static com.example.tomyongji.validation.ErrorMsg.EMPTY_CONTENT;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_RECEIPT;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_BELONGING;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_USER;

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
import com.example.tomyongji.validation.CustomException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final StudentClubRepository studentClubRepository;
    private final UserRepository userRepository;
    private final ReceiptMapper receiptMapper;

    @Autowired
    public ReceiptService(ReceiptRepository receiptRepository,
        StudentClubRepository studentClubRepository, UserRepository userRepository,
        ReceiptMapper receiptMapper) {
        this.receiptRepository = receiptRepository;
        this.studentClubRepository = studentClubRepository;
        this.userRepository = userRepository;
        this.receiptMapper = receiptMapper;
    }

    public ReceiptDto createReceipt(ReceiptCreateDto receiptDto) {
        //유저 및 소속 클럽 조회
        User user = userRepository.findByUserId(receiptDto.getUserId())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));
        StudentClub studentClub = user.getStudentClub();

        //필수 입력값 검증
        validateReceiptDto(receiptDto);

        //날짜 초기화 및 시분초 제거
        if (receiptDto.getDate() == null) {
            receiptDto.setDate(new Date());
        }
        receiptDto.setDate(resetTimeToMidnight(receiptDto.getDate()));

        //영수증 엔티티 생성 및 저장
        Receipt receipt = receiptMapper.toReceiptEntity(receiptDto);
        receipt.setStudentClub(studentClub);
        receiptRepository.save(receipt);

        //잔액 업데이트
        int balanceAdjustment = receiptDto.getDeposit() - receiptDto.getWithdrawal();
        studentClub.setBalance(studentClub.getBalance() + balanceAdjustment);
        studentClubRepository.save(studentClub);

        //생성된 영수증 DTO 반환
        return receiptMapper.toReceiptDto(receipt);
    }


    public List<ReceiptDto> getAllReceipts() {
        List<Receipt> receipts = receiptRepository.findAll();
        return receiptDtoList(receipts);
    }
    public ReceiptByStudentClubDto getReceiptsByClub(Long clubId) {

        StudentClub studentClub = studentClubRepository.findById(clubId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_STUDENT_CLUB, 400));

        List<Receipt> receipts = receiptRepository.findAllByStudentClub(studentClub);

        ReceiptByStudentClubDto receiptByStudentClubDto = new ReceiptByStudentClubDto();
        receiptByStudentClubDto.setReceiptList(receipts.stream()
                .map(receiptMapper::toReceiptDto)
                .collect(Collectors.toList()));
        receiptByStudentClubDto.setBalance(studentClub.getBalance());
        return receiptByStudentClubDto;
    }
    public ReceiptDto getReceiptById(Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_RECEIPT, 400));

        return receiptMapper.toReceiptDto(receipt);
    }
//    public ReceiptDto deleteReceipt(Long receiptId, CustomUserDetails currentUser) {
//        //접근 권한: 유저가 아닌 경우
//        if (currentUser == null || currentUser.getUser() == null) {
//            throw new CustomException(NO_AUTHORIZATION_USER, 400);
//        }
//        User user = currentUser.getUser();
//        StudentClub userStudentClub = user.getStudentClub();
//
//        //영수증 조회 및 존재 여부 확인
//        Receipt receipt = receiptRepository.findById(receiptId)
//            .orElseThrow(() -> new CustomException(NOT_FOUND_RECEIPT, 400));
//
//        //접근 권한: 다른 학생회인 경우
//        StudentClub studentClub = receipt.getStudentClub();
//        if (studentClub != userStudentClub) {
//            throw new CustomException(NO_AUTHORIZATION_BELONGING, 400);
//        }
//
//        //영수증 삭제
//        receiptRepository.delete(receipt);
//
//        //잔액 업데이트 (항상 deposit 또는 withdrawal 중 하나만 값이 존재)
//        int balanceAdjustment = receipt.getDeposit() - receipt.getWithdrawal();
//        studentClub.setBalance(studentClub.getBalance() - balanceAdjustment);
//        studentClubRepository.save(studentClub);
//
//        //DTO 반환
//        return receiptMapper.toReceiptDto(receipt);
//    }

    public ReceiptDto deleteReceipt(Long receiptId) {
        //영수증 조회 및 존재 여부 확인
        Receipt receipt = receiptRepository.findById(receiptId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_RECEIPT, 400));

        StudentClub studentClub = receipt.getStudentClub();

        //영수증 삭제
        receiptRepository.delete(receipt);

        //잔액 업데이트 (항상 deposit 또는 withdrawal 중 하나만 값이 존재)
        int balanceAdjustment = receipt.getDeposit() - receipt.getWithdrawal();
        studentClub.setBalance(studentClub.getBalance() - balanceAdjustment);
        studentClubRepository.save(studentClub);

        //DTO 반환
        return receiptMapper.toReceiptDto(receipt);
    }

    public ReceiptDto updateReceipt(ReceiptDto receiptDto) {
        //영수증 조회
        Receipt existingReceipt = receiptRepository.findById(receiptDto.getReceiptId())
            .orElseThrow(() -> new CustomException(NOT_FOUND_RECEIPT, 400));

        //유효성 검증
        validateReceiptDtoForUpdate(receiptDto);

        StudentClub studentClub = existingReceipt.getStudentClub();

        //기존 잔액 복원
        int previousAdjustment = existingReceipt.getDeposit() - existingReceipt.getWithdrawal();
        studentClub.setBalance(studentClub.getBalance() - previousAdjustment);

        //영수증 값 업데이트
        if (receiptDto.getDate() != null) {
            existingReceipt.setDate(receiptDto.getDate());
        }
        if (receiptDto.getContent() != null) {
            existingReceipt.setContent(receiptDto.getContent());
        }
        existingReceipt.setDeposit(receiptDto.getDeposit());
        existingReceipt.setWithdrawal(receiptDto.getWithdrawal());

        //새 잔액 반영
        int newAdjustment = receiptDto.getDeposit() - receiptDto.getWithdrawal();
        studentClub.setBalance(studentClub.getBalance() + newAdjustment);

        studentClubRepository.save(studentClub);
        receiptRepository.save(existingReceipt);

        return receiptMapper.toReceiptDto(existingReceipt);
    }


    private List<ReceiptDto> receiptDtoList(List<Receipt> receipts) {
        List<ReceiptDto> receiptDtoList = new ArrayList<>();
        for (Receipt receipt : receipts) {
            receiptDtoList.add(receiptMapper.toReceiptDto(receipt));
        }
        return receiptDtoList;
    }

    private void validateReceiptDto(ReceiptCreateDto receiptDto) {
        if ((receiptDto.getDeposit() == 0 && receiptDto.getWithdrawal() == 0) ||
            (receiptDto.getDeposit() != 0 && receiptDto.getWithdrawal() != 0)) {
            throw new CustomException(DUPLICATED_FLOW, 400);
        }
        if (receiptDto.getContent() == null || receiptDto.getContent().trim().isEmpty()) {
            throw new CustomException(EMPTY_CONTENT, 400);
        }
    }

    private void validateReceiptDtoForUpdate(ReceiptDto receiptDto) {
        if ((receiptDto.getDeposit() == 0 && receiptDto.getWithdrawal() == 0) ||
            (receiptDto.getDeposit() != 0 && receiptDto.getWithdrawal() != 0)) {
            throw new CustomException(DUPLICATED_FLOW, 400);
        }
    }

    private Date resetTimeToMidnight(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
