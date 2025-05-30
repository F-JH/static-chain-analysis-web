package com.analysis.core.Recorders.Entrances;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GrpcRecord {
    /* gRPC 服务类集合（class 级别）*/
    private final Set<String> grpcClasses;

    /* gRPC 服务方法集合（method 级别，如 com/foo/BarGrpcImpl.method）*/
    private final List<String> grpcMethods;

    /* 保存 "方法 → Service 名称" 的映射 */
    private final Map<String, String> grpcServiceMap;

    public GrpcRecord() {
        grpcClasses   = ConcurrentHashMap.newKeySet();
        grpcMethods   = new CopyOnWriteArrayList<>();
        grpcServiceMap= new ConcurrentHashMap<>();
    }

    /** 记录一个 gRPC 服务类 */
    public void addGrpcClass(String className) {
        grpcClasses.add(className);
    }

    /** 判断某个类是否已经被识别为 grpc 服务类 */
    public boolean isGrpcClass(String className) {
        return grpcClasses.contains(className);
    }

    /** 记录 gRPC 服务方法（带包名+方法名）*/
    public void putGrpcMethod(String fullMethodName) {
        grpcMethods.add(fullMethodName);
    }

    /** 保存 "方法 → serviceName" 映射 */
    public void putGrpcService(String fullMethodName, String serviceName) {
        grpcServiceMap.put(fullMethodName, serviceName);
    }

    /** 判断某个方法是否在 gRPC 方法集合中 */
    public boolean contains(String fullMethodNme) {
        return grpcMethods.contains(fullMethodNme);
    }

    /** 获取某个方法对应的 serviceName */
    public String getGrpcService(String fullMethodNme) {
        return grpcServiceMap.get(fullMethodNme);
    }

    public List<String> getGrpcMethods() {
        return List.copyOf(grpcMethods);
    }
}
