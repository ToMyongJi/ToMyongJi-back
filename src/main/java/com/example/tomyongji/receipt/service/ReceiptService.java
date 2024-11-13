package com.example.tomyongji.receipt.service;

import static com.example.tomyongji.validation.ErrorMsg.DUPLICATED_FLOW;
import static com.example.tomyongji.validation.ErrorMsg.EMPTY_CONTENT;
import static com.example.tomyongji.validation.ErrorMsg.EMPTY_MONEY;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_RECEIPT;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
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

    @Autowired
    public ReceiptService(ReceiptRepository receiptRepository,
        StudentClubRepository studentClubRepository, UserRepository userRepository) {
        this.receiptRepository = receiptRepository;
        this.studentClubRepository = studentClubRepository;
        this.userRepository = userRepository;
    }

    public ReceiptDto createReceipt(ReceiptDto receiptDto, Long id) {
        Optional<User> user = userRepository.findById(id);
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

        Receipt receipt = convertToReceipt(receiptDto);
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
        return receiptDto;
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
    public ReceiptDto getReceiptById(Long id) {
        Optional<Receipt> receipt = receiptRepository.findById(id);
        if (receipt.isEmpty()) {
            throw new CustomException(NOT_FOUND_RECEIPT, 400);
        }
        return convertToReceiptDto(receipt.get());
    }
    public ReceiptDto deleteReceipt(Long id) {
        Optional<Receipt> receipt = receiptRepository.findById(id);
        if (receipt.isEmpty()) {
            throw new CustomException(NOT_FOUND_RECEIPT, 400);
        }
        ReceiptDto receiptDto = convertToReceiptDto(receipt.get());
        receiptRepository.deleteById(id);
        return receiptDto;
    }
    public ReceiptDto updateReceipt(Long id, ReceiptDto receiptDto) {
        Optional<Receipt> optionalReceipt = receiptRepository.findById(id);

        if (optionalReceipt.isPresent()) {
            Receipt existingReceipt = optionalReceipt.get();

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
            return receiptDto;
        } else {
            throw new CustomException(NOT_FOUND_RECEIPT, 400);
        }
    }
    public Receipt convertToReceipt(ReceiptDto receiptDto) {
        Receipt receipt = new Receipt();
        receipt.setDate(receiptDto.getDate());
        receipt.setContent(receiptDto.getContent());
        receipt.setDeposit(receiptDto.getDeposit());
        receipt.setWithdrawal(receiptDto.getWithdrawal());
        return receipt;
    }

    private ReceiptDto convertToReceiptDto(Receipt receipt) {
        ReceiptDto receiptDto = new ReceiptDto();
        receiptDto.setReceiptId(receipt.getId());
        receiptDto.setDate(receipt.getDate());
        receiptDto.setContent(receipt.getContent());
        receiptDto.setDeposit(receipt.getDeposit());
        receiptDto.setWithdrawal(receipt.getWithdrawal());
        return receiptDto;
    }

    private List<ReceiptDto> receiptDtoList(List<Receipt> receipts) {
        List<ReceiptDto> receiptDtoList = new ArrayList<>();
        for (Receipt receipt : receipts) {
            receiptDtoList.add(convertToReceiptDto(receipt));
        }
        return receiptDtoList;
    }


}
