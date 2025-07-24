package com.example.tomyongji.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static com.example.tomyongji.validation.ErrorMsg.EMPTY_BODY;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {
        // 1) 필터 단계에서 요청·응답을 모두 래핑
        ContentCachingRequestWrapper  wrappedReq  = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedRes  = new ContentCachingResponseWrapper(response);

        // 2) 체인 진행
        filterChain.doFilter(wrappedReq, wrappedRes);

        // 3) 로깅
        logRequest(wrappedReq);
        logResponse(wrappedRes);

        // 4) 래퍼에 저장된 응답 바디를 실제 출력 스트림으로 복사
        wrappedRes.copyBodyToResponse();
    }

    private void logRequest(ContentCachingRequestWrapper req) {
        byte[] buf = req.getContentAsByteArray();
        String payload;
        try {
            payload = buf.length > 0
                ? new String(buf, 0, buf.length, req.getCharacterEncoding())
                : EMPTY_BODY;
        } catch (UnsupportedEncodingException e) {
            payload = "[unknown]";
        }
        log.info("→ 요청: [{}] {}{}  바디: {}",
            req.getMethod(),
            req.getRequestURI(),
            (req.getQueryString() != null ? "?" + req.getQueryString() : ""),
            payload
        );
    }

    private void logResponse(ContentCachingResponseWrapper res) {
        byte[] buf = res.getContentAsByteArray();
        String payload;
        try {
            payload = buf.length > 0
                ? new String(buf, 0, buf.length, res.getCharacterEncoding())
                : EMPTY_BODY;
        } catch (UnsupportedEncodingException e) {
            payload = "[unknown]";
        }
        log.info("← 응답: 상태={}  바디: {}",
            res.getStatus(),
            payload
        );
    }
}
