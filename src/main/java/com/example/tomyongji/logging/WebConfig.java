package com.example.tomyongji.logging;

import com.example.tomyongji.logging.LoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

    public WebConfig(LoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 모든 요청에 대해 LoggingInterceptor를 적용
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**"); // 모든 경로에 적용
    }
}
