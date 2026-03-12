package com.example.tomyongji.domain.receipt.service;

import com.example.tomyongji.domain.admin.dto.PresidentDto;
import com.example.tomyongji.domain.admin.entity.Member;
import com.example.tomyongji.domain.admin.entity.President;
import com.example.tomyongji.domain.admin.repository.MemberRepository;
import com.example.tomyongji.domain.admin.repository.PresidentRepository;
import com.example.tomyongji.domain.admin.service.AdminService;
import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.auth.service.UserService;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.mapper.StudentClubMapper;
import com.example.tomyongji.domain.receipt.repository.ReceiptRepository;
import com.example.tomyongji.domain.receipt.repository.StudentClubRepository;
import com.example.tomyongji.domain.receipt.dto.ClubDto;
import com.example.tomyongji.domain.receipt.dto.TransferDto;
import com.example.tomyongji.domain.receipt.entity.Receipt;

import java.util.*;
import java.util.stream.Collectors;

import com.example.tomyongji.global.error.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.tomyongji.global.error.ErrorMsg.*;

@Service
@RequiredArgsConstructor
public class StudentClubService {
    private final StudentClubRepository studentClubRepository;
    private final StudentClubMapper studentClubMapper;
    private final ReceiptRepository receiptRepository;
    private final UserRepository userRepository;
    private final PresidentRepository presidentRepository;
    private final MemberRepository memberRepository;
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

    @Transactional
    public TransferDto transferStudentClubAndUser(PresidentDto nextPresident, UserDetails currentUser, List<String> remainUserIds) {

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

        //토스뱅크 인증 마크 제거
        studentClub.setVerification(false);
        studentClubRepository.save(studentClub);

        //차기 회장 정보 처리
        handleNextPresident(nextPresident, remainUserIds, studentClub, user);

        //잔류 인원 제외 기존 학생회 멤버 전체 삭제
        deleteAllStudentClubMembersExceptRemainUsers(studentClub, new HashSet<>(remainUserIds));

        return summary;
    }

    private void handleNextPresident(PresidentDto nextPresident, List<String> remainUserIds, StudentClub studentClub, User oldPresidentUser) {
        if (nextPresident == null)  {
            demoteOldPresident(oldPresidentUser, studentClub);
            studentClub.setPresident(null);
            return;
        }

        String nextNum = nextPresident.getStudentNum();
        // 차기 회장이 이미 타 학생회 소속인지 확인
        validateMembership(nextNum, studentClub);
        // 차기 회장 잔류 인원 처리
        remainUserIds.add(nextNum);

        // 차기 회장이 기존 회장 -> 변경 사항 추가(이름)
        if (presidentRepository.existsByStudentNum(nextNum)) {
            nextPresident.setClubId(studentClub.getId());
            adminService.savePresident(nextPresident);
            return;
        }

        // 차기 회장이 기존 부원 -> member 에서 삭제 + User 권한 변경
        memberRepository.findByStudentNum(nextNum).ifPresent(member -> {
            memberRepository.delete(member);

            User nextPresidentUser = userRepository.findByStudentNum(nextNum);
            if(nextPresidentUser != null) {
                nextPresidentUser.setRole("PRESIDENT");
                userRepository.save(nextPresidentUser);
            }
        });

        // 차기 회장이 신규 유저 또는 기존 부원 공통 -> President에 저장
        nextPresident.setClubId(studentClub.getId());
        adminService.savePresident(nextPresident);

        // 이전 회장 부원으로 강등
        demoteOldPresident(oldPresidentUser, studentClub);

    }

    private void demoteOldPresident(User oldUser, StudentClub studentClub) {
        // User 권한 강등 (PRESIDENT -> STU)
        oldUser.setRole("STU");
        userRepository.save(oldUser);

        // President 테이블에서 삭제
        presidentRepository.deleteByStudentNum(oldUser.getStudentNum());

        // Member 테이블에 부원으로 등록
        if (!memberRepository.existsByStudentNum(oldUser.getStudentNum())) {
            Member member = Member.builder()
                    .studentNum(oldUser.getStudentNum())
                    .name(oldUser.getName())
                    .studentClub(studentClub)
                    .build();
            memberRepository.save(member);
        }
    }

    private void validateMembership(String studentNum, StudentClub currentClub) {
        // 타 학생회 부원 여부 체크
        memberRepository.findByStudentNum(studentNum)
                .filter(member -> !member.getStudentClub().equals(currentClub))
                .ifPresent(member -> { throw new CustomException(ALREADY_BELONGING_USER, 400); });

        // 타 학생회 회장 여부 체크
        President president = presidentRepository.findByStudentNum(studentNum);
        if (president != null && !currentClub.getPresident().equals(president)) {
            throw new CustomException(ALREADY_BELONGING_USER, 400);
        }
    }


    // 잔류인원 제외 회장 및 학생회 멤버 삭제
    private void deleteAllStudentClubMembersExceptRemainUsers(StudentClub studentClub, Set<String> remainUserIds) {
        // 잔류인원 제외 회장 및 부원 삭제
        List<User> students = userRepository.findByStudentClub(studentClub);
        for (User student : students) {
            if(!remainUserIds.contains(student.getStudentNum())) {
                userService.deleteUser(student.getUserId());
            }
        }
    }
}
