package com.example.tomyongji.receipt.service;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.dto.BreakDownDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.mapper.ReceiptMapper;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import static com.example.tomyongji.validation.ErrorMsg.*;

@Service
@RequiredArgsConstructor
public class BreakDownService {

    private final UserRepository userRepository;
    private final RestClient restClient;
    private final ReceiptRepository receiptRepository;
    private final StudentClubRepository studentClubRepository;
    private final ReceiptMapper mapper;
    private final ReceiptService receiptService;

    public BreakDownDto parsePdf(MultipartFile file,
        String userId, String keyword, UserDetails currentUser) throws Exception {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));

        StudentClub club = user.getStudentClub();
        if (club == null) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }

        // 권한 검증
        checkClub(club, currentUser);

        if (!validatePdfFile(file)) {
            throw new CustomException(EMPTY_FILE, 400);
        }

        try (InputStream in = file.getInputStream();
            PDDocument document = PDDocument.load(in)) {

            String text = new PDFTextStripper().getText(document);

            //날짜 파싱 과정
            // 1. pdf에서 추출 -> 2. 날짜 Date형식으로 정리 -> 3. ISO 타입으로 재 포맷
            String korDate = extractValue(text,
                "발급일자\\s+(\\d{4}년\\s+\\d{2}월\\s+\\d{2}일)");
            Date parsed = new SimpleDateFormat("yyyy년 MM월 dd일")
                .parse(korDate);
            String isoDate = new SimpleDateFormat("yyyy-MM-dd")
                .format(parsed);

            String issueNumber = extractValue(text,
                "발급번호\\s+([A-Z0-9\\-]+)");

            return BreakDownDto.builder()
                    .keyword(keyword)
                    .issueDate(isoDate)
                    .issueNumber(issueNumber)
                    .studentClubId(club.getId())
                    .build();

        } catch (Exception e) {
            throw new CustomException(PARSING_ERROR, 500);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<ReceiptDto> fetchAndProcessDocument(BreakDownDto dto) throws ParseException {
        String url = "https://api.tossbank.com/api-public/document/view/{date}/{docId}";
        String body = restClient.get()
            .uri(url, dto.getIssueDate(), dto.getIssueNumber())
            .retrieve()
            .body(String.class);
        //getBody = HTML 로 반환
        return proceedNext(body, dto.getStudentClubId(), dto.getKeyword());
    }

    //진위 여부 후 돌아온 html 응답을 파라미터로 넘겨줌
    private List<ReceiptDto> proceedNext(String html, Long clubId, String keyword) throws ParseException {
        StudentClub club = studentClubRepository.findById(clubId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_STUDENT_CLUB, 400));

        Document doc = Jsoup.parse(html);
        Elements rows = doc.select("table.table tbody tr:has(td)");

        List<Receipt> receipts = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Element row : rows) {
            Elements tds = row.select("td");
            if (tds.size() < 5) continue;

            Date parsedDate = fmt.parse(tds.get(0).text());
            int amt = Integer.parseInt(tds.get(2).text().replace(",", ""));
            int deposit    = amt > 0 ? amt : 0;
            int withdrawal = amt < 0 ? Math.abs(amt) : 0;
            String content = tds.get(4).text();

            if(keyword != null && !keyword.trim().isEmpty()) {
                content = "[" + keyword + "]" + " " + content;
            }

            receipts.add(Receipt.builder()
                .date(parsedDate)
                .content(content)
                .deposit(deposit)
                .withdrawal(withdrawal)
                .studentClub(club)
                .verification(true)
                .build()
            );

            //잔액 업데이트
            int balanceAdjustment = deposit - withdrawal;
            club.setBalance(club.getBalance() + balanceAdjustment);
        }
        receiptRepository.saveAll(receipts);
        studentClubRepository.save(club);

        receiptService.checkAndUpdateVerificationStatus(clubId);

        return receipts.stream()
                .map(mapper :: toReceiptDto)
                .collect(Collectors.toList());
    }

    private Workbook openExcelFile(MultipartFile file, String password) {
        InputStream inputStream = null;

        try {
            inputStream = file.getInputStream();

            //암호가 걸려있는 경우
            if (password != null && !password.trim().isEmpty()) {
                try {
                    return WorkbookFactory.create(inputStream, password);
                } catch (EncryptedDocumentException e) {
                    throw new CustomException(INVALID_PASSWORD, 400);
                } catch (Exception e) {
                    throw new CustomException(EXCEL_OPEN_FAILED, 500);
                }
            }

            //암호가 없는 경우
            try {
                return WorkbookFactory.create(inputStream);
            } catch (EncryptedDocumentException e) {
                throw new CustomException(PASSWORD_REQUIRED, 400);
            } catch (Exception e) {
                throw new CustomException(EXCEL_OPEN_FAILED, 500);
            }

        } catch (CustomException e) {
            throw e;
        } catch (IOException e) {
            throw new CustomException(EMPTY_FILE, 400);
        } finally {
        }
    }

    @Transactional
    public List<ReceiptDto> loadDataFromExcel(MultipartFile file, String excelPassword, String keyword, String userId, UserDetails currentUser) {
        if (!validateExcelFile(file)) {
            throw new CustomException(EMPTY_FILE, 400);
        }
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));
        StudentClub studentClub = user.getStudentClub();

        //Workbook = 파일 자체를 워크북 객체로 반환
        Workbook workbook = null;
        List<Receipt> receipts = new ArrayList<>();

        try {
            workbook = openExcelFile(file, excelPassword);
            //Sheet = 엑셀 파일 시트
            Sheet sheet = workbook.getSheetAt(0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

            //9행까지는 헤더나 다른 정보라서 10행부터 파싱
            for (int i = 9; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                //cell = column
                Cell dateCell = row.getCell(1);
                Cell contentCell = row.getCell(2);
                Cell amountCell = row.getCell(6);

                if (dateCell == null || contentCell == null || amountCell == null) {
                    continue;
                }

                try {
                    String dateTimeStr = getCellValueAsString(dateCell);
                    if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
                        continue;
                    }
                    String dateOnly = dateTimeStr.split(" ")[0];
                    Date date = dateFormat.parse(dateOnly);

                    String content = getCellValueAsString(contentCell);
                    if (content == null || content.trim().isEmpty()) {
                        continue;
                    }

                    //동일하게 키워드 추가
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        content = "[" + keyword + "]" + " " + content;
                    }

                    int amount = getCellValueAsInteger(amountCell);
                    //입금 출금 구분
                    int deposit = amount > 0 ? amount : 0;
                    int withdrawal = amount < 0 ? Math.abs(amount) : 0;

                    if (receiptRepository.existsByDateAndContent(date, content)) {
                        continue;
                    }

                    Receipt receipt = Receipt.builder()
                            .date(date)
                            .content(content)
                            .deposit(deposit)
                            .withdrawal(withdrawal)
                            .verification(false)
                            .studentClub(studentClub)
                            .build();

                    receiptRepository.save(receipt);
                    receipts.add(receipt);

                    //잔액 계산
                    int balanceAdjustment = deposit - withdrawal;
                    studentClub.setBalance(studentClub.getBalance() + balanceAdjustment);

                } catch (ParseException e) {
                    continue;
                }
            }

            studentClubRepository.save(studentClub);

            receiptService.checkAndUpdateVerificationStatus(studentClub.getId());

        } catch (Exception e) {
            throw new CustomException(EXCEL_OPEN_FAILED, 500);
        }

        return receipts.stream()
                .map(mapper :: toReceiptDto)
                .collect(Collectors.toList());
    }

    //엑셀 파일 검증
    private boolean validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase();
        return lower.endsWith(".xlsx") || lower.endsWith(".xls");
    }

    //엑셀 문자열 추출
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                    return sdf.format(cell.getDateCellValue());
                }
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    //엑셀 숫자 추출
    private int getCellValueAsInteger(Cell cell) {
        if (cell == null) return 0;

        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                String str = cell.getStringCellValue().replace(",", "").trim();
                try {
                    return Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    return 0;
                }
            default:
                return 0;
        }
    }

    public boolean validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;
        String name = file.getOriginalFilename();
        return name != null && name.toLowerCase().endsWith(".pdf");
    }

    private String extractValue(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return (m.find() ? m.group(1) : "NOT_FOUND");
    }

    private void checkClub(StudentClub club,
        UserDetails currentUser) {
        User u = userRepository.findByUserId(currentUser.getUsername())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));
        if (!club.equals(u.getStudentClub())) {
            throw new CustomException(NO_AUTHORIZATION_BELONGING, 403);
        }
    }
}
