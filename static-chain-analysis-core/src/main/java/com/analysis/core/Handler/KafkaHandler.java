package com.analysis.core.Handler;

import com.analysis.core.Entitys.DTO.Handler.*;
import com.analysis.core.Enums.EntranceEnums;
import com.analysis.core.Recorders.Entrances.KafkaRecord;

import java.util.ArrayList;
import java.util.List;

import static com.analysis.tools.Config.Code.METHOD_SPLIT;

public class KafkaHandler extends AbstractHandler{
    private boolean isKafka = false;
    private boolean isVisitTopic = false;
    private List<String> topics;

    @Override
    public void recordMethodVisitAnnotationHandle(HandleDTO<?> handleDTO) {
        MethodVisitorVisitAnnotationHandleDTO methodAnnotationHandleDTO = (MethodVisitorVisitAnnotationHandleDTO) handleDTO.getHandleData();
        if(EntranceEnums.isKafkaAnnotation(methodAnnotationHandleDTO.getDescriptor())){
            // 因为先执行的visitMethod再执行到这，所以不会有下游了，在这里直接记录kafka方法
            // 补充：其实还会有下游，因为在这里还拿不到具体的topic名，要到 AnnotationVisitor 里才拿得到
            isKafka = true;
            KafkaRecord kafkaRecord = methodAnnotationHandleDTO.getRecordDTO().getKafkaRecord();
            kafkaRecord.putKafkaMethod(methodAnnotationHandleDTO.getClassName() + METHOD_SPLIT + methodAnnotationHandleDTO.getMethodName());
        }
    }

    @Override
    public void recordAnnotationVisitArrayHandle(HandleDTO<?> handleDTO){
        AnnotationVisitorArrayHandleDTO annotationVisitorArrayHandleDTO = (AnnotationVisitorArrayHandleDTO) handleDTO.getHandleData();
        if (isKafka && annotationVisitorArrayHandleDTO.getName().equals("topics")){
            // 这里的name是参数的名称
            isVisitTopic = true;
            topics = new ArrayList<>();
        }
    }

    @Override
    public void recordAnnotationVisitHandle(HandleDTO<?> handleDTO){
        if (isKafka){
            // 这里的name是参数的名称
            AnnotationVisitorVisitHandleDTO annotationVisitorArrayHandleDTO = (AnnotationVisitorVisitHandleDTO) handleDTO.getHandleData();
            String name = annotationVisitorArrayHandleDTO.getName();
            // 如果上一级是数组，那么这里的name会是null
            if (name == null && isVisitTopic){
                // 理论上 visitArray("topic") 后会立刻 visit(topicName),所以这里可以认为是在进入topic名
                // 这里的value是topic的名称
                if (topics != null && annotationVisitorArrayHandleDTO.getValue() != null){
                    topics.add((String) annotationVisitorArrayHandleDTO.getValue());
                }
            }
        }
    }

    @Override
    public void recordAnnotationVisitEndHandle(HandleDTO<?> handleDTO){
        if (isKafka && isVisitTopic){
            // 到这里代表着遍历topic数组结束了，重置标志位防止其他参数误入topic
            // 并将topics记录下来
            isVisitTopic = false;
            AnnotationVisitorVisitEndHandleDTO annotationVisitorVisitEndHandleDTO = (AnnotationVisitorVisitEndHandleDTO) handleDTO.getHandleData();
            topics.forEach(topic -> {
                KafkaRecord kafkaRecord = annotationVisitorVisitEndHandleDTO.getRecordDTO().getKafkaRecord();
                kafkaRecord.putKafkaTopic(annotationVisitorVisitEndHandleDTO.getClassName() + METHOD_SPLIT + annotationVisitorVisitEndHandleDTO.getMethodName(), topic);
            });
        }
    }
}
