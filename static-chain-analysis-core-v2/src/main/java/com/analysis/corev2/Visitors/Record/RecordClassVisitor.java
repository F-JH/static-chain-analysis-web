package com.analysis.corev2.Visitors.Record;

import com.analysis.corev2.Entitys.DTO.RecordDTO;
import com.analysis.corev2.Enums.JdkVersionEnum;
import com.analysis.tools.Utils.BasicUtil;
import com.analysis.tools.Utils.FilterUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static com.analysis.tools.Config.Code.METHOD_SPLIT;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

public class RecordClassVisitor extends ClassVisitor {
    private final JdkVersionEnum jdkVersionEnum;
    private final RecordDTO recordDTO;
    private String className;
    private boolean isInterface = false;
    private boolean isAbstract = false;
    private boolean isDubbo = false;

    public RecordClassVisitor(JdkVersionEnum jdkVersion, ClassVisitor cv, RecordDTO recordDTO) {
        super(jdkVersion.getCode(), cv);
        this.jdkVersionEnum = jdkVersion;
        this.recordDTO = recordDTO;
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
            if((access & ACC_ABSTRACT)==ACC_ABSTRACT){
                recordDTO.getInterfaceRecord().putMethod(className, methodName, true);
            }else{
                recordDTO.getInterfaceRecord().putMethod(className, methodName, false);
            }
        }
        if(isAbstract){
            if((access & ACC_ABSTRACT)==ACC_ABSTRACT){
                recordDTO.getAbstractRecord().putMethod(className, methodName, true);
            }else{
                recordDTO.getAbstractRecord().putMethod(className, methodName, false);
            }
        }
        if(isDubbo){
            recordDTO.getDubboRecord().putDubboMethod(className + METHOD_SPLIT + methodName);
        }
        return new RecordMethodVisitor(jdkVersionEnum,
            super.visitMethod(access, name, descriptor, signature, exceptions),
            access, name, descriptor
        );
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visiable){
        if(FilterUtils.isDubboAnnotation(descriptor)){
            isDubbo = true;
        }
        return super.visitAnnotation(descriptor, visiable);
    }
}
