package com.analysis.core.Handler;

import com.analysis.core.Entitys.DTO.Handler.ClassVisitorVisitAnnotationHandleDTO;
import com.analysis.core.Entitys.DTO.Handler.ClassVisitorVisitMethodHandleDTO;
import com.analysis.core.Entitys.DTO.Handler.HandleDTO;
import com.analysis.core.Enums.EntranceEnums;
import com.analysis.tools.Utils.BasicUtil;

import static com.analysis.tools.Config.Code.METHOD_SPLIT;

public class DubboHandler extends AbstractHandler{
    private boolean isDubbo = false;

    @Override
    public void recordClassVisitAnnotationHandle(HandleDTO<?> handleDTO) {
        ClassVisitorVisitAnnotationHandleDTO classAnnotationHandleDTO = (ClassVisitorVisitAnnotationHandleDTO) handleDTO.getHandleData();
        isDubbo = EntranceEnums.isDubboAnnotation(classAnnotationHandleDTO.getDescriptor());
    }

    @Override
    public void recordClassVisitMethodHandle(HandleDTO<?> handleDTO) {
        if (isDubbo){
            ClassVisitorVisitMethodHandleDTO methodHandleDTO = (ClassVisitorVisitMethodHandleDTO) handleDTO.getHandleData();
            String methodName = BasicUtil.getMethodSignatureName(methodHandleDTO.getName(), methodHandleDTO.getDescriptor());
            methodHandleDTO.getRecordDTO().getDubboRecord()
                    .putDubboMethod(methodHandleDTO.getClassName() + METHOD_SPLIT + methodName);
        }
    }
}
