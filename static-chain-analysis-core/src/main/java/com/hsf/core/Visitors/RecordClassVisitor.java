package com.hsf.core.Visitors;

import com.hsf.core.Recorders.AbstractRecord;
import com.hsf.core.Recorders.DubboRecord;
import com.hsf.core.Recorders.InterfaceRecord;
import com.hsf.core.Recorders.ProjectRecord;
import com.hsf.tools.Utils.BasicUtil;
import com.hsf.tools.Utils.FilterUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static com.hsf.tools.Config.Code.METHOD_SPLIT;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;

public class RecordClassVisitor extends ClassVisitor {
    private final InterfaceRecord interfaceRecord;
    private final AbstractRecord abstractRecord;
    private final DubboRecord dubboRecord;
    private final ProjectRecord projectRecord;
    private String className;
    private boolean isInterface = false;
    private boolean isAbstract = false;
    private boolean isDubbo = false;

    public RecordClassVisitor(
        ClassVisitor cv, InterfaceRecord interfaceRecord, AbstractRecord abstractRecord, DubboRecord dubboRecord,
        ProjectRecord projectRecord
    ) {
        super(ASM7, cv);
        this.interfaceRecord = interfaceRecord;
        this.abstractRecord = abstractRecord;
        this.dubboRecord = dubboRecord;
        this.projectRecord = projectRecord;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
        // 记录className
        projectRecord.addProjectPackage(name);
        className = name;
        // 如果是interface
        if((access & ACC_INTERFACE)==ACC_INTERFACE){
            interfaceRecord.putInterfaceClass(name);
            isInterface = true;
        }
        // 如果是abstract类
        if((access & ACC_ABSTRACT)==ACC_ABSTRACT){
            abstractRecord.putAbstractClass(name);
            isAbstract = true;
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions){
        // 保存方法名
        String methodName = BasicUtil.getMethodSignatureName(name, descriptor);
        projectRecord.addProjectMethod(className + METHOD_SPLIT + methodName);
        // 接口或抽象类的abstract方法需要记录
        if(isInterface){
            if((access & ACC_ABSTRACT)==ACC_ABSTRACT){
                interfaceRecord.putMethod(className, methodName, true);
            }else{
                interfaceRecord.putMethod(className, methodName, false);
            }
        }
        if(isAbstract){
            if((access & ACC_ABSTRACT)==ACC_ABSTRACT){
                abstractRecord.putMethod(className, methodName, true);
            }else{
                abstractRecord.putMethod(className, methodName, false);
            }
        }
        if(isDubbo){
            dubboRecord.putDubboMethod(className + METHOD_SPLIT + methodName);
        }
        return new RecordMethodVisitor(
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
