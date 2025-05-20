package com.analysis.tools.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public enum RecordEnum {
    // Request的注解
    REQUEST_MAPPING("Lorg/springframework/web/bind/annotation/RequestMapping;", "RequestMapping"),
    GET_MAPPING("Lorg/springframework/web/bind/annotation/GetMapping;", "GetMapping"),
    POST_MAPPING("Lorg/springframework/web/bind/annotation/PostMapping;", "PostMapping"),
    DELETE_MAPPING("Lorg/springframework/web/bind/annotation/DeleteMapping;", "DeleteMapping"),
    PATCH_MAPPING("Lorg/springframework/web/bind/annotation/PatchMapping;", "PatchMapping"),
    PUT_MAPPING("Lorg/springframework/web/bind/annotation/PutMapping;", "PutMapping"),
    MESSAGE_MAPPING("Lorg/springframework/web/bind/annotation/MessageMapping;", "MessageMapping"),
    SUBSCRIBE_MAPPING("Lorg/springframework/web/bind/annotation/SubscribeMapping;", "SubscribeMapping"),

    // Controller 的注解
    REST_CONTROLLER("Lorg/springframework/web/bind/annotation/RestController;", "RestController"),
    CONTROLLER("Lorg/springframework/stereotype/Controller;", "Controller"),

    // DubboService 的注解
    DUBBO_SERVICE("Lorg/apache/dubbo/config/annotation/DubboService;", "DubboService"),
    ALIBABA_DUBBO_SERVICE("Lcom/alibaba/dubbo/config/annotation/Service;", "AlibabaDubboService"),
    APACHE_DUBBO_SERVICE("Lorg/apache/dubbo/config/annotation/Service;", "ApacheDubboService"),

    // mybatis的xml配置的tagName
    INSERT("insert", "Insert"),
    DELETE("delete", "Delete"),
    UPDATE("update", "Update"),
    SELECT("select", "Select"),
    ;

    private final String code;
    private final String desc;

    private static List<RecordEnum> requestAnnotation = List.of(
            REQUEST_MAPPING,
            GET_MAPPING,
            POST_MAPPING,
            DELETE_MAPPING,
            PATCH_MAPPING,
            PUT_MAPPING,
            MESSAGE_MAPPING,
            SUBSCRIBE_MAPPING
    );
    private static List<RecordEnum> controllerAnnotation = List.of(
            REST_CONTROLLER,
            CONTROLLER
    );
    private static List<RecordEnum> dubboAnnotation = List.of(
            DUBBO_SERVICE,
            ALIBABA_DUBBO_SERVICE,
            APACHE_DUBBO_SERVICE
    );

    private static List<RecordEnum> mybatisTagName = List.of(
            INSERT,
            DELETE,
            UPDATE,
            SELECT
    );

    public static boolean isControllerAnnotation(String annoDesc){
        if(null == annoDesc)
            return false;
        return controllerAnnotation.stream()
                .anyMatch(recordEnum -> recordEnum.getCode().equals(annoDesc));
    }

    public static boolean isRequestAnnotation(String annoDesc){
        if(null == annoDesc)
            return false;
        return requestAnnotation.stream()
                .anyMatch(recordEnum -> recordEnum.getCode().equals(annoDesc));
    }

    public static boolean isDubboAnnotation(String annoDesc){
        if(null == annoDesc)
            return false;
        return dubboAnnotation.stream()
                .anyMatch(recordEnum -> recordEnum.getCode().equals(annoDesc));
    }

    public static boolean isCURD(String tagName){
        if(null == tagName)
            return false;
        return mybatisTagName.stream()
                .anyMatch(recordEnum -> recordEnum.getCode().equals(tagName));
    }
}
