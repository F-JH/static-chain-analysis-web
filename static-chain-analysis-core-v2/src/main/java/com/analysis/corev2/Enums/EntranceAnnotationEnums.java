package com.analysis.corev2.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
    调用链入口配置枚举
 */
@Getter
@AllArgsConstructor
public enum EntranceAnnotationEnums {
    // 入口注解
    ENTRANCE_ANNOTATION("", "入口注解"),
    ;

    private final String value;
    private final String desc;
}
