package com.example.tomyongji.receipt.service;

import static com.example.tomyongji.validation.ErrorMsg.EMPTY_FILE;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_BELONGING;
import static com.example.tomyongji.validation.ErrorMsg.PARSING_ERROR;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.dto.BreakDownDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BreakDownService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ReceiptRepository receiptRepository;
    private final StudentClubRepository studentClubRepository;

    /**
     * 1) PDF에서 발급일자·번호 파싱
     *    + 소속 권한 검사
     */
    public BreakDownDto parsePdf(MultipartFile file,
        UserDetails currentUser) throws Exception {
        User user = userRepository.findByUserId(currentUser.getUsername())
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

            // 1) 한글 포맷으로 뽑아낸 날짜
            String korDate = extractValue(text,
                "발급일자\\s+(\\d{4}년\\s+\\d{2}월\\s+\\d{2}일)");
            // 2) Date 객체로 변환 (한글 포맷에 맞춰 파싱)
            Date parsed = new SimpleDateFormat("yyyy년 MM월 dd일")
                .parse(korDate);
            // 3) ISO 포맷 문자열로 재포맷팅
            String isoDate = new SimpleDateFormat("yyyy-MM-dd")
                .format(parsed);

            String issueNumber = extractValue(text,
                "발급번호\\s+([A-Z0-9\\-]+)");

            return BreakDownDto.builder()
                .issueDate(isoDate)
                .issueNumber(issueNumber)
                .studentClubId(club.getId())
                .build();

        } catch (Exception e) {
            throw new CustomException(PARSING_ERROR, 500);
        }
    }

    /**
     * 2) 외부 API 호출 + DB 저장을 하나의 트랜잭션으로 묶음
     */
    @Transactional(rollbackFor = Exception.class)
    public void fetchAndProcessDocument(BreakDownDto dto) throws ParseException {
        String url = "https://api.tossbank.com/api-public/document/view/{date}/{docId}";
        ResponseEntity<String> resp = restTemplate.getForEntity(
            url, String.class,
            dto.getIssueDate(), dto.getIssueNumber()
        );
        proceedNext(resp.getBody(), dto.getStudentClubId());
    }

    /**
     * 3) HTML 파싱 → Receipt 저장
     */
    private void proceedNext(String html, Long clubId) throws ParseException {
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
            int amt = Integer.parseInt(
                tds.get(2).text().replace(",", "")
            );
            int deposit    = amt > 0 ? amt : 0;
            int withdrawal = amt < 0 ? Math.abs(amt) : 0;
            String content = tds.get(4).text();

            receipts.add(Receipt.builder()
                .date(parsedDate)
                .content(content)
                .deposit(deposit)
                .withdrawal(withdrawal)
                .studentClub(club)
                .verification(true)
                .build()
            );
        }
        receiptRepository.saveAll(receipts);
    }

    /** PDF 파일 검사 */
    public boolean validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;
        String name = file.getOriginalFilename();
        return name != null && name.toLowerCase().endsWith(".pdf");
    }

    /** 정규식으로 그룹 1 추출 */
    private String extractValue(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return (m.find() ? m.group(1) : "NOT_FOUND");
    }

    /** 동아리 소속 권한 검사 */
    private void checkClub(StudentClub club,
        UserDetails currentUser) {
        User u = userRepository.findByUserId(currentUser.getUsername())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));
        if (!club.equals(u.getStudentClub())) {
            throw new CustomException(NO_AUTHORIZATION_BELONGING, 403);
        }
    }
}
