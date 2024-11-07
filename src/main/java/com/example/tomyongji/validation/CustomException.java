package com.example.tomyongji.validation;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    int errorCode;

    public CustomException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
