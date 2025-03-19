package com.example.tomyongji.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import static com.example.tomyongji.validation.ErrorMsg.EMPTY_BODY;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_INCODING;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        logger.info("-> 요청: [{}] {}{}",
                requestWrapper.getMethod(),
                requestWrapper.getRequestURI(),
                getQueryParams(request)
        );
        return true;
    }

    private String getQueryParams(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return (queryString != null) ? "?" + queryString : "";
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request;
        ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) response;

        logger.info("<- 응답: [{}] {} - 상태 코드: {}",
                requestWrapper.getMethod(),
                requestWrapper.getRequestURI(),
                responseWrapper.getStatus()
        );

        logger.info("응답 바디: {}", getResponseBody(responseWrapper));

        try {
            responseWrapper.copyBodyToResponse(); // 응답 내용을 클라이언트에 전달해야 함
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String getResponseBody(ContentCachingResponseWrapper response) {
        try {
            byte[] content = response.getContentAsByteArray();
            return (content.length > 0) ? new String(content, response.getCharacterEncoding()) : EMPTY_BODY;
        } catch (UnsupportedEncodingException e) {
            return NOT_FOUND_INCODING;
        }
    }
    private String getHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.append(headerName).append(": ").append(request.getHeader(headerName)).append("; ");
        }
        return headers.toString();
    }

}
