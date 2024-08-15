package com.example.tomyongji.auth.constant;

public class JwtConstant {
    public static final long ACCESS_TOKEN_EXPIRE_TIME = 3600000; // 1시간 (60분 * 60초 * 1000밀리초)
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 604800000; // 7일 (7일 * 24시간 * 60분 * 60초 * 1000밀리초)
    public static final String GRANT_TYPE = "Bearer";
}
