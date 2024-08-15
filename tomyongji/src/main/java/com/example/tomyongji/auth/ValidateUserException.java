package com.example.tomyongji.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ValidateUserException extends Throwable {
    public ValidateUserException(@NotBlank(message = "이메일은 필수 입력값입니다") @Email(message = "이메일 형식에 맞지 않습니다.") String s) {

    }
}
