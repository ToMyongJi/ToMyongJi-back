package com.example.tomyongji.receipt.entity;

import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.validation.CustomException;
import lombok.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.PARSING_ERROR;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BreakDown {
    private String date;
    private String amount;
    private String contents;

    public Receipt toReceipt(StudentClub studentClub) {
        try {
            Date parsedDate = parseDate(this.date);

            int parsedAmount = parseAmount(this.amount);

            Receipt.ReceiptBuilder receiptBuilder = Receipt.builder()
                    .date(parsedDate)
                    .content(this.contents)
                    .studentClub(studentClub);

            if (parsedAmount >= 0) {
                receiptBuilder.deposit(parsedAmount).withdrawal(0);
            } else {
                receiptBuilder.deposit(0).withdrawal(Math.abs(parsedAmount));
            }

            return receiptBuilder.build();

        } catch (Exception e) {
            throw new CustomException(PARSING_ERROR, 400);
        }
    }

    private Date parseDate(String dateStr) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date parsedDate = formatter.parse(dateStr);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parsedDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private int parseAmount(String amountStr) {
        return Integer.parseInt(amountStr.replace(",", ""));
    }

    public boolean isDeposit() {
        return parseAmount(this.amount) >= 0;
    }

    public boolean isWithdrawal() {
        return parseAmount(this.amount) < 0;
    }

    public int getAbsoluteAmount() {
        return Math.abs(parseAmount(this.amount));
    }
}