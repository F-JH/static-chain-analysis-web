package com.analysis.corev2.Visitors.Record;

import com.analysis.corev2.Entitys.DTO.Handler.HandleDTO;
import com.analysis.corev2.Entitys.DTO.Handler.MethodAnnotationHandleDTO;
import com.analysis.corev2.Entitys.DTO.RecordDTO;
import com.analysis.corev2.Enums.HandleTypeEnum;
import com.analysis.corev2.Enums.JdkVersionEnum;
import com.analysis.corev2.Handler.BaseHandler;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.List;

public class RecordMethodVisitor extends AdviceAdapter {

    private final RecordDTO recordDTO;
    private final List<BaseHandler> handlers;
    private final String className;
    private final String methodName;

    public RecordMethodVisitor(JdkVersionEnum jdkVersion, RecordDTO recordDTO, List<BaseHandler> handlers, MethodVisitor mv, String className, int access, String methodName, String desc){
        super(jdkVersion.getCode(), mv, access, methodName, desc);
        this.recordDTO = recordDTO;
        this.handlers = handlers;
        this.className = className;
        this.methodName = methodName;
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
            HandleDTO<MethodAnnotationHandleDTO> handleDTO = new HandleDTO<>(HandleTypeEnum.METHOD_ANNOTATION_HANDLE, new MethodAnnotationHandleDTO(
                    descriptor, visiable, recordDTO, className, methodName
            ));
            handler.methodAnnotationHandle(handleDTO);
        });
        return super.visitAnnotation(descriptor, visiable);
    }
}
