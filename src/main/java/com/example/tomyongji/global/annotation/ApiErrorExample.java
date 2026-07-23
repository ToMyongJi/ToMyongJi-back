package com.example.tomyongji.global.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiErrorExamples.class)
public @interface ApiErrorExample {
    int status() default 400;
    String message();
}
