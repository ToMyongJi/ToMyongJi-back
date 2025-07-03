package com.example.tomyongji.receipt.service;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.dto.BreakDownDto;
import com.example.tomyongji.receipt.entity.BreakDown;
import com.example.tomyongji.receipt.entity.TempReceipt;
import com.example.tomyongji.receipt.mapper.BreakDownMapper;
import com.example.tomyongji.receipt.repository.BreakDownRepository;
import com.example.tomyongji.receipt.repository.TempReceiptRepository;
import com.example.tomyongji.validation.CustomException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.tomyongji.validation.ErrorMsg.*;

@Service
@RequiredArgsConstructor

public class BreakDownService {

    private final UserRepository userRepository;
    private final BreakDownRepository breakDownRepository;
    private final BreakDownMapper breakDownMapper;
    private final TempReceiptRepository tempReceiptRepository;

    public BreakDownDto parsePdf(MultipartFile file, UserDetails currentUser) throws Exception {

        User user = userRepository.findByUserId(currentUser.getUsername())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));

        if (user.getStudentClub() == null) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }

        if (!validatePdfFile(file)) {
            throw new CustomException(EMPTY_FILE, 400);
        }

        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            String text = new PDFTextStripper().getText(document);

            String issueDateStr = extractValue(text, "발급일자\\s+(\\d{4}년\\s+\\d{2}월\\s+\\d{2}일)");
            String issueNumber = extractValue(text, "발급번호\\s+([A-Z0-9\\-]+)");

            Date issueDate = parseDate(issueDateStr);

            BreakDown breakDown = new BreakDown();
            breakDown.setIssueDate(issueDate);
            breakDown.setIssueNumber(issueNumber);
            breakDown.setStudentClub(user.getStudentClub());

            BreakDown savedBreakDown = breakDownRepository.save(breakDown);

            this.extractTransactions(text, savedBreakDown);

            return breakDownMapper.toBreakDownDto(savedBreakDown);

        } catch (Exception e) {
            throw new CustomException(PARSING_ERROR, 500);
        }
    }

    private void extractTransactions(String text, BreakDown breakDown) {
        Pattern transPattern = Pattern.compile(
                "(\\d{4}-\\d{2}-\\d{2})\\s+" +          // 날짜
                        "(\\d{2}:\\d{2}:\\d{2})\\s+" +  // 시간
                        "(\\S+)\\s+" +                  // 거래구분
                        "(-?[\\d,]+)\\s+" +             // 거래금액
                        "([\\d,]+)\\s+" +               // 잔액
                        "(.+)"                          // 내용
        );

        Matcher matcher = transPattern.matcher(text);

        while (matcher.find()) {
            try {
                String dateStr = matcher.group(1);
                String timeStr = matcher.group(2);
                String transType = matcher.group(3);
                String amountStr = matcher.group(4).replace(",", "");
                String balanceStr = matcher.group(5);
                String description = matcher.group(6).trim();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = dateFormat.parse(dateStr);
                int amount = Integer.parseInt(amountStr);

                TempReceipt tempReceipt = new TempReceipt(date, amount, description);
                tempReceipt.setBreakDown(breakDown);
                tempReceiptRepository.save(tempReceipt);

            } catch (Exception e) {
                throw new CustomException(PARSING_TRANSACTION_ERROR, 500);
            }
        }
    }

    private Date parseDate(String Date) throws Exception {
        if ("NOT_FOUND".equals(Date)) {
            throw new CustomException(PARSING_ERROR, 500);
        }

        String convertedDate = Date
                .replaceAll("년\\s*", "-")
                .replaceAll("월\\s*", "-")
                .replaceAll("일", "");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.parse(convertedDate);
    }

    private String extractValue(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String result = matcher.group(1);
            return result;
        } else {
            return "NOT_FOUND";
        }
    }

    public boolean validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            return false;
        }
        return true;
    }
}