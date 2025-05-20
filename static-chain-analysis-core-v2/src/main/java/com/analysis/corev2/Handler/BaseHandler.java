package com.analysis.corev2.Handler;

import com.analysis.corev2.Entitys.DTO.Handler.HandleDTO;

/**
 * 通过检查注解，判断是否要处理成入口函数，以及各个阶段的处理方法
 */
public interface BaseHandler {
    /**
     * RecordClassVisitor::visitAnnotation阶段的处理过程
     */
    void annotationHandle(HandleDTO<?> handleDTO);

    /**
     * RecordClassVisitor::visitMethod阶段的处理过程
     */
    void methodHandle(HandleDTO<?> handleDTO);

    /**
     * RecordClassVisitor::visit阶段的处理过程
     */
    void classHandle(HandleDTO<?> handleDTO);

    /**
     * RecordMethodVisitor::visitAnnotation阶段的处理过程
     */
    void methodAnnotationHandle(HandleDTO<?> handleDTO);
}
