package com.example.tomyongji.global.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorExamples {
    ApiErrorExample[] value();
}
