package com.analysis.corev2.Handler;

import com.analysis.corev2.Entitys.DTO.Handler.ClassAnnotationHandleDTO;
import com.analysis.corev2.Entitys.DTO.Handler.HandleDTO;
import com.analysis.corev2.Entitys.DTO.Handler.MethodHandleDTO;
import com.analysis.corev2.Enums.EntranceEnums;
import com.analysis.corev2.Recorders.Entrances.DubboRecord;
import com.analysis.tools.Utils.BasicUtil;

import static com.analysis.tools.Config.Code.METHOD_SPLIT;

public class DubboHandler implements BaseHandler{
    private boolean isDubbo = false;

    @Override
    public void annotationHandle(HandleDTO<?> handleDTO) {
        ClassAnnotationHandleDTO classAnnotationHandleDTO = (ClassAnnotationHandleDTO) handleDTO.getHandleData();
        isDubbo = EntranceEnums.isDubboAnnotation(classAnnotationHandleDTO.getDescriptor());
    }

    @Override
    public void methodHandle(HandleDTO<?> handleDTO) {
        if (isDubbo){
            MethodHandleDTO methodHandleDTO = (MethodHandleDTO) handleDTO.getHandleData();
            String methodName = BasicUtil.getMethodSignatureName(methodHandleDTO.getName(), methodHandleDTO.getDescriptor());
            methodHandleDTO.getRecordDTO().getDubboRecord()
                    .putDubboMethod(methodHandleDTO.getClassName() + METHOD_SPLIT + methodName);
        }
    }

    @Override
    public void classHandle(HandleDTO<?> handleDTO) {

    }

    @Override
    public void methodAnnotationHandle(HandleDTO<?> handleDTO) {

    }
}
