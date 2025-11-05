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
import org.springframework.security.core.userdetails.UserDetails;
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

        Object[] sanitizedArgs = sanitizeArguments(args);

        try {
            JsonNode argsAsJsonNode = objectMapper.valueToTree(sanitizedArgs);
            log.info("[Audit] Method called",
                new Object[]{
                    kv("action", action),
                    kv("method", methodName),
                    kv("args", argsAsJsonNode)
                }
            );
        } catch (Exception e) {
            log.warn("[Audit] Failed to serialize arguments for method {}", methodName, e);
            log.info("[Audit] Method called (fallback)",
                new Object[]{
                    kv("action", action),
                    kv("method", methodName),
                    kv("args", Arrays.toString(args))
                }
            );
        }

        Object result = joinPoint.proceed(); // ◀ 실제 메소드 실행

        try {
            JsonNode resultAsJsonNode = objectMapper.valueToTree(result);
            log.info("[Audit] Method executed",
                new Object[]{
                    kv("action", action),
                    kv("method", methodName),
                    kv("result", resultAsJsonNode)
                }
            );
        } catch (Exception e) {
            log.warn("[Audit] Failed to serialize result for method {}", methodName, e);
            log.info("[Audit] Method executed (fallback)",
                new Object[]{
                    kv("action", action),
                    kv("method", methodName),
                    kv("result", result != null ? result.toString() : "null")
                }
            );
        }
        return result;
    }
    private Object[] sanitizeArguments(Object[] args) {
        if (args == null) {
            return new Object[0];
        }

        Object[] sanitized = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof UserDetails userDetails) {
                // UserDetails 대신 username만 기록
                sanitized[i] = "UserDetails(username=" + userDetails.getUsername() + ")";
            } else {
                sanitized[i] = args[i];
            }
        }
        return sanitized;
    }

}