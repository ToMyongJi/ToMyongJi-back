package com.example.tomyongji.validation;

import com.example.tomyongji.validation.CustomException;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import static com.example.tomyongji.validation.ErrorMsg.AUTENTICITY_FAILURE;
import static com.example.tomyongji.validation.ErrorMsg.EXTERNAL_SERVER_ERROR;

public class CustomResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus status = (HttpStatus) response.getStatusCode();

        // 4xx 클라이언트 에러 처리
        if (status.is4xxClientError()) {
            throw new CustomException(AUTENTICITY_FAILURE, status.value());
        }

        // 5xx 서버 에러 처리
        if (status.is5xxServerError()) {
            throw new CustomException(EXTERNAL_SERVER_ERROR, status.value());
        }

        // 그 외는 기본 핸들러 동작 유지
        super.handleError(response);
    }
}

