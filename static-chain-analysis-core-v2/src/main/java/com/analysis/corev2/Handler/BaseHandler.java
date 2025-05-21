package com.analysis.corev2.Handler;

import com.analysis.corev2.Entitys.DTO.Handler.HandleDTO;

/**
 * 通过检查注解，判断是否要处理成入口函数，以及各个阶段的处理方法
 */
public interface BaseHandler {
    /**
     * RecordClassVisitor::visitAnnotation阶段的处理过程
     */
    void recordClassVisitAnnotationHandle(HandleDTO<?> handleDTO);

    /**
     * RecordClassVisitor::visitMethod 阶段的处理过程
     */
    void recordClassVisitMethodHandle(HandleDTO<?> handleDTO);

    /**
     * RecordClassVisitor::visit 阶段的处理过程
     */
    void recordClassVisitClassHandle(HandleDTO<?> handleDTO);

    /**
     * RecordMethodVisitor::visitAnnotation 阶段的处理过程
     */
    void recordMethodVisitAnnotationHandle(HandleDTO<?> handleDTO);

    /**
     * RecordAnnotationVisitor::visitArray 阶段的处理过程
     */
    void recordAnnotationVisitArrayHandle(HandleDTO<?> handleDTO);

    /**
     * RecordAnnotationVisitor::visit 阶段的处理过程
     */
    void recordAnnotationVisitHandle(HandleDTO<?> handleDTO);

    /**
     * RecordAnnotationVisitor::visitEnd 阶段的处理过程
     */
    void recordAnnotationVisitEndHandle(HandleDTO<?> handleDTO);
}
