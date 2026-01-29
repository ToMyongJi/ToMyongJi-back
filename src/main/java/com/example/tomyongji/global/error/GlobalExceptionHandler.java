package com.example.tomyongji.global.error;

import com.example.tomyongji.global.common.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 공통 응답 생성 메서드
    private ResponseEntity<ApiResponse<Void>> createErrorResponse(HttpStatus status, String message) {
        return ResponseEntity
            .status(status)
            .body(ApiResponse.onFailure(status.value(), message));
    }

    // 400 Bad Request 시리즈
    @ExceptionHandler({IllegalArgumentException.class, MultipartException.class, RuntimeException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequestExceptions(Exception ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 비즈니스 커스텀 예외
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        // CustomException의 에러 코드가 정수형(int)이면 그대로 사용
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.onFailure(ex.getErrorCode(), ex.getMessage()));
    }

    // DB 제약 조건 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "중복된 데이터가 존재합니다.");
    }

    // 404 Not Found
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoResourceFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}
