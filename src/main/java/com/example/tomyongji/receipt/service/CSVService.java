package com.example.tomyongji.receipt.service;

import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.repository.ReceiptRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class CSVService {

    private final ReceiptRepository receiptRepository;

    public CSVService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    @Transactional
    public List<Receipt> loadDataFromCSV(MultipartFile file) {
        List<Receipt> receipts = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            csvReader.readNext(); // 헤더 줄 건너뜀
            String[] nextRecord;

            while ((nextRecord = csvReader.readNext()) != null) {
                Date date = parseDate(nextRecord[0]);
                String content = nextRecord[1];
                int deposit = Integer.parseInt(nextRecord[2]);
                int withdrawal = Integer.parseInt(nextRecord[3]);

                // 중복 확인
                boolean exists = receiptRepository.existsByDateAndContent(date, content);
                if (!exists) {
                    Receipt receipt = Receipt.builder()
                            .date(date)
                            .content(content)
                            .deposit(deposit)
                            .withdrawal(withdrawal)
                            .build();
                    receiptRepository.save(receipt);
                    receipts.add(receipt);
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return receipts;
    }

    private Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
