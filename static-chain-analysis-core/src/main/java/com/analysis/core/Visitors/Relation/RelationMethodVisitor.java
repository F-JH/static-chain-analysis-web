package com.analysis.core.Visitors.Relation;

import com.analysis.core.Enums.EntranceEnums;
import com.analysis.core.Enums.JdkVersionEnum;
import com.analysis.core.Entitys.DTO.RecordDTO;
import com.analysis.tools.Utils.BasicUtil;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.analysis.tools.Config.Code.METHOD_SPLIT;

public class RelationMethodVisitor extends AdviceAdapter{
    private final String className;
    private final String methodName;
    private final String desc;
    private final RecordDTO recordDTO;
    private final Set<String> parentPath;
    private Set<String> requestMappingValue;
    private final Map<String, Set<String>> recordMapping;
    private final JdkVersionEnum jdkVersionEnum;

    private final String currentFullMethodName;

    public RelationMethodVisitor(
        JdkVersionEnum jdkVersionEnum,
        int access, String methodName, String desc, MethodVisitor mv,
        RecordDTO recordDTO, String className, Set<String> parentPath, Map<String, Set<String>> recordMapping
    ){
        super(jdkVersionEnum.getCode(), mv, access, methodName, desc);
        this.methodName = methodName;
        this.recordDTO = recordDTO;
        this.desc = desc;
        this.className = className;
        this.parentPath = parentPath;
        this.recordMapping = recordMapping;
        this.jdkVersionEnum = jdkVersionEnum;

        this.currentFullMethodName = className + METHOD_SPLIT + BasicUtil.getMethodSignatureName(methodName, desc);

    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descript, boolean isInterface){
        String fullMethodName = owner + METHOD_SPLIT + BasicUtil.getMethodSignatureName(name, descript);
        List<String> relationShip = recordDTO.getRelationRecord().getRelationRecord(className, BasicUtil.getMethodSignatureName(methodName, desc));
        if(recordDTO.getProjectRecord().isNeedInject(owner)
                && recordDTO.getProjectRecord().isNeedInjectMethod(fullMethodName)
                && !relationShip.contains(fullMethodName)){
            relationShip.add(fullMethodName);
            // 因为每个类只会检索一次，所以直接添加逆向调用关系进去，不会重复添加
            recordDTO.getRelationReverseRecord()
                    .addRelationReverseRecord(owner, BasicUtil.getMethodSignatureName(name, descript), this.currentFullMethodName);
        }
        super.visitMethodInsn(opcode, owner, name, descript, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs){
        if (bsm.getOwner().equals(EntranceEnums.LAMBDA_HANDLER.getCode()) && bsm.getName().equals("metafactory")){
            // 引导方法是 LambdaMetafactory.metafactory，直接跳过,解析bsmArgs里真正的方法
            Handle handle = (Handle) bsmArgs[1];
            String fullMethodName = handle.getOwner() + METHOD_SPLIT + BasicUtil.getMethodSignatureName(handle.getName(), handle.getDesc());
            List<String> relationShip = recordDTO.getRelationRecord().getRelationRecord(className, BasicUtil.getMethodSignatureName(methodName, this.desc));
            if(recordDTO.getProjectRecord().isNeedInject(handle.getOwner())
                    && recordDTO.getProjectRecord().isNeedInjectMethod(fullMethodName)
                    && !relationShip.contains(fullMethodName)){
                relationShip.add(fullMethodName);
                recordDTO.getRelationReverseRecord()
                        .addRelationReverseRecord(handle.getOwner(), BasicUtil.getMethodSignatureName(handle.getName(), handle.getDesc()), this.currentFullMethodName);
            }
        }
        try{
            super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        }catch (TypeNotPresentException ignored){
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visiable){
        if(EntranceEnums.isRequestAnnotation(descriptor)){
            // request的方法
            recordDTO.getControllerRecord().putControlMethod(className, BasicUtil.getMethodSignatureName(methodName, desc));
            requestMappingValue = ConcurrentHashMap.newKeySet();
            // 这里去获取方法的 requestMappingValue
            // 故而：requestMappingValue记录自己的，parentPath上一级(比如类)传过来的 requestMappingValue
            return new RelationAnnotationVisitor(jdkVersionEnum, super.visitAnnotation(descriptor, visiable), requestMappingValue, parentPath);
        }
        return super.visitAnnotation(descriptor, visiable);
    }

    @Override
    public void visitTypeInsn(int opcode, String type){
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitMaxs(int a, int b){
        try{
            super.visitMaxs(a, b);
        }catch (TypeNotPresentException e){
            return;
        }
    }

    @Override
    public void visitEnd(){
        // 处理一下Mapping
        if(requestMappingValue!=null){
            String fullMethodName = className + METHOD_SPLIT + BasicUtil.getMethodSignatureName(methodName, desc);
            recordMapping.put(fullMethodName, requestMappingValue);
            recordDTO.getApiRecord().putApi(fullMethodName, requestMappingValue);
        }
        super.visitEnd();
    }
}
