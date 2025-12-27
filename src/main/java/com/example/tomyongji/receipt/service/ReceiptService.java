package com.example.tomyongji.receipt.service;

import static com.example.tomyongji.validation.ErrorMsg.DUPLICATED_FLOW;
import static com.example.tomyongji.validation.ErrorMsg.EMPTY_CONTENT;
import static com.example.tomyongji.validation.ErrorMsg.INVALID_DATE_SEARCH;
import static com.example.tomyongji.validation.ErrorMsg.INVALID_KEYWORD;
import static com.example.tomyongji.validation.ErrorMsg.MISMATCHED_USER;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_RECEIPT;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_BELONGING;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_USER;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.auth.service.CustomUserDetails;
import com.example.tomyongji.logging.AuditLog;
import com.example.tomyongji.receipt.dto.PagingReceiptDto;
import com.example.tomyongji.receipt.dto.ReceiptByStudentClubDto;
import com.example.tomyongji.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.mapper.ReceiptMapper;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final StudentClubRepository studentClubRepository;
    private final UserRepository userRepository;
    private final ReceiptMapper receiptMapper;
    private final CacheManager cacheManager;

    @Transactional
    public ReceiptDto createReceipt(ReceiptCreateDto receiptDto, UserDetails currentUser) {
        //유저 및 소속 클럽 조회
        User user = userRepository.findByUserId(receiptDto.getUserId())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400)); //유저를 찾을 수 없는 경우
        StudentClub studentClub = user.getStudentClub();
        Long clubId = studentClub.getId();

        //소속 검증
        checkClub(studentClub, currentUser);

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

        //캐시 수동삭제
        clearReceiptCache(clubId);

        //생성된 영수증 DTO 반환
        return receiptMapper.toReceiptDto(receipt);
    }

    @Transactional(readOnly = true)
    public List<ReceiptDto> getAllReceipts() {
        List<Receipt> receipts = receiptRepository.findAll();
        return receiptDtoList(receipts);
    }

    @Transactional(readOnly = true)
    public ReceiptByStudentClubDto getReceiptsByClub(Long id, UserDetails currentUser) {

        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));

        StudentClub studentClub = user.getStudentClub();

        checkClub(studentClub, currentUser);

        List<Receipt> receipts = receiptRepository.findAllByStudentClubOrderByIdDesc(studentClub);

        ReceiptByStudentClubDto receiptByStudentClubDto = new ReceiptByStudentClubDto();
        receiptByStudentClubDto.setReceiptList(receipts.stream()
                .map(receiptMapper::toReceiptDto)
                .collect(Collectors.toList()));
        receiptByStudentClubDto.setBalance(studentClub.getBalance());
        return receiptByStudentClubDto;
    }

    @Transactional(readOnly = true)
    public List<ReceiptDto> getReceiptsByClubForStudent(Long clubId) {
        StudentClub studentClub = studentClubRepository.findById(clubId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_STUDENT_CLUB, 400));

        List<Receipt> receipts = receiptRepository.findAllByStudentClubOrderByIdDesc(studentClub);

        return receipts.stream()
            .map(receiptMapper::toReceiptDto)
            .collect(Collectors.toList());
    }

    @Cacheable(
        value = "'receiptList:' + #clubId",
        key = "'p' + #page + '_s' + #size + '_' + #year + '_' + #month",
        sync = true
    )
    @Transactional(readOnly = true)
    public PagingReceiptDto getReceiptsByClubPaging(Long clubId, int page, int size, Integer year, Integer month) { // page, size를 파라미터로 받음

        StudentClub studentClub = studentClubRepository.findById(clubId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_STUDENT_CLUB, 400));

        // "date" 내림차순, "id" 내림차순 (인덱스 순서와 동일하게)
        Pageable pageable = PageRequest.of(page, size, Sort.by(
            Sort.Order.desc("date"),
            Sort.Order.desc("id")
        ));

        Page<Receipt> receiptPage;

        // 1. [정상] 년도와 월이 모두 있는 경우 -> 해당 월 조회
        if (year != null && month != null) {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDateTime startDateTime = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endDateTime = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            receiptPage = searchByDateRange(studentClub, startDateTime, endDateTime, pageable);

        }
        // 2. [정상] 년도만 있는 경우 -> 해당 연도 전체(1월~12월) 조회
        else if (year != null) {
            LocalDateTime startDateTime = Year.of(year).atDay(1).atStartOfDay(); // 1월 1일 00:00:00
            LocalDateTime endDateTime = Year.of(year).atMonth(12).atEndOfMonth().atTime(23, 59, 59); // 12월 31일 23:59:59

            receiptPage = searchByDateRange(studentClub, startDateTime, endDateTime, pageable);
        }
        // 3. [에러] 월만 있는 경우 -> 년도 없이 월만 조회할 수 없음
        else if (month != null) {
            throw new CustomException(INVALID_DATE_SEARCH, 400);
        }
        // 4. [정상] 둘 다 없는 경우 -> 전체 조회
        else {
            receiptPage = receiptRepository.findByStudentClub(studentClub, pageable);
        }

        Page<ReceiptDto> dtoPage = receiptPage.map(receiptMapper::toReceiptDto);

        // DTO 변환 및 반환
        return PagingReceiptDto.from(dtoPage);
    }

    private Page<Receipt> searchByDateRange(StudentClub club, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        Date startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());

        return receiptRepository.findAllByStudentClubAndDateBetween(club, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceiptById(Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_RECEIPT, 400));

        return receiptMapper.toReceiptDto(receipt);
    }

    @Transactional
    @AuditLog(action = "영수증 삭제")
    public ReceiptDto deleteReceipt(Long receiptId, UserDetails currentUser) {
        //접근 권한: 유저가 아닌 경우

        //영수증 조회 및 존재 여부 확인
        Receipt receipt = receiptRepository.findById(receiptId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_RECEIPT, 400));

        //접근 권한: 다른 학생회인 경우
        StudentClub studentClub = receipt.getStudentClub();
        checkClub(studentClub, currentUser);
        Long clubId = studentClub.getId();

        //영수증 삭제
        receiptRepository.delete(receipt);

        //잔액 업데이트 (항상 deposit 또는 withdrawal 중 하나만 값이 존재)
        int balanceAdjustment = receipt.getDeposit() - receipt.getWithdrawal();
        studentClub.setBalance(studentClub.getBalance() - balanceAdjustment);
        studentClubRepository.save(studentClub);

        //영수증 비율 확인 및 학생회 상태 변경
        checkAndUpdateVerificationStatus(studentClub.getId());

        //캐시 수동삭제
        clearReceiptCache(clubId);

        //DTO 반환
        return receiptMapper.toReceiptDto(receipt);
    }

    @Transactional
    @AuditLog(action = "영수증 수정")
    public ReceiptDto updateReceipt(ReceiptDto receiptDto, UserDetails currentUser) {
        //영수증 조회
        Receipt existingReceipt = receiptRepository.findById(receiptDto.getReceiptId())
            .orElseThrow(() -> new CustomException(NOT_FOUND_RECEIPT, 400));

        StudentClub studentClub = existingReceipt.getStudentClub();

        //영수증의 학생회와 접속한 유저의 학생회 비교
        checkClub(studentClub, currentUser);
        Long clubId = studentClub.getId();

        int updatedDeposit = receiptDto.getDeposit();
        int updatedWithdrawal = receiptDto.getWithdrawal();

        validateReceiptUpdateValues(updatedDeposit, updatedWithdrawal, receiptDto.getContent());

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
        existingReceipt.setDeposit(updatedDeposit);
        existingReceipt.setWithdrawal(updatedWithdrawal);

        //새 잔액 반영
        int newAdjustment = updatedDeposit - updatedWithdrawal;
        studentClub.setBalance(studentClub.getBalance() + newAdjustment);

        studentClubRepository.save(studentClub);
        receiptRepository.save(existingReceipt);

        //캐시 수동삭제
        clearReceiptCache(clubId);

        return receiptMapper.toReceiptDto(existingReceipt);
    }

    private void validateReceiptUpdateValues(Integer deposit, Integer withdrawal, String content) {
        // 입출금 값 검증: 둘 다 0이거나 둘 다 0이 아니면 예외 발생
        if ((deposit == 0 && withdrawal == 0) || (deposit != 0 && withdrawal != 0)) {
            throw new CustomException(DUPLICATED_FLOW, 400);
        }

        // content가 전달되었으면(즉, null이 아니면) 빈 문자열이 아닌지 검증
        if (content != null && content.trim().isEmpty()) {
            throw new CustomException(EMPTY_CONTENT, 400);
        }
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

    private Date resetTimeToMidnight(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    //전체 영수증 대비 토스뱅크로 검증된 영수증 비율에 따른 뱃지 부여 메서드
    //영수증 삭제 등과 같은 경우 사용
    public void checkAndUpdateVerificationStatus(Long clubId) {
        StudentClub club = studentClubRepository.findById(clubId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_STUDENT_CLUB, 400));

        ReceiptRepository.ReceiptCount count = receiptRepository.countTotalAndVerified(club);
        Long totalReceipts = count.getTotal();
        if (totalReceipts == 0) {
            return;
        }

        Long verifiedReceipts = count.getVerified();
        long verified = verifiedReceipts != null ? verifiedReceipts : 0L;
        double verificationRatio = (double) verified / totalReceipts;

        //boolean 으로 현재 상태와 검증된 상태의 값이 다를 경우에만 DB 업데이트
        boolean shouldBeVerified = verificationRatio > 0.3;

        if (club.isVerification() != shouldBeVerified) {
            if (shouldBeVerified) {
                studentClubRepository.updateVerificationById(clubId);
            } else {
                studentClubRepository.updateVerificationToFalseById(clubId);
            }
        }
    }

    //전체 영수증 대비 토스뱅크로 검증된 영수증 비율에 따른 뱃지 부여 메서드
    //토스뱅크 거래내역서를 통한 영수증 생성 시 성능 개선 메서드
    public void checkAndUpdateVerificationStatus(Long clubId, long totalReceipts, long verifiedReceipts) {
        StudentClub club = studentClubRepository.findById(clubId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_STUDENT_CLUB, 400));

        if (totalReceipts == 0) {
            return;
        }

        double verificationRatio = (double) verifiedReceipts / totalReceipts;

        //boolean 으로 현재 상태와 검증된 상태의 값이 다를 경우에만 DB 업데이트
        boolean shouldBeVerified = verificationRatio > 0.3;

        if (club.isVerification() != shouldBeVerified) {
            if (shouldBeVerified) {
                studentClubRepository.updateVerificationById(clubId);
            } else {
                studentClubRepository.updateVerificationToFalseById(clubId);
            }
        }
    }

    private void checkClub(StudentClub studentClub, UserDetails currentUser) {
        User compareUser = userRepository.findByUserId(currentUser.getUsername())
            .orElseThrow(() -> new CustomException(NO_AUTHORIZATION_USER, 400));
        if (!studentClub.equals(compareUser.getStudentClub())) {
            throw new CustomException(NO_AUTHORIZATION_BELONGING, 400);
        }
    }

    @Transactional(readOnly = true)
    public List<ReceiptDto> searchReceiptByKeyword(String keyword, UserDetails currentUser) {
        //접근 권한: 유저가 아닌 경우
        User user = userRepository.findByUserId(currentUser.getUsername())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));
        StudentClub studentClub = user.getStudentClub();

        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.length() < 2) {
            throw new CustomException(INVALID_KEYWORD, 400);
        }

        List<Receipt> matchedReceipts = receiptRepository.findByStudentClubAndContent(
            studentClub.getId(),keyword.trim());

        return matchedReceipts.stream()
            .map(receiptMapper::toReceiptDto)
            .collect(Collectors.toList());
    }

    public void clearReceiptCache(Long clubId) {
        Cache cache = cacheManager.getCache("receiptList:" + clubId);
        if (cache != null) {
            cache.clear();
        }
    }
}
