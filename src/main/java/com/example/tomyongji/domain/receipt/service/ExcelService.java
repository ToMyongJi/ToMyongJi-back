package com.example.tomyongji.domain.receipt.service;

import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_USER;
import static com.example.tomyongji.global.error.ErrorMsg.NO_AUTHORIZATION_BELONGING;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.dto.ExcelExportDto;
import com.example.tomyongji.domain.receipt.entity.Receipt;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.repository.ReceiptRepository;
import com.example.tomyongji.global.error.CustomException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final ReceiptRepository receiptRepository;
    private final UserRepository userRepository;

    public void writeExcel(HttpServletResponse response, ExcelExportDto dto, UserDetails currentUser) {
        String userId = dto.getUserId();
        int year = dto.getYear();
        int month = dto.getMonth();

        StudentClub studentClub = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER, 400))
                .getStudentClub();

        checkClub(studentClub, currentUser);

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date endDate = cal.getTime();

        List<Receipt> receipts = receiptRepository.findByStudentClubAndDateBetween(studentClub, startDate, endDate);

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "receipts_" + studentClub.getStudentClubName() + "_" + year + "_" + month + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

            List<List<String>> head = Arrays.asList(
                    Arrays.asList("date"),
                    Arrays.asList("content"),
                    Arrays.asList("deposit"),
                    Arrays.asList("withdrawal")
            );

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            List<List<Object>> data = new ArrayList<>();
            for (Receipt receipt : receipts) {
                data.add(Arrays.<Object>asList(
                        sdf.format(receipt.getDate()),
                        receipt.getContent(),
                        receipt.getDeposit(),
                        receipt.getWithdrawal()
                ));
            }

            WriteFont font = new WriteFont();
            font.setFontName("맑은 고딕");

            WriteCellStyle headStyle = new WriteCellStyle();
            headStyle.setWriteFont(font);

            WriteCellStyle contentStyle = new WriteCellStyle();
            contentStyle.setWriteFont(font);

            EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .registerWriteHandler(new HorizontalCellStyleStrategy(headStyle, contentStyle))
                    .autoCloseStream(false)
                    .sheet("영수증")
                    .doWrite(data);

        } catch (IOException e) {
            throw new RuntimeException("Excel export 실패", e);
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
