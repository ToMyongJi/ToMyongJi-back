package com.example.tomyongji.receipt.service;

import static com.example.tomyongji.validation.ErrorMsg.DUPLICATED_FLOW;
import static com.example.tomyongji.validation.ErrorMsg.EMPTY_CONTENT;
import static com.example.tomyongji.validation.ErrorMsg.EMPTY_MONEY;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_RECEIPT;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.dto.ReceiptUpdateDto;
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
import java.util.Optional;
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
        Optional<User> user = userRepository.findById(receiptDto.getUserId());
        if (user.isEmpty()) {
            throw new CustomException(NOT_FOUND_USER, 400);
        }
        StudentClub studentClub = user.get().getStudentClub();

        if (receiptDto.getDate() == null) {
            receiptDto.setDate(new Date());
        }
        if (receiptDto.getContent() == null || receiptDto.getContent().trim().isEmpty()) {
            throw new CustomException(EMPTY_CONTENT, 400);
        }

        // null 값을 0으로 처리
        if (receiptDto.getDeposit() == 0 && receiptDto.getWithdrawal() == 0) {
            throw new CustomException(EMPTY_MONEY, 400);
        }

        if (receiptDto.getDeposit() != 0 && receiptDto.getWithdrawal() != 0) {
            throw new CustomException(DUPLICATED_FLOW, 400);
        }

        Receipt receipt = receiptMapper.toReceiptEntity(receiptDto);
        receipt.setStudentClub(studentClub);

        // 시분초를 0으로 설정하여 저장
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(receipt.getDate());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        receipt.setDate(calendar.getTime());

        receiptRepository.save(receipt);
        return receiptMapper.toReceiptDto(receiptDto);
    }

    public List<ReceiptDto> getAllReceipts() {
        List<Receipt> receipts = receiptRepository.findAll();
        return receiptDtoList(receipts);
    }
    public List<ReceiptDto> getReceiptsByClub(Long clubId) {

        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }

        List<Receipt> receipts = receiptRepository.findAllByStudentClub(studentClub.get());
        return receiptDtoList(receipts);
    }
    public ReceiptDto getReceiptById(Long receiptId) {
        Optional<Receipt> receipt = receiptRepository.findById(receiptId);
        if (receipt.isEmpty()) {
            throw new CustomException(NOT_FOUND_RECEIPT, 400);
        }
        return receiptMapper.toReceiptDto(receipt.get());
    }
    public ReceiptDto deleteReceipt(Long receiptId) {
        Optional<Receipt> receipt = receiptRepository.findById(receiptId);
        if (receipt.isEmpty()) {
            throw new CustomException(NOT_FOUND_RECEIPT, 400);
        }
        ReceiptDto receiptDto = receiptMapper.toReceiptDto(receipt.get());
        receiptRepository.deleteById(receiptId);
        return receiptDto;
    }
    public ReceiptDto updateReceipt(ReceiptUpdateDto receiptDto) {
        Optional<Receipt> optionalReceipt = receiptRepository.findById(receiptDto.getReceiptId());

        if (optionalReceipt.isPresent()) {
            Receipt existingReceipt = optionalReceipt.get(); //기존의

            // DTO로 들어온 값이 null이 아니면 해당 필드 업데이트
            if (receiptDto.getDate() != null) {
                existingReceipt.setDate(receiptDto.getDate());
            }
            if (receiptDto.getContent() != null) {
                existingReceipt.setContent(receiptDto.getContent());
            }
            if (receiptDto.getDeposit() != 0) {
                existingReceipt.setDeposit(receiptDto.getDeposit());
            }
            if (receiptDto.getWithdrawal() != 0) {
                existingReceipt.setWithdrawal(receiptDto.getWithdrawal());
            }

            receiptRepository.save(existingReceipt);
            return receiptMapper.toReceiptDto(receiptDto);
        } else {
            throw new CustomException(NOT_FOUND_RECEIPT, 400);
        }
    }

    private List<ReceiptDto> receiptDtoList(List<Receipt> receipts) {
        List<ReceiptDto> receiptDtoList = new ArrayList<>();
        for (Receipt receipt : receipts) {
            receiptDtoList.add(receiptMapper.toReceiptDto(receipt));
        }
        return receiptDtoList;
    }


}
