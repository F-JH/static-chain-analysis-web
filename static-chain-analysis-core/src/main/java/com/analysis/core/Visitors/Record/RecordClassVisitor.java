package com.analysis.core.Visitors.Record;

import com.analysis.core.Entitys.DTO.Handler.ClassVisitorVisitAnnotationHandleDTO;
import com.analysis.core.Entitys.DTO.Handler.ClassVisitorVisitMethodHandleDTO;
import com.analysis.core.Entitys.DTO.Handler.HandleDTO;
import com.analysis.core.Entitys.DTO.RecordDTO;
import com.analysis.core.Enums.EntranceEnums;
import com.analysis.core.Enums.HandleTypeEnum;
import com.analysis.core.Enums.JdkVersionEnum;
import com.analysis.core.Handler.BaseHandler;
import com.analysis.tools.Utils.BasicUtil;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private final List<BaseHandler> handlers;
    // 记录类注解上的 requestMapping::Value
    private Set<String> paths;

    public RecordClassVisitor(JdkVersionEnum jdkVersion, ClassVisitor cv, RecordDTO recordDTO) {
        super(jdkVersion.getCode(), cv);
        this.jdkVersionEnum = jdkVersion;
        this.recordDTO = recordDTO;
        handlers = new ArrayList<>();
        paths = new HashSet<>();
        EntranceEnums.getNeedHandleEnum()
                .forEach(entrance -> {
                    Class<? extends BaseHandler> cls = entrance.getHandler();
                    if (cls != null) {
                        try{
                            BaseHandler handler = cls.getDeclaredConstructor().newInstance();
                            handlers.add(handler);
                        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                                 NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
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
            HandleDTO<ClassVisitorVisitMethodHandleDTO> handleDTO = new HandleDTO<>(HandleTypeEnum.METHOD_HANDLE, new ClassVisitorVisitMethodHandleDTO(
                    access, name, descriptor, signature, exceptions,
                    className, recordDTO, isInterface, isAbstract
            ));
            handler.recordClassVisitMethodHandle(handleDTO);
        });
        return new RecordMethodVisitor(jdkVersionEnum, recordDTO, handlers, super.visitMethod(access, name, descriptor, signature, exceptions), className, access, name, descriptor, paths);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible){
        handlers.forEach(handler -> {
            HandleDTO<ClassVisitorVisitAnnotationHandleDTO> handleDTO = new HandleDTO<>(HandleTypeEnum.ANNOTATION_HANDLE, new ClassVisitorVisitAnnotationHandleDTO(
                    descriptor, visible, className, recordDTO, isInterface, isAbstract, paths
            ));
            handler.recordClassVisitAnnotationHandle(handleDTO);
        });
        return new RecordAnnotationVisitor(jdkVersionEnum, super.visitAnnotation(descriptor, visible), recordDTO, handlers, className, null, paths);
    }
}
