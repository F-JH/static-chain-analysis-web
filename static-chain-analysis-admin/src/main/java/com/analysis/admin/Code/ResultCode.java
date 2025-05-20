package com.analysis.admin.Code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200,"success"),//成功
    FAIL(400,"request error"),//失败
    PARAMS_NULL_ERROR(1001,"参数为空"), //参数为空
    PARAMS_ERROR(1002,"参数错误"),//参数错误
    NOT_FOUND(404,"未找到接口"),// 未找到接口
    INTERNAL_SERVER_ERROR(500,"服务器错误"), //服务器内部错误
    SPECIAL_INSTRUCTION(100, "返回带有特殊说明的错误")
    ;

    private final int code;
    private final String message;
}
