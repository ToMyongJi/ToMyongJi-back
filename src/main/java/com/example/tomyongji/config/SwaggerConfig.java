package com.example.tomyongji.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger springdoc-ui 구성 파일
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
            .title("투명지 API")
            .version("v1.0.0")
            .description("투명지 API 입니다");

        return new OpenAPI()
            .components(new Components())
            // 여기서 servers에 "/"를 넣어주면 Swagger UI가
            // https://<호스트>/swagger-ui 로부터 상대경로로 API 요청을 보냅니다.
            .addServersItem(new Server().url("/"))
            .info(info);
    }
}