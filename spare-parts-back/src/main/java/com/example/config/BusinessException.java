package com.example.config;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    // 可选：带原因的构造器
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}