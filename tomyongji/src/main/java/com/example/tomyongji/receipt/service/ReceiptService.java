package com.example.tomyongji.receipt.service;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
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
            throw new RuntimeException("유저를 찾을 수 없습니다.");
        }
        StudentClub studentClub = user.get().getStudentClub();

        if (receiptDto.getDate() == null) {
            receiptDto.setDate(new Date());
        }
        if (receiptDto.getContent() == null || receiptDto.getContent().trim().isEmpty()) {
            throw new RuntimeException("내용을 적어주세요.");
        }

        // null 값을 0으로 처리
        if (receiptDto.getDeposit() == 0 && receiptDto.getWithdrawal() == 0) {
            throw new RuntimeException("입출금 내역을 적어주세요.");
        }

        if (receiptDto.getDeposit() != 0 && receiptDto.getWithdrawal() != 0) {
            throw new RuntimeException("입금과 출금 둘 중 하나만 적어주세요.");
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

    public List<Receipt> getAllReceipts() {
        return receiptRepository.findAll();
    }
    public List<Receipt> getReceiptsByClub(Long clubId) {

        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new RuntimeException("학생회를 찾을 수 없습니다.");
        }

        return receiptRepository.findAllByStudentClub(studentClub.get());
    }
    public Receipt getReceiptById(Long id) {
        Optional<Receipt> receipt = receiptRepository.findById(id);
        if (receipt.isEmpty()) {
            throw new RuntimeException("영수증을 찾을 수 없습니다.");
        }
        return receipt.get();
    }
    public void deleteReceipt(Long id) {
        receiptRepository.deleteById(id);
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
            throw new RuntimeException("영수증을 찾을 수 없습니다.");
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


}
