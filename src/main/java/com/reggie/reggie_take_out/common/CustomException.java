package com.reggie.reggie_take_out.common;

/**
 * 自定义异常
 * RuntimeException 运行时异常
 */
public class CustomException extends RuntimeException{
    public CustomException(String message) {
        super(message);
    }
}
