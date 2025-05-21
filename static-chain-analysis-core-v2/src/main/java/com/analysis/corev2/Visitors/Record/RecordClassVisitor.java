package com.analysis.corev2.Visitors.Record;

import com.analysis.corev2.Entitys.DTO.Handler.ClassAnnotationHandleDTO;
import com.analysis.corev2.Entitys.DTO.Handler.HandleDTO;
import com.analysis.corev2.Entitys.DTO.Handler.MethodHandleDTO;
import com.analysis.corev2.Entitys.DTO.RecordDTO;
import com.analysis.corev2.Enums.EntranceEnums;
import com.analysis.corev2.Enums.HandleTypeEnum;
import com.analysis.corev2.Enums.JdkVersionEnum;
import com.analysis.corev2.Handler.BaseHandler;
import com.analysis.tools.Utils.BasicUtil;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

import static com.analysis.tools.Config.Code.METHOD_SPLIT;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

/**
 * 这里的方法是先记录一遍需要记录的入口类型，只做记录
 */
@Slf4j
public class RecordClassVisitor extends ClassVisitor {
    private final JdkVersionEnum jdkVersionEnum;
    private final RecordDTO recordDTO;
    private String className;
    private boolean isInterface = false;
    private boolean isAbstract = false;
    private boolean isDubbo = false;

    private final List<BaseHandler> handlers;

    public RecordClassVisitor(JdkVersionEnum jdkVersion, ClassVisitor cv, RecordDTO recordDTO) {
        super(jdkVersion.getCode(), cv);
        this.jdkVersionEnum = jdkVersion;
        this.recordDTO = recordDTO;
        handlers = new ArrayList<>();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
        // 记录className
        recordDTO.getProjectRecord().addProjectPackage(name);
        className = name;
        // 如果是interface
        if((access & ACC_INTERFACE)==ACC_INTERFACE){
            recordDTO.getInterfaceRecord().putInterfaceClass(name);
            isInterface = true;
        }
        // 如果是abstract类
        if((access & ACC_ABSTRACT)==ACC_ABSTRACT){
            recordDTO.getAbstractRecord().putAbstractClass(name);
            isAbstract = true;
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions){
        // 保存方法名
        String methodName = BasicUtil.getMethodSignatureName(name, descriptor);
        recordDTO.getProjectRecord().addProjectMethod(className + METHOD_SPLIT + methodName);
        // 接口或抽象类的abstract方法需要记录
        if(isInterface){
            recordDTO.getInterfaceRecord().putMethod(className, methodName, (access & ACC_ABSTRACT) == ACC_ABSTRACT);
        }
        if(isAbstract){
            recordDTO.getAbstractRecord().putMethod(className, methodName, (access & ACC_ABSTRACT) == ACC_ABSTRACT);
        }
        // 流程化处理
        handlers.forEach(handler -> {
            HandleDTO<MethodHandleDTO> handleDTO = new HandleDTO<>(HandleTypeEnum.METHOD_HANDLE, new MethodHandleDTO(
                    access, name, descriptor, signature, exceptions,
                    className, recordDTO, isInterface, isAbstract
            ));
            handler.recordClassVisitMethodHandle(handleDTO);
        });
        return new RecordMethodVisitor(jdkVersionEnum, recordDTO, handlers, super.visitMethod(access, name, descriptor, signature, exceptions), className, access, name, descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible){
        EntranceEnums.getNeedHandleEnum()
                .forEach(entrance -> {
                    Class<? extends BaseHandler> cls = entrance.getHandler();
                    if (cls != null) {
                        try {
                            BaseHandler handler = cls.getDeclaredConstructor().newInstance();
                            handlers.add(handler);
                            handler.recordClassVisitAnnotationHandle(new HandleDTO<>(HandleTypeEnum.ANNOTATION_HANDLE, new ClassAnnotationHandleDTO(
                                    descriptor, visible, className, recordDTO, isInterface, isAbstract
                            )));
                        } catch (Exception e) {
                            log.error("Error creating handler instance: {}", e.getMessage());
                        }
                    }
                });
        return new RecordAnnotationVisitor(jdkVersionEnum, super.visitAnnotation(descriptor, visible), recordDTO, handlers, className, null);
    }
}
