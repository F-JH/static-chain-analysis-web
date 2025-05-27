package com.analysis.core.Visitors.Relation;

import com.analysis.core.Entitys.DTO.RecordDTO;
import com.analysis.core.Enums.EntranceEnums;
import com.analysis.core.Enums.JdkVersionEnum;
import com.analysis.core.Handler.BaseHandler;
import com.analysis.tools.Utils.BasicUtil;
import lombok.Getter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.analysis.tools.Config.Code.METHOD_SPLIT;

public class RelationClassVisitor extends ClassVisitor {

    private String className;
//    @Getter
//    private final Map<String, List<String>> methodRelations = new ConcurrentHashMap<>();
    private final RecordDTO recordDTO;

    private boolean isController = false;
    private boolean isAbstract = false;
    private boolean hasRequestMapping = false;
    // 记录是哪个接口的实体类
    private final List<String> interfaces;
    // 记录是哪个抽象类的子类
    private String abstractClass;
    // 记录自己的 requestMapping::Value
    private Set<String> requestMappingValue;
    // 记录全部子方法的api入口
    private final List<BaseHandler> handlers;
    @Getter
    private final Map<String, Set<String>> recordMapping = new ConcurrentHashMap<>();
    private final JdkVersionEnum jdkVersionEnum;

    public RelationClassVisitor(JdkVersionEnum jdkVersionEnum, ClassVisitor classVisitor, RecordDTO recordDTO) {
        super(jdkVersionEnum.getCode(), classVisitor);
        this.recordDTO = recordDTO;
        this.jdkVersionEnum = jdkVersionEnum;
        this.interfaces = new ArrayList<>();
        handlers = new ArrayList<>();

        EntranceEnums.getNeedRelationHandleEnum()
                .forEach(entrance -> {
                    Class<? extends BaseHandler> cls = entrance.getHandler();
                    if (cls != null) {
                        try{
                            BaseHandler handler = cls.getDeclaredConstructor().newInstance();
                            handlers.add(handler);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces){
        className = name;
        // 如果是接口的实体类，则加入interfaceRecord
        for (String interfaceClassName : interfaces) {
            if (recordDTO.getProjectRecord().isNeedInject(interfaceClassName)) {
                recordDTO.getInterfaceRecord().putInterfaceEntry(interfaceClassName, name);
                this.interfaces.add(interfaceClassName);
            }
        }
        // 如果是abstract类的实体类...
        if(recordDTO.getAbstractRecord().containAbstract(superName)){
            recordDTO.getAbstractRecord().putAbstractEntry(superName, name);
            isAbstract = true;
            this.abstractClass = superName;
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions){
        List<String> methodRelation = new CopyOnWriteArrayList<>();
        // 处理一下方法名，适配javaparser
        String signatureName = BasicUtil.getMethodSignatureName(name, descriptor);
        recordDTO.getRelationRecord().addRelationRecord(className, signatureName, methodRelation);
        // 接口实现方法处理
        interfaces.forEach(interfaceClassName -> {
            recordDTO.getInterfaceRecord().getMethod(interfaceClassName).entrySet().stream()
                    .filter(entry -> entry.getValue()) // 筛选出抽象方法
                    .forEach(entry -> {
                        String methodName = entry.getKey();
                        if (signatureName.equals(methodName)) {
                            // 添加实体类方法 -> 接口方法逆向记录
                            recordDTO.getRelationReverseRecord()
                                    .addRelationReverseRecord(className, signatureName, interfaceClassName + METHOD_SPLIT + methodName);
                        }
                    });
        });
        // 抽象类重写方法处理
        if (isAbstract){
            recordDTO.getAbstractRecord().getMethod(abstractClass).forEach((methodName, value) -> {
                if (signatureName.equals(methodName)) {
                    // 添加实体类方法 -> 抽象类方法逆向记录
                    recordDTO.getRelationReverseRecord()
                            .addRelationReverseRecord(className, signatureName, abstractClass + METHOD_SPLIT + methodName);
                }
            });
        }
        if(hasRequestMapping){
            return new RelationMethodVisitor(jdkVersionEnum, access, name, descriptor, super.visitMethod(access,name,descriptor,signature,exceptions), recordDTO, className, requestMappingValue, recordMapping);
        }else{
            // 类没有 RequestMapping 则parentPath为空的Set
            return new RelationMethodVisitor(jdkVersionEnum, access, name, descriptor, super.visitMethod(access,name,descriptor,signature,exceptions), recordDTO, className, new HashSet<>(), recordMapping);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visiable){
        if(EntranceEnums.isControllerAnnotation(descriptor)){
            // controller类
            recordDTO.getControllerRecord().putControlClass(className);
            isController = true;
        }
        if(EntranceEnums.isRequestAnnotation(descriptor)){
            hasRequestMapping = true;
            requestMappingValue = ConcurrentHashMap.newKeySet();
            Set<String> parentPath = ConcurrentHashMap.newKeySet();
            // 这里去获取类的 requestMappingValue
            return new RelationAnnotationVisitor(jdkVersionEnum, super.visitAnnotation(descriptor, visiable), requestMappingValue, parentPath);
        }
        return super.visitAnnotation(descriptor, visiable);
    }

    @Override
    public void visitEnd(){
        super.visitEnd();
    }

}
