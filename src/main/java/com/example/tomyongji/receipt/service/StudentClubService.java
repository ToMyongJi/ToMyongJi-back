package com.example.tomyongji.receipt.service;

import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.admin.service.AdminService;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.auth.service.UserService;
import com.example.tomyongji.receipt.dto.ClubDto;
import com.example.tomyongji.receipt.dto.TransferDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.mapper.StudentClubMapper;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

import com.example.tomyongji.validation.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_ROLE;
import static com.example.tomyongji.validation.ErrorMsg.NO_RECEIPTS_TO_TRANSFER;

@Service
@RequiredArgsConstructor
public class StudentClubService {
    private final StudentClubRepository studentClubRepository;
    private final StudentClubMapper studentClubMapper;
    private final ReceiptRepository receiptRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AdminService adminService;

    public List<ClubDto> getAllStudentClub() {
        List<StudentClub> studentClubs = studentClubRepository.findAll();

        // 특정 StudentClub을 제외 (예: 특정 이름 제외)
        List<StudentClub> filteredClubs = studentClubs.stream()
            .filter(club -> !club.getStudentClubName().equals("어드민")) // 제외할 조건
            .collect(Collectors.toList());

        return clubDtoList(filteredClubs);
    }


    public List<ClubDto> getStudentClubById(Long collegeId) {
        List<StudentClub> studentClubs = studentClubRepository.findAllByCollege_Id(collegeId);
        return clubDtoList(studentClubs);
    }
    private List<ClubDto> clubDtoList(List<StudentClub> studentClubs) {
        List<ClubDto> clubDtoList = new ArrayList<>();
        for (StudentClub studentClub : studentClubs) {
            clubDtoList.add(studentClubMapper.toClubDto(studentClub));
        }
        return clubDtoList;
    }

    @Transactional
    public TransferDto transferStudentClub(PresidentDto nextPresident, UserDetails currentUser) {

        User user = userRepository.findByUserId(currentUser.getUsername())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));

        if (!"PRESIDENT".equals(user.getRole())) {
            throw new CustomException(NO_AUTHORIZATION_ROLE, 403);
        }

        StudentClub studentClub = user.getStudentClub();
        if (studentClub == null) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }

        //영수증 처리
        TransferDto summary = transferReceipt(studentClub);

        //기존 학생회 멤버 전체 삭제
        deleteAllStudentClubMembers(studentClub);

        //토스뱅크 인증 마크 제거
        studentClub.setVerification(false);
        studentClubRepository.save(studentClub);

        //다음 회장이 있을 땐 새 회장 등록
        if (nextPresident != null) {
            nextPresident.setClubId(studentClub.getId());
            adminService.savePresident(nextPresident);
        }

        return summary;
    }

    private TransferDto transferReceipt(StudentClub studentClub) {
        // 모든 영수증 조회
        List<Receipt> receipts = receiptRepository.findAllByStudentClubOrderByIdDesc(studentClub);

        // 총 입금/출금 계산
        int totalDeposit = receipts.stream()
                .mapToInt(Receipt::getDeposit)
                .sum();

        int totalWithdrawal = receipts.stream()
                .mapToInt(Receipt::getWithdrawal)
                .sum();

        int netAmount = totalDeposit - totalWithdrawal;

        //영수증 0개일 경우 에러 발생
        if (receipts.isEmpty()) {
            throw new CustomException(NO_RECEIPTS_TO_TRANSFER, 400);
        }

        // 모든 영수증 삭제
        receiptRepository.deleteAll(receipts);

        // 이월 영수증 생성
        Receipt transferReceipt = Receipt.builder()
                .date(new Date())
                .content("학생회비 이월")
                .deposit(totalDeposit-totalWithdrawal)
                .withdrawal(0)
                .verification(false)
                .studentClub(studentClub)
                .build();

        receiptRepository.save(transferReceipt);

        return TransferDto.builder()
                .studentClubName(studentClub.getStudentClubName())
                .totalDeposit(totalDeposit-totalWithdrawal)
                .netAmount(netAmount)
                .build();
    }

    //회장 및 학생회 멤버 삭제
    private void deleteAllStudentClubMembers(StudentClub studentClub) {
        // 회장 삭제
        User president = userRepository.findFirstByStudentClubAndRole(studentClub, "PRESIDENT");
        if (president != null) {
            userService.deleteUser(president.getUserId());
        }

        // 부원 삭제
        List<User> students = userRepository.findByStudentClubAndRole(studentClub, "STU");
        for (User student : students) {
            userService.deleteUser(student.getUserId());
        }
    }
}