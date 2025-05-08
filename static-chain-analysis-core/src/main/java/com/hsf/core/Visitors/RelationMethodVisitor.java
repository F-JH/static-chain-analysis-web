package com.hsf.core.Visitors;

import com.hsf.core.Enums.JdkVersionEnum;
import com.hsf.core.Recorders.ApiRecord;
import com.hsf.core.Recorders.ControllerRecord;
import com.hsf.core.Recorders.ProjectRecord;
import com.hsf.tools.Utils.BasicUtil;
import com.hsf.tools.Utils.FileUtil;
import com.hsf.tools.Utils.FilterUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hsf.tools.Config.Code.METHOD_SPLIT;

public class RelationMethodVisitor extends AdviceAdapter{
    private final String className;
    private final String methodName;
    private final String desc;
    private final ApiRecord apiRecord;
    private final ProjectRecord projectRecord;
    private final ControllerRecord controllerRecord;
    private final List<String> relationShip;
    private final Set<String> parentPath;
    private Set<String> requestMappingValue;
    private final Map<String, Set<String>> recordMapping;
    private final JdkVersionEnum jdkVersionEnum;

    public RelationMethodVisitor(
            JdkVersionEnum jdkVersionEnum,
        int access, String methodName, String desc, MethodVisitor mv, ProjectRecord projectRecord, ApiRecord apiRecord,
        ControllerRecord controllerRecord, List<String> relationShip, String className, Set<String> parentPath, Map<String,
        Set<String>> recordMapping
    ){
        super(jdkVersionEnum.getCode(), mv, access, methodName, desc);
        this.methodName = methodName;
        this.apiRecord = apiRecord;
        this.projectRecord = projectRecord;
        this.controllerRecord = controllerRecord;
        this.relationShip = relationShip;
        this.desc = desc;
        this.className = className;
        this.parentPath = parentPath;
        this.recordMapping = recordMapping;
        this.jdkVersionEnum = jdkVersionEnum;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descript, boolean isInterface){
        String fullMethodName = owner + METHOD_SPLIT + BasicUtil.getMethodSignatureName(name, descript);
        if(projectRecord.isNeedInject(owner) && projectRecord.isNeedInjectMethod(fullMethodName) && !relationShip.contains(fullMethodName))
            relationShip.add(fullMethodName);
        super.visitMethodInsn(opcode, owner, name, descript, isInterface);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visiable){
        if(FilterUtils.isRequestAnnotation(descriptor)){
            // request的方法
            controllerRecord.putControlMethod(className, BasicUtil.getMethodSignatureName(methodName, desc));
            requestMappingValue = new HashSet<>();
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
            apiRecord.putApi(fullMethodName, requestMappingValue);
        }
        super.visitEnd();
    }
}
