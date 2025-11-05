package com.example.tomyongji.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
// 1. kv와 JsonNode를 import
import static net.logstash.logback.argument.StructuredArguments.kv;

@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);
    private final ObjectMapper objectMapper;

    public AuditLogAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(com.example.tomyongji.logging.AuditLog)")
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {

        // 메서드 정보
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 메서드에 붙은 @AuditLog 어노테이션 정보
        AuditLog auditLog = method.getAnnotation(AuditLog.class);

        if (auditLog == null) {
            return joinPoint.proceed();
        }

        // 어노테이션에서 "action" 값 꺼내기
        String action = auditLog.action();
        Object[] args = joinPoint.getArgs();
        String methodName = method.getName();

        try {
            // 2. String이 아닌 JsonNode(JSON 객체)로 변환
            JsonNode argsAsJsonNode = objectMapper.valueToTree(args);

            // 3. kv를 사용해 구조화된 로그 전달
            log.info("[Audit Action] Method called",
                kv("action", action),       // 1. JSON_FILE은 필드로 사용
                kv("method", methodName),   // 2. JSON_FILE은 필드로 사용
                kv("args", argsAsJsonNode)  // 3. JSON_FILE은 필드로 사용
            );

        } catch (Exception e) { // JsonProcessingException 포함 더 넓은 범위의 예외 처리
            log.warn("[Audit Log] Failed to serialize arguments for method {}", methodName, e);
            // JSON 변환 실패 시 그냥 기본 toString() 사용
            log.info("[Audit Action: {}] Method {} called with arguments - {}",
                action, methodName, Arrays.toString(args));
        }

        Object result = joinPoint.proceed(); // ◀ 실제 메소드 실행

        try {
            // 2. String이 아닌 JsonNode(JSON 객체)로 변환
            JsonNode resultAsJsonNode = objectMapper.valueToTree(result);

            // 3. kv를 사용해 구조화된 로그 전달
            log.info("[Audit Action: {}] Method {} executed",
                action,
                methodName,
                kv("action", action),
                kv("method", methodName),
                kv("result", resultAsJsonNode) // "result" 필드를 JSON 객체로 추가
            );

        } catch (Exception e) {
            log.warn("[Audit Log] Failed to serialize result for method {}", methodName, e);
            // JSON 변환 실패 시 그냥 기본 toString() 사용
            log.info("[Audit Action: {}] Method {} executed with result - {}",
                action, methodName, (result != null ? result.toString() : "null"));
        }
        return result;
    }
}