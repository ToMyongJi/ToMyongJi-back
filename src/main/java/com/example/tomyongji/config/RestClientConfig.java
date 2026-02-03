package com.example.tomyongji.config;

import static com.example.tomyongji.global.error.ErrorMsg.AUTENTICITY_FAILURE;
import static com.example.tomyongji.global.error.ErrorMsg.EXTERNAL_SERVER_ERROR;

import com.example.tomyongji.global.error.CustomException;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return builder
            .requestFactory(requestFactory)
            .defaultStatusHandler(status -> status.is4xxClientError(), (request, response) -> {
                throw new CustomException(AUTENTICITY_FAILURE, response.getStatusCode().value());
            })
            .defaultStatusHandler(status -> status.is5xxServerError(), (request, response) -> {
                throw new CustomException(EXTERNAL_SERVER_ERROR, response.getStatusCode().value());
            })
            .build();
    }
}
