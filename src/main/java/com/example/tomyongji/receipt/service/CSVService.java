package com.example.tomyongji.receipt.service;

import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CSVService {
    private ReceiptRepository receiptRepository;
    private UserRepository userRepository;
    private static final Logger LOGGER = Logger.getLogger(ReceiptService.class.getName());

    @Autowired
    public CSVService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void ReceiptService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    @Transactional
    public List<Receipt> loadDataFromCSV(MultipartFile file, long userIndexId) {
        List<Receipt> receipts = new ArrayList<>();
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
                        StudentClub studentClub = userRepository.findById(userIndexId).get().getStudentClub();
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
}
