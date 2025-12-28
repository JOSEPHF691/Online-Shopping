package com.shop.onlineshopping.common;

import lombok.Data;

/**
 * 统一返回结果：以后不管给前端发什么，都必须用这个包起来
 */
@Data
public class Result<T> {
    private Integer code; // 1成功，0失败
    private String msg;   // 错误信息
    private T data;       // 具体数据

    // 成功（带数据）
    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<>();
        result.data = object;
        result.code = 1;
        return result;
    }

    // 成功（不带数据）
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 1;
        return result;
    }

    // 失败
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = 0;
        return result;
    }
}