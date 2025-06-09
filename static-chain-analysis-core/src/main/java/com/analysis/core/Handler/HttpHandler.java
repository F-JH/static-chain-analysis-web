package com.analysis.core.Handler;

import com.analysis.core.Entitys.DTO.Handler.*;
import com.analysis.core.Enums.EntranceEnums;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static com.analysis.tools.Config.Code.METHOD_SPLIT;

public class HttpHandler extends AbstractHandler{
    // 是否正在解析方法上的http注解
    private boolean isVisitPath = false;
    // 是否正在读注解里的 `String[] value()` 这个注解参数
    private boolean isValue = false;
    private boolean hasValue = false;
    private boolean isVisitArray = false;
    private boolean isVisitBaseRequestMapping = false;

    @Override
    public void recordClassVisitAnnotationHandle(HandleDTO<?> handleDTO){
        ClassVisitorVisitAnnotationHandleDTO classVisitorVisitAnnotationHandleDTO =
            (ClassVisitorVisitAnnotationHandleDTO) handleDTO.getHandleData();
        if (EntranceEnums.isControllerAnnotation(classVisitorVisitAnnotationHandleDTO.getDescriptor())) {
            classVisitorVisitAnnotationHandleDTO.getRecordDTO()
                    .getControllerRecord().putControlClass(classVisitorVisitAnnotationHandleDTO.getClassName());
        }
        isVisitBaseRequestMapping = EntranceEnums.isRequestAnnotation(classVisitorVisitAnnotationHandleDTO.getDescriptor());
    }

    @Override
    public void recordMethodVisitAnnotationHandle(HandleDTO<?> handleDTO) {
        MethodVisitorVisitAnnotationHandleDTO methodVisitorVisitAnnotationHandleDTO =
            (MethodVisitorVisitAnnotationHandleDTO) handleDTO.getHandleData();
        if (EntranceEnums.isRequestAnnotation(methodVisitorVisitAnnotationHandleDTO.getDescriptor())){
            isVisitPath = true;
            methodVisitorVisitAnnotationHandleDTO.getRecordDTO().getControllerRecord()
                    .putControlMethod(methodVisitorVisitAnnotationHandleDTO.getClassName(), methodVisitorVisitAnnotationHandleDTO.getMethodName());
        }
    }

    @Override
    public void recordAnnotationVisitArrayHandle(HandleDTO<?> handleDTO){
        isVisitArray = true;
        // 处理注解数组
        AnnotationVisitorArrayHandleDTO annotationVisitorArrayHandleDTO =
            (AnnotationVisitorArrayHandleDTO) handleDTO.getHandleData();
        // 从类注解中进来的处理
        isValue = annotationVisitorArrayHandleDTO.getName().equals("value");
        if (!hasValue){
            hasValue = annotationVisitorArrayHandleDTO.getName().equals("value");
        }
    }

    @Override
    public void recordAnnotationVisitHandle(HandleDTO<?> handleDTO){
        AnnotationVisitorVisitHandleDTO annotationVisitorVisitHandleDTO = (AnnotationVisitorVisitHandleDTO) handleDTO.getHandleData();
        if (isVisitBaseRequestMapping && isValue){
            // 从类注解进来
            annotationVisitorVisitHandleDTO.getParentPaths().add((String) annotationVisitorVisitHandleDTO.getValue());
        }

        if (isVisitPath && isValue) {
            Set<String> parentPaths = annotationVisitorVisitHandleDTO.getParentPaths();
            if (parentPaths.isEmpty()) {
                // 如果没有父级路径，则认为是当前方法的路径
                String path = (String) annotationVisitorVisitHandleDTO.getValue();
                annotationVisitorVisitHandleDTO.getRecordDTO().getApiRecord()
                        .putApi(annotationVisitorVisitHandleDTO.getClassName() + METHOD_SPLIT + annotationVisitorVisitHandleDTO.getMethodName(), path);
            } else {
                // 如果有父级路径，则将当前路径与父级路径拼接
                for (String parentPath : parentPaths) {
                    Path fullPath = Paths.get(parentPath, (String) annotationVisitorVisitHandleDTO.getValue());
                    // 处理逻辑，比如记录到某个数据结构中
                    annotationVisitorVisitHandleDTO.getRecordDTO().getApiRecord()
                            .putApi(annotationVisitorVisitHandleDTO.getClassName() + METHOD_SPLIT + annotationVisitorVisitHandleDTO.getMethodName(), fullPath.toString());
                }
            }
        }
    }

    @Override
    public void recordAnnotationVisitEndHandle(HandleDTO<?> handleDTO) {
        // 处理注解结束
        AnnotationVisitorVisitEndHandleDTO annotationVisitorVisitEndHandleDTO = (AnnotationVisitorVisitEndHandleDTO) handleDTO.getHandleData();
        // 每一个注解参数中，存在数组类型的参数时，会通过 annotationVisitor0.visitArray 来获取参数级别的 annotationVisitor1
        // 因此需要根据 isVisitArray 来判断需要重置的状态
        if (isVisitArray) {
            // annotationVisitor1 重置状态
            isVisitArray = false;
            isValue = false;
        }else{
            // annotationVisitor0 重置状态
            if (!hasValue && isVisitPath){
                // 这里是处理方法上有http注解，但没有写value的情况，这时候认为其路径是继承父级也就是类注解的路径
                Set<String> parentPaths = annotationVisitorVisitEndHandleDTO.getParentPaths();
                annotationVisitorVisitEndHandleDTO.getRecordDTO().getApiRecord()
                        .putApi(annotationVisitorVisitEndHandleDTO.getClassName() + METHOD_SPLIT + annotationVisitorVisitEndHandleDTO.getMethodName(), parentPaths);
            }
            isVisitBaseRequestMapping = false;
            isVisitPath = false;
        }
    }
}
