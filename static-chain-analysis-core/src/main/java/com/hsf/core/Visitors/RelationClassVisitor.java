package com.hsf.core.Visitors;

import com.hsf.core.Enums.JdkVersionEnum;
import com.hsf.core.Recorders.*;
import com.hsf.tools.Utils.BasicUtil;
import com.hsf.tools.Utils.FilterUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.*;

import static org.objectweb.asm.Opcodes.ASM7;

public class RelationClassVisitor extends ClassVisitor {

    private String className;
    private final Map<String, List<String>> methodRelations = new HashMap<>();
    private final InterfaceRecord interfaceRecord;
    private final AbstractRecord abstractRecord;
    private final ProjectRecord projectRecord;
    private final ControllerRecord controllerRecord;
    private final ApiRecord apiRecord;
    private boolean isController = false;
    private boolean hasRequestMapping = false;
    private boolean isInterface;
    // 记录自己的 requestMapping::Value
    private Set<String> requestMappingValue;
    // 记录全部子方法的api入口
    private final Map<String, Set<String>> recordMapping = new HashMap<>();
    private final JdkVersionEnum jdkVersionEnum;

    public RelationClassVisitor(
            JdkVersionEnum jdkVersionEnum, ClassVisitor classVisitor, InterfaceRecord interfaceRecord, AbstractRecord abstractRecord,
            ProjectRecord projectRecord, ControllerRecord controllerRecord, ApiRecord apiRecord
    ) {
        super(jdkVersionEnum.getCode(), classVisitor);
        this. interfaceRecord = interfaceRecord;
        this.abstractRecord = abstractRecord;
        this.projectRecord = projectRecord;
        this.controllerRecord = controllerRecord;
        this.apiRecord = apiRecord;
        this.jdkVersionEnum = jdkVersionEnum;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
        className = name;
        // 如果是接口的实体类，则加入interfaceRecord
        if(interfaces.length > 0){
            for(String interfaceClassName:interfaces) {
                if (projectRecord.isNeedInject(interfaceClassName)) {
                    interfaceRecord.putInterfaceEntry(interfaceClassName, name);
                }
            }
        }
        // 如果是abstract类的实体类...
        if(abstractRecord.containAbstract(superName)){
            abstractRecord.putAbstractEntry(superName, name);
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions){
        List<String> methodRelation = new ArrayList<>();
        // 处理一下方法名，适配javaparser
        String signatureName = BasicUtil.getMethodSignatureName(name, descriptor);
        methodRelations.put(signatureName, methodRelation);
        if(hasRequestMapping){
            return new RelationMethodVisitor(
                    jdkVersionEnum,
                access, name, descriptor, super.visitMethod(access,name,descriptor,signature,exceptions), projectRecord,
                apiRecord, controllerRecord, methodRelation, className, requestMappingValue, recordMapping
            );
        }else{
            // 类没有 RequestMapping 则parentPath为空的Set
            return new RelationMethodVisitor(
                    jdkVersionEnum,
                access, name, descriptor, super.visitMethod(access,name,descriptor,signature,exceptions), projectRecord,
                apiRecord, controllerRecord, methodRelation, className, new HashSet<>(), recordMapping
            );
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visiable){
        if(FilterUtils.isControllerAnnotation(descriptor)){
            // controller类
            controllerRecord.putControlClass(className);
            isController = true;
        }
        if(FilterUtils.isRequestAnnotation(descriptor)){
            hasRequestMapping = true;
            requestMappingValue = new HashSet<>();
            Set<String> parentPath = new HashSet<>();
            // 这里去获取类的 requestMappingValue
            return new RelationAnnotationVisitor(jdkVersionEnum, super.visitAnnotation(descriptor, visiable), requestMappingValue, parentPath);
        }
        return super.visitAnnotation(descriptor, visiable);
    }

    @Override
    public void visitEnd(){
        super.visitEnd();
    }

    public Map<String, List<String>> getMethodRelations() {
        return methodRelations;
    }

    public Map<String, Set<String>> getRecordMapping() {
        return recordMapping;
    }
}
