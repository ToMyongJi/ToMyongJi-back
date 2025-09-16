package com.example.tomyongji.receipt.service;

import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_BELONGING;
import static com.example.tomyongji.validation.ErrorMsg.NO_AUTHORIZATION_USER;

import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.dto.CsvExportDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.example.tomyongji.validation.CustomException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Calendar;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class CSVService {
    private final ReceiptRepository receiptRepository;
    private final UserRepository userRepository;
    private static final Logger LOGGER = Logger.getLogger(ReceiptService.class.getName());

    @Transactional
    public List<Receipt> loadDataFromCSV(MultipartFile file, long userIndexId, UserDetails currentUser) {
        List<Receipt> receipts = new ArrayList<>();
        StudentClub studentClub = userRepository.findById(userIndexId).get().getStudentClub();

        checkClub(studentClub, currentUser); // userIndexId의 유저 소속 학생회와 현재 접속한 유저의 소속 학생회를 비교

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            csvReader.readNext(); // Skip header row
            String[] nextRecord;

            while ((nextRecord = csvReader.readNext()) != null) {
                Date date = parseDate(nextRecord[0]);
                String content = nextRecord[1];
                int deposit = parseInteger(nextRecord[2]);
                int withdrawal = parseInteger(nextRecord[3]);

                // 중복 확인
                if (date != null && !content.isEmpty() && deposit != -1 && withdrawal != -1) {
                    boolean exists = receiptRepository.existsByDateAndContent(date, content);
                    if (!exists) {
                        Receipt receipt = Receipt.builder()
                                .date(date)
                                .content(content)
                                .deposit(deposit)
                                .withdrawal(withdrawal)
                                .studentClub(studentClub)
                                .build();
                        if(withdrawal!=0&&deposit!=0){
                        }else if(withdrawal==0) {
                            studentClub.setBalance(studentClub.getBalance()+deposit);
                            receiptRepository.save(receipt);
                            receipts.add(receipt);
                        }else if(deposit==0){
                            studentClub.setBalance(studentClub.getBalance()-withdrawal);
                            receiptRepository.save(receipt);
                            receipts.add(receipt);

                        }
                    } else {
                        LOGGER.info("Duplicate found for date: " + date + ", content: " + content);
                    }
                } else {
                    LOGGER.warning("Invalid data skipped: date=" + date + ", content=" + content +
                            ", deposit=" + deposit + ", withdrawal=" + withdrawal);
                }
            }
        } catch (IOException | CsvValidationException e) {
            LOGGER.log(Level.SEVERE, "Error reading CSV file", e);
        }
        return receipts;
    }

    private Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            LOGGER.warning("Date parsing failed for: " + dateString);
            return null;
        }
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.warning("Integer parsing failed for: " + value);
            return -1;
        }
    }

    public void writeCsv(HttpServletResponse response, CsvExportDto csvExportDto, UserDetails currentUser) {
        String userId = csvExportDto.getUserId();
        int year = csvExportDto.getYear();
        int month = csvExportDto.getMonth();
        // 학생회 조회
        StudentClub studentClub = userRepository.findByUserId(userId).get().getStudentClub();

        checkClub(studentClub, currentUser); //csvExportDto의 UserId를 통해 얻은 유저의 소속 학생회와 현재 접속한 유저의 소속 학생회 비교

        try {
            // 지정 월의 시작일과 종료일 계산
            Calendar cal = Calendar.getInstance();
            cal.set(year, month - 1, 1, 0, 0, 0);
            Date startDate = cal.getTime();
            cal.add(Calendar.MONTH, 1);
            Date endDate = cal.getTime();

            // 해당 기간의 영수증 데이터 조회
            List<Receipt> receipts = receiptRepository.findByStudentClubAndDateBetween(studentClub, startDate, endDate);

            // CSV 파일 다운로드를 위한 응답 헤더 설정
            response.setContentType("text/csv");
            String fileName = "receipts_" + studentClub.getStudentClubName() + "_" + year + "_" + month + ".csv";
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);


            // CSVWriter를 이용해 CSV 데이터 작성
            PrintWriter writer = response.getWriter();
            CSVWriter csvWriter = new CSVWriter(writer);

            // CSV 헤더 작성
            String[] header = { "date", "content", "deposit", "withdrawal" };
            csvWriter.writeNext(header);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Receipt receipt : receipts) {
                String[] record = {
                    sdf.format(receipt.getDate()),
                    receipt.getContent(),
                    String.valueOf(receipt.getDeposit()),
                    String.valueOf(receipt.getWithdrawal())
                };
                csvWriter.writeNext(record);
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "CSV export 실패", e);
            // 예외를 내부에서 처리하고 RuntimeException으로 전환하여 상위로 전달할 수도 있습니다.
            throw new RuntimeException("CSV export 실패", e);
        }
    }

    private void checkClub(StudentClub studentClub, UserDetails currentUser) {
        User compareUser = userRepository.findByUserId(currentUser.getUsername())
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400));
        if (!studentClub.equals(compareUser.getStudentClub())) {
            throw new CustomException(NO_AUTHORIZATION_BELONGING, 400);
        }
    }
}
