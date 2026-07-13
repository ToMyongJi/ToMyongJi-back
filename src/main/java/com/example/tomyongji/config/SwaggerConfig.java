package com.example.tomyongji.config;

import com.example.tomyongji.global.annotation.ApiErrorExample;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH = "BearerAuth";

    @Bean
    public OperationCustomizer apiErrorExamplesCustomizer() {
        return (operation, handlerMethod) -> {
            ApiErrorExample[] errorExamples = handlerMethod.getMethod()
                .getAnnotationsByType(ApiErrorExample.class);

            if (errorExamples.length == 0) return operation;

            if (operation.getResponses() == null) {
                operation.setResponses(new ApiResponses());
            }

            Map<Integer, List<ApiErrorExample>> grouped = Arrays.stream(errorExamples)
                .collect(Collectors.groupingBy(ApiErrorExample::status));

            grouped.forEach((status, examples) -> {
                MediaType mediaType = new MediaType();
                examples.forEach(ex -> {
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("statusCode", ex.status());
                    body.put("message", ex.message());
                    body.put("data", null);
                    mediaType.addExamples(ex.message(), new Example().value(body));
                });

                Content content = new Content().addMediaType("application/json", mediaType);
                ApiResponse apiResponse = new ApiResponse()
                    .description(resolveDescription(status))
                    .content(content);

                operation.getResponses().addApiResponse(String.valueOf(status), apiResponse);
            });

            return operation;
        };
    }

    private String resolveDescription(int status) {
        try {
            return HttpStatus.valueOf(status).getReasonPhrase();
        } catch (IllegalArgumentException e) {
            return String.valueOf(status);
        }
    }

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
            .title("투명지 API")
            .version("v1.0.0")
            .description("투명지 API 입니다");

        SecurityScheme bearerAuth = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
            .addList(BEARER_AUTH);

        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes(BEARER_AUTH, bearerAuth))
            .addSecurityItem(securityRequirement)
            .addServersItem(new Server().url("/"))
            .info(info);
    }
}