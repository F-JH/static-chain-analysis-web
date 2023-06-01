package com.hsf.admin.Code;

public enum TaskStatus {
    INIT(0, "初始化"),
    RUNNING(1, "执行中"),
    SUCCESS(2, "执行成功"),
    FAILD(3, "失败"),
    RETRY(4, "准备重试")
    ;

    public final Integer code;
    public final String message;

    TaskStatus(Integer code, String message){
        this.code = code;
        this.message = message;
    }
}
