package com.example.tomyongji.admin.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ApiResponse<T> {
    private int statusCode;
    private String statusMessage;
    private T data;

    public ApiResponse(int statusCode, String statusMessage, T data) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.data = data;
    }

}
