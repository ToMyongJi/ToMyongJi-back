package com.example.tomyongji.global.error;

import lombok.Data;

@Data
public class ErrorResponse {
    private int statusCode;
    private String message;

    public ErrorResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    // 정적 팩토리 메서드 추가
    public static ErrorResponse of(int statusCode, String message) {
        return new ErrorResponse(statusCode, message);
    }
}
