package com.analysis.admin.Code;

import lombok.Data;

@Data
public class Response<T> {
    private int code;
    private String message;
    private T data;

    public Response(){
        this.code = ResultCode.SUCCESS.getCode();
        this.message = ResultCode.SUCCESS.getMessage();
    }
    public Response(int code, String message){
        this.code = code;
        this.message = message;
    }

    public Response(int code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Response(T data){
        this.code = ResultCode.SUCCESS.getCode();
        this.message = ResultCode.SUCCESS.getMessage();
        this.data = data;
    }



    public static <T> Response<T> success(){
        return new Response<>();
    }

    public static <T> Response<T> success(T data){
        return new Response<>(data);
    }

    public static <T> Response<T> failed(){
        return new Response<>(
            ResultCode.FAIL.getCode(),
            ResultCode.FAIL.getMessage()
        );
    }

    public static <T> Response<T> failed(T data){
        return new Response<>(
            ResultCode.FAIL.getCode(),
            ResultCode.FAIL.getMessage(),
            data
        );
    }

}
