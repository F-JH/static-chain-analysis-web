package com.analysis.corev2.Handler;

import com.analysis.corev2.Entitys.DTO.Handler.ClassAnnotationHandleDTO;
import com.analysis.corev2.Entitys.DTO.Handler.HandleDTO;
import com.analysis.corev2.Entitys.DTO.Handler.MethodAnnotationHandleDTO;
import com.analysis.corev2.Enums.EntranceEnums;
import com.analysis.corev2.Recorders.Entrances.KafkaRecord;

import static com.analysis.tools.Config.Code.METHOD_SPLIT;

public class KafkaHandler implements BaseHandler{
    private boolean isKafka = false;

    @Override
    public void annotationHandle(HandleDTO<?> handleDTO) {
        // 判断是否是Kafka注解
        ClassAnnotationHandleDTO classAnnotationHandleDTO = (ClassAnnotationHandleDTO) handleDTO.getHandleData();
        isKafka = EntranceEnums.isKafkaAnnotation(classAnnotationHandleDTO.getDescriptor());
    }

    @Override
    public void methodHandle(HandleDTO<?> handleDTO) {

    }

    @Override
    public void classHandle(HandleDTO<?> handleDTO) {

    }

    @Override
    public void methodAnnotationHandle(HandleDTO<?> handleDTO) {
        MethodAnnotationHandleDTO methodAnnotationHandleDTO = (MethodAnnotationHandleDTO) handleDTO.getHandleData();
        if(EntranceEnums.isKafkaAnnotation(methodAnnotationHandleDTO.getDescriptor())){
            // 因为先执行的visitMethod再执行到这，所以不会有下游了，在这里直接记录kafka方法
            KafkaRecord kafkaRecord = methodAnnotationHandleDTO.getRecordDTO().getKafkaRecord();
            kafkaRecord.putKafkaMethod(methodAnnotationHandleDTO.getClassName() + METHOD_SPLIT + methodAnnotationHandleDTO.getMethodName());
        }
    }
}
