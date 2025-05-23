package com.analysis.tools.Utils;

import java.util.HashSet;
import java.util.Set;

public class FilterUtils {
//    private static final Set<String> requestAnnotation = new HashSet<>();
//    private static final Set<String> controllerAnnotation = new HashSet<>();
//    private static final Set<String> dubboAnnotation = new HashSet<>();
    private static final Set<String> mybatisTagName = new HashSet<>();
    static {
//        // Request的注解
//        requestAnnotation.add("Lorg/springframework/web/bind/annotation/RequestMapping;");
//        requestAnnotation.add("Lorg/springframework/web/bind/annotation/GetMapping;");
//        requestAnnotation.add("Lorg/springframework/web/bind/annotation/PostMapping;");
//        requestAnnotation.add("Lorg/springframework/web/bind/annotation/DeleteMapping;");
//        requestAnnotation.add("Lorg/springframework/web/bind/annotation/PatchMapping;");
//        requestAnnotation.add("Lorg/springframework/web/bind/annotation/PutMapping;");
//        requestAnnotation.add("Lorg/springframework/web/bind/annotation/MessageMapping;");
//        requestAnnotation.add("Lorg/springframework/web/bind/annotation/SubscribeMapping;");
//
//        // Controller 的注解
//        controllerAnnotation.add("Lorg/springframework/web/bind/annotation/RestController;");
//        controllerAnnotation.add("Lorg/springframework/stereotype/Controller;");
//
//        // DubboService 的注解
//        dubboAnnotation.add("Lorg/apache/dubbo/config/annotation/DubboService;");
//        dubboAnnotation.add("Lcom/alibaba/dubbo/config/annotation/Service;");
//        dubboAnnotation.add("Lorg/apache/dubbo/config/annotation/Service;");

        // mybatis的xml配置的tagName
        mybatisTagName.add("insert");
        mybatisTagName.add("delete");
        mybatisTagName.add("update");
        mybatisTagName.add("select");
    }

//    public static boolean isControllerAnnotation(String annoDesc){
//        if(null == annoDesc)
//            return false;
//        return controllerAnnotation.contains(annoDesc);
//    }
//
//    public static boolean isRequestAnnotation(String annoDesc){
//        if(null == annoDesc)
//            return false;
//        return requestAnnotation.contains(annoDesc);
//    }
//
//    public static boolean isDubboAnnotation(String annoDesc){
//        if(null == annoDesc)
//            return false;
//        return dubboAnnotation.contains(annoDesc);
//    }

    public static boolean isCURD(String tagName){
        if(null == tagName)
            return false;
        return mybatisTagName.contains(tagName);
    }
}
