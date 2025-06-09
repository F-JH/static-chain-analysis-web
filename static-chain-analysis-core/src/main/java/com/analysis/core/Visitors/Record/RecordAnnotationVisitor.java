package com.analysis.core.Visitors.Record;

import com.analysis.core.Entitys.DTO.Handler.AnnotationVisitorArrayHandleDTO;
import com.analysis.core.Entitys.DTO.Handler.AnnotationVisitorVisitEndHandleDTO;
import com.analysis.core.Entitys.DTO.Handler.AnnotationVisitorVisitHandleDTO;
import com.analysis.core.Entitys.DTO.Handler.HandleDTO;
import com.analysis.core.Entitys.DTO.RecordDTO;
import com.analysis.core.Enums.HandleTypeEnum;
import com.analysis.core.Enums.JdkVersionEnum;
import com.analysis.core.Handler.BaseHandler;
import org.objectweb.asm.AnnotationVisitor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecordAnnotationVisitor extends AnnotationVisitor {
    private final JdkVersionEnum jdkVersionEnum;
    private final RecordDTO recordDTO;
    private final List<BaseHandler> handlers;

    private final String className;
    private final String methodName;

    // 上一级的requestMappingValue(一般是类的RequestMapping)
    private final Set<String> parentPaths;

    public RecordAnnotationVisitor(JdkVersionEnum jdkVersionEnum, AnnotationVisitor av, RecordDTO recordDTO, List<BaseHandler> handlers, String className, String methodName, Set<String> parentPaths){
        super(jdkVersionEnum.getCode(), av);
        this.jdkVersionEnum = jdkVersionEnum;
        this.recordDTO = recordDTO;
        this.handlers = handlers;
        this.className = className;
        this.methodName = methodName;
        this.parentPaths = parentPaths;
    }

    @Override
    public AnnotationVisitor visitArray(String name){
        // annotationVisitor0 访问
        handlers.forEach(handler -> {
            // 这里的name是数组的名称
            AnnotationVisitorArrayHandleDTO annotationVisitorArrayHandleDTO = new AnnotationVisitorArrayHandleDTO(name, className, methodName, recordDTO, parentPaths);
            HandleDTO<AnnotationVisitorArrayHandleDTO> handleDTO = new HandleDTO<>(HandleTypeEnum.ANNOTATION_VISIT_ARRAY_HANDLE, annotationVisitorArrayHandleDTO);
            handler.recordAnnotationVisitArrayHandle(handleDTO);
        });
        return new RecordAnnotationVisitor(jdkVersionEnum, super.visitArray(name), recordDTO, handlers, className, methodName, parentPaths);
    }

    @Override
    public void visit(String name, Object value){
        handlers.forEach(handler -> {
            AnnotationVisitorVisitHandleDTO annotationVisitorVisitHandleDTO = new AnnotationVisitorVisitHandleDTO(name, value, className, methodName, recordDTO, parentPaths);
            HandleDTO<AnnotationVisitorVisitHandleDTO> handleDTO = new HandleDTO<>(HandleTypeEnum.ANNOTATION_VISIT_HANDLE, annotationVisitorVisitHandleDTO);
            handler.recordAnnotationVisitHandle(handleDTO);
        });
        super.visit(name, value);
    }

    @Override
    public void visitEnd(){
        handlers.forEach(handler -> {
            AnnotationVisitorVisitEndHandleDTO annotationVisitorVisitEndHandleDTO = new AnnotationVisitorVisitEndHandleDTO(className, methodName, recordDTO, parentPaths);
            HandleDTO<AnnotationVisitorVisitEndHandleDTO> handleDTO = new HandleDTO<>(HandleTypeEnum.ANNOTATION_VISIT_END_HANDLE, annotationVisitorVisitEndHandleDTO);
            handler.recordAnnotationVisitEndHandle(handleDTO);
        });
        super.visitEnd();
    }
}
