package com.example.tomyongji.global.error;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

public class CustomResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus status = (HttpStatus) response.getStatusCode();

        // 4xx 클라이언트 에러 처리
        if (status.is4xxClientError()) {
            throw new CustomException(ErrorMsg.AUTENTICITY_FAILURE, status.value());
        }

        // 5xx 서버 에러 처리
        if (status.is5xxServerError()) {
            throw new CustomException(ErrorMsg.EXTERNAL_SERVER_ERROR, status.value());
        }

        // 그 외는 기본 핸들러 동작 유지
        super.handleError(response);
    }
}

