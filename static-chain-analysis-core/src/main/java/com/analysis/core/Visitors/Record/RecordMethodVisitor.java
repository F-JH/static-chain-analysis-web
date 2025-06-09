package com.analysis.core.Visitors.Record;

import com.analysis.core.Entitys.DTO.Handler.HandleDTO;
import com.analysis.core.Entitys.DTO.Handler.MethodVisitorVisitAnnotationHandleDTO;
import com.analysis.core.Entitys.DTO.RecordDTO;
import com.analysis.core.Enums.HandleTypeEnum;
import com.analysis.core.Enums.JdkVersionEnum;
import com.analysis.core.Handler.BaseHandler;
import com.analysis.tools.Utils.BasicUtil;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.List;
import java.util.Set;

public class RecordMethodVisitor extends AdviceAdapter {

    private final JdkVersionEnum jdkVersion;
    private final RecordDTO recordDTO;
    private final List<BaseHandler> handlers;
    private final String className;
    private final String methodName;
    // 记录自己的requestMappingValue
//    private final Set<String> paths;
    // 上一级的requestMappingValue(一般是类的RequestMapping)
    private final Set<String> parentPaths;

    public RecordMethodVisitor(JdkVersionEnum jdkVersion, RecordDTO recordDTO, List<BaseHandler> handlers, MethodVisitor mv, String className, int access, String methodName, String desc, Set<String> parentPaths) {
        super(jdkVersion.getCode(), mv, access, methodName, desc);
        this.jdkVersion = jdkVersion;
        this.recordDTO = recordDTO;
        this.handlers = handlers;
        this.className = className;
        this.methodName = BasicUtil.getMethodSignatureName(methodName, desc);
        this.parentPaths = parentPaths;
    }

    @Override
    public void visitMaxs(int a, int b){
        // 这里不关心Type的报错，直接忽略
        try{
            super.visitMaxs(a, b);
        }catch (TypeNotPresentException ignored){
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visiable){
        handlers.forEach(handler -> {
            HandleDTO<MethodVisitorVisitAnnotationHandleDTO> handleDTO = new HandleDTO<>(HandleTypeEnum.METHOD_ANNOTATION_HANDLE, new MethodVisitorVisitAnnotationHandleDTO(
                    descriptor, visiable, recordDTO, className, methodName
            ));
            handler.recordMethodVisitAnnotationHandle(handleDTO);
        });
        return new RecordAnnotationVisitor(jdkVersion, super.visitAnnotation(descriptor, visiable), recordDTO, handlers, className, methodName, parentPaths);
    }
}
