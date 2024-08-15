package com.example.tomyongji.receipt.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CustomDateDeserializer extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
        String dateString = jsonParser.getText().trim();

        if (dateString.isEmpty()) {
            // 빈 문자열을 null로 처리
            return null;
        }

        SimpleDateFormat dateFormat;

        if (dateString.matches("\\d{8}")) {
            dateFormat = new SimpleDateFormat("yyyyMMdd");
        } else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }

        try {
            Date parsedDate = dateFormat.parse(dateString);

            // 시분초를 0으로 설정
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar.getTime();
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format: " + dateString);
        }
    }
}

