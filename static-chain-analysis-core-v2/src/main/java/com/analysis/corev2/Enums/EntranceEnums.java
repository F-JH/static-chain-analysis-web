package com.analysis.corev2.Enums;

import com.analysis.corev2.Handler.BaseHandler;
import com.analysis.corev2.Handler.DubboHandler;
import com.analysis.corev2.Handler.KafkaHandler;
import lombok.Getter;

import java.util.List;

/*
    调用链入口配置枚举
 */
@Getter
public enum EntranceEnums {
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
    DUBBO_SERVICE("Lorg/apache/dubbo/config/annotation/DubboService;", "DubboService", DubboHandler.class),
    ALIBABA_DUBBO_SERVICE("Lcom/alibaba/dubbo/config/annotation/Service;", "AlibabaDubboService"),
    APACHE_DUBBO_SERVICE("Lorg/apache/dubbo/config/annotation/Service;", "ApacheDubboService"),

    // Kafka的注解
    KAFKA_LISTENER("Lorg/springframework/kafka/annotation/KafkaListener;", "KafkaListener", KafkaHandler.class),
    ;

    private final String code;
    private final String desc;
    private final Class<? extends BaseHandler> handler;

    EntranceEnums(String code, String desc) {
        this.code = code;
        this.desc = desc;
        this.handler = null;
    }

    EntranceEnums(String code, String desc, Class<? extends BaseHandler> handler) {
        this.code = code;
        this.desc = desc;
        this.handler = handler;
    }

    private static List<EntranceEnums> requestAnnotation = List.of(
            REQUEST_MAPPING,
            GET_MAPPING,
            POST_MAPPING,
            DELETE_MAPPING,
            PATCH_MAPPING,
            PUT_MAPPING,
            MESSAGE_MAPPING,
            SUBSCRIBE_MAPPING
    );
    private static List<EntranceEnums> controllerAnnotation = List.of(
            REST_CONTROLLER,
            CONTROLLER
    );
    private static List<EntranceEnums> dubboAnnotation = List.of(
            DUBBO_SERVICE,
            ALIBABA_DUBBO_SERVICE,
            APACHE_DUBBO_SERVICE
    );
    private static List<EntranceEnums> kafkaAnnotation = List.of(
            KAFKA_LISTENER
    );
    @Getter
    private static final List<EntranceEnums> needHandleEnum = List.of(
            KAFKA_LISTENER
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

    public static boolean isKafkaAnnotation(String annoDesc){
        if(null == annoDesc)
            return false;
        return kafkaAnnotation.stream()
                .anyMatch(recordEnum -> recordEnum.getCode().equals(annoDesc));
    }
}
