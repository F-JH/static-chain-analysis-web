package com.hsf.admin.Code;

import lombok.Data;
import com.hsf.admin.Code.ResultCode;

@Data
public class ResultTemplate<T> {
    private int code;
    private String message;
    private T data;

    public ResultTemplate(){
        this.code = ResultCode.SUCCESS.getCode();
        this.message = ResultCode.SUCCESS.getMessage();
    }
    public ResultTemplate(int code, String message){
        this.code = code;
        this.message = message;
    }

    public ResultTemplate(int code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResultTemplate(T data){
        this.code = ResultCode.SUCCESS.getCode();
        this.message = ResultCode.SUCCESS.getMessage();
        this.data = data;
    }



    public static <T>ResultTemplate<T> success(){
        return new ResultTemplate<>();
    }

    public static <T>ResultTemplate<T> success(T data){
        return new ResultTemplate<>(data);
    }

    public static <T>ResultTemplate<T> failed(){
        return new ResultTemplate<>(
            ResultCode.FAIL.getCode(),
            ResultCode.FAIL.getMessage()
        );
    }

    public static <T>ResultTemplate<T> failed(T data){
        return new ResultTemplate<>(
            ResultCode.FAIL.getCode(),
            ResultCode.FAIL.getMessage(),
            data
        );
    }

}
