package com.example.tomyongji.config;
import com.example.tomyongji.auth.jwt.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                // REST API이므로 basic auth 및 csrf 보안을 사용하지 않음
                .httpBasic(httpBasic -> httpBasic.disable())
                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())
                // Form Login 비활성화
                .formLogin(formLogin -> formLogin.disable())
                // JWT를 사용하기 때문에 세션을 사용하지 않음

                .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                    // actuator 헬스체크와 Prometheus 엔드포인트는 인증 없이 허용
                        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                        // 해당 API에 대해서는 모든 요청을 허가
                        .requestMatchers("/api/users/**","/swagger-ui/**", "/v3/api-docs/**","/api/csv/**","/api/club/**","/api/collegesAndClubs").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/receipt").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/receipt").hasAnyRole("STU","PRESIDENT","ADMIN")
                        .requestMatchers(HttpMethod.PATCH,"/api/receipt").hasAnyRole("STU","PRESIDENT","ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/status").permitAll()
                        //breakDown 권한 설정
                        .requestMatchers(HttpMethod.POST,"/api/breakdown/**").hasAnyRole("STU","PRESIDENT","ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/receipt").hasAnyRole("STU","PRESIDENT","ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/receipt/{receiptId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/receipt/club/{clubId}/student").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/receipt/club/{userId}").hasAnyRole("STU","PRESIDENT","ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/api/receipt/{receiptId}").hasAnyRole("STU","PRESIDENT","ADMIN")
                        .requestMatchers("/api/csv/upload/{userIndexId}","/api/ocr/upload/{userId}","/api/my/{id}").hasAnyRole("STU","PRESIDENT","ADMIN")
                        .requestMatchers("/api/my/members","/api/my/members/{id}","/api/my/members/{deletedStudentNum}").hasAnyRole("PRESIDENT","ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll())
//                        .anyRequest().permitAll())
                // JWT 인증을 위하여 직접 구현한 필터를 UsernamePasswordAuthenticationFilter 전에 실행
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 통합
                .build();
    }
    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // 모든 도메인 허용
        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 쿠키 허용
        configuration.setMaxAge(3600L); // preflight 요청 캐시 시간 설정

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 설정 적용
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
