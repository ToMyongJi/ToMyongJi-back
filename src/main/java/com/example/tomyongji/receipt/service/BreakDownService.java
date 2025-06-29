package com.example.tomyongji.receipt.service;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.dto.BreakDownDto;
import com.example.tomyongji.receipt.entity.BreakDown;
import com.example.tomyongji.validation.CustomException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.tomyongji.validation.ErrorMsg.*;

@Service
@RequiredArgsConstructor
public class BreakDownService {

    private final UserRepository userRepository;

    public BreakDownDto.PdfParseResult parsePdf(MultipartFile file, UserDetails currentUser) throws Exception {

        User user = userRepository.findByUserId(currentUser.getUsername())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));

        if (user.getStudentClub() == null) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }

        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            String text = new PDFTextStripper().getText(document);

            String issueDate = extractValue(text, "발급일자\\s+(\\d{4}년 \\d{2}월 \\d{2}일)");
            String issueNumber = extractValue(text, "발급번호\\s+([A-Z0-9\\-]+)");

            List<BreakDown> transactions = extractTransactions(text);

            BreakDownDto.PdfParseResult result = new BreakDownDto.PdfParseResult();
            result.setIssueDate(issueDate);
            result.setIssueNumber(issueNumber);
            result.setTransactions(transactions);
            result.setStudentClubName(user.getStudentClub().getStudentClubName());

            return result;

        }
    }

    private List<BreakDown> extractTransactions(String text) {
        List<BreakDown> transactions = new ArrayList<>();

        //거래 구분이나 이런건 캡쳐 안해도됨. 일단 해두는 이유 -> 추후에 사용할 수 있기에
        Pattern transPattern = Pattern.compile(
                "(\\d{4}-\\d{2}-\\d{2})\\s+" +                  // 날짜
                        "([\\d:]+)\\s+" +                       // 시간 X
                        "(\\S+)\\s+" +                          // 거래구분 X
                        "(-?[\\d,]+)\\s+" +                     // 거래금액
                        "([\\d,]+)\\s+" +                       // 거래후잔액 X
                        "(.+)"                                  // 거래내용
        );

        Matcher matcher = transPattern.matcher(text);

        while (matcher.find()) {
            try {
                String date = matcher.group(1);
                String amountStr = matcher.group(4);
                String description = matcher.group(6).trim();

                BreakDown transaction = new BreakDown(date, amountStr, description);
                transactions.add(transaction);
            } catch (Exception e) {
            }
        }
        return transactions;
    }

    private String extractValue(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : "NOT_FOUND";
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