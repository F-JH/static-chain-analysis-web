package com.analysis.core.Handler;

import com.analysis.core.Entitys.DTO.Handler.ClassVisitorVisitAnnotationHandleDTO;
import com.analysis.core.Entitys.DTO.Handler.ClassVisitorVisitMethodHandleDTO;
import com.analysis.core.Entitys.DTO.Handler.HandleDTO;
import com.analysis.core.Enums.EntranceEnums;
import com.analysis.tools.Utils.BasicUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class GrpcHandler extends AbstractHandler {

    // gRPC 方法参数中固定会出现的 StreamObserver 描述符
    private static final String GRPC_OBSERVER_DESC = "Lio/grpc/stub/StreamObserver;";

    /**
     * 1. 识别类注解 —— 只要看见 @GrpcService 就登记为 gRPC 类
     */
    @Override
    public void recordClassVisitAnnotationHandle(HandleDTO<?> handleDTO) {
        ClassVisitorVisitAnnotationHandleDTO dto = (ClassVisitorVisitAnnotationHandleDTO) handleDTO.getHandleData();
        if (EntranceEnums.isGrpcAnnotation(dto.getDescriptor())) {
            dto.getRecordDTO().getGrpcRecord().addGrpcClass(dto.getClassName());
            log.info("[GrpcService] 发现带有 gRPC 注解的类: {}", dto.getClassName());
        }
    }

    /**
     * 2. 识别方法 —— 方法签名待 StreamObserver 且所在类是 GRPC 类
     */
    @Override
    public void recordClassVisitMethodHandle(HandleDTO<?> handleDTO) {
        ClassVisitorVisitMethodHandleDTO dto = (ClassVisitorVisitMethodHandleDTO) handleDTO.getHandleData();
        if (!dto.getRecordDTO().getGrpcRecord().isGrpcClass(dto.getClassName())) {
            return;  //类不是 grpc 直接返回
        }
        if (dto.getDescriptor().contains(GRPC_OBSERVER_DESC)) {
            String signatureName = BasicUtil.getMethodSignatureName(dto.getName(), dto.getDescriptor());
            String fullClassName = dto.getClassName() + "." + signatureName;
            dto.getRecordDTO().getGrpcRecord().putGrpcMethod(fullClassName);
            dto.getRecordDTO().getGrpcRecord().putGrpcService(fullClassName, dto.getClassName());
            log.info("[GrpcMethod] 检测到 gRPC 方法: {}", fullClassName);
        }
    }
}