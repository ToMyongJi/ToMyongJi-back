package com.example.tomyongji.global.error;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    int errorCode;

    public CustomException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
