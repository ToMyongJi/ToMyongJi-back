package com.example.tomyongji.global.common.response;


import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ApiResponse<T> {
    private int statusCode;
    private String message;
    private T data;

    public ApiResponse(int statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.message = statusMessage;
    }

    // 1. 성공 (200 OK)
    public static <T> ApiResponse<T> onSuccess(T data) {
        return new ApiResponse<>(200, "요청에 성공했습니다.", data);
    }

    // 2. 생성 (201 Created)
    public static <T> ApiResponse<T> onCreated(T data) {
        return new ApiResponse<>(201, "성공적으로 생성되었습니다.", data);
    }

    // 3. 실패 (에러 응답용) - ErrorResponse를 대체하는 핵심 메서드
    public static <T> ApiResponse<T> onFailure(int statusCode, String message) {
        return new ApiResponse<>(statusCode, message, null);
    }

    // 4. 실패 상황에서도 데이터를 넘겨야 할 때
    public static <T> ApiResponse<T> onFailure(int statusCode, String message, T data) {
        return new ApiResponse<>(statusCode, message, data);
    }


}
