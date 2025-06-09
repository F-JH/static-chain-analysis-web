package com.analysis.core.Handler;

import com.analysis.core.Entitys.DTO.Handler.HandleDTO;

public abstract class AbstractHandler implements BaseHandler{
    public void recordClassVisitAnnotationHandle(HandleDTO<?> handleDTO){}

    /**
     * RecordClassVisitor::visitMethod阶段的处理过程
     */
    public void recordClassVisitMethodHandle(HandleDTO<?> handleDTO){}

    /**
     * RecordClassVisitor::visit阶段的处理过程
     */
    public void recordClassVisitClassHandle(HandleDTO<?> handleDTO){}

    /**
     * RecordMethodVisitor::visitAnnotation阶段的处理过程
     */
    public void recordMethodVisitAnnotationHandle(HandleDTO<?> handleDTO){}

    /**
     * RecordMethodVisitor::visitEnd 阶段的处理过程
     */
    public void recordMethodVisitEndHandle(HandleDTO<?> handleDTO){}

    /**
     * RecordAnnotationVisitor::visitArray 阶段的处理过程
     */
    public void recordAnnotationVisitArrayHandle(HandleDTO<?> handleDTO){}

    /**
     * RecordAnnotationVisitor::visit 阶段的处理过程
     */
    public void recordAnnotationVisitHandle(HandleDTO<?> handleDTO){}

    /**
     * RecordAnnotationVisitor::visitEnd 阶段的处理过程
     */
    public void recordAnnotationVisitEndHandle(HandleDTO<?> handleDTO){}
}
