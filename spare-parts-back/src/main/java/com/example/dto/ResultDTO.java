package com.example.dto;


public class ResultDTO<T> {
    private int code;
    private String msg;
    private T data;

    // 私有构造函数
    private ResultDTO(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功响应（带数据）
    public static <T> ResultDTO<T> success(T data) {
        return new ResultDTO<>(200, "成功", data);
    }

    // 成功响应（无数据）
    public static <T> ResultDTO<T> success() {
        return new ResultDTO<>(200, "成功", null);
    }

    // 错误响应
    public static <T> ResultDTO<T> error(String msg) {
        return new ResultDTO<>(500, msg, null);
    }

    // 自定义状态码的错误响应
    public static <T> ResultDTO<T> error(int code, String msg) {
        return new ResultDTO<>(code, msg, null);
    }

    // Getter方法
    public int getCode() { return code; }
    public String getMsg() { return msg; }
    public T getData() { return data; }
}