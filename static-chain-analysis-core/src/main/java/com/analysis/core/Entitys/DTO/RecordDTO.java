package com.analysis.core.Entitys.DTO;

import com.analysis.core.Recorders.Entrances.*;
import com.analysis.core.Recorders.Relation.RelationRecord;
import com.analysis.core.Recorders.Relation.RelationReverseRecord;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

import static com.analysis.tools.Config.Code.*;

@Data
public class RecordDTO {
    private InterfaceRecord interfaceRecord;
    private AbstractRecord abstractRecord;
    private DubboRecord dubboRecord;
    private ProjectRecord projectRecord;
    private ApiRecord apiRecord;
    private ControllerRecord controllerRecord;
    private RelationRecord relationRecord;
    private RelationReverseRecord relationReverseRecord;
    private KafkaRecord kafkaRecord;
    private GrpcRecord grpcRecord;

    public RecordDTO(){
        interfaceRecord = new InterfaceRecord();
        abstractRecord = new AbstractRecord();
        dubboRecord = new DubboRecord();
        projectRecord = new ProjectRecord();
        apiRecord = new ApiRecord();
        controllerRecord = new ControllerRecord();
        relationRecord = new RelationRecord();
        relationReverseRecord = new RelationReverseRecord();
        kafkaRecord = new KafkaRecord();
        grpcRecord = new GrpcRecord();
    }

    /*
        传入方法名，检查是否是入口函数
     */
    public Entrance checkEntrance(String className, String methodName){
        String fullMethodName = className + METHOD_SPLIT + methodName;
        // http
        if (apiRecord.getApis(fullMethodName) != null && !apiRecord.getApis(fullMethodName).isEmpty()){
            return new Entrance(HTTP, apiRecord.getApis(fullMethodName));
        }

        // dubbo
        if (dubboRecord.contains(fullMethodName)){
            return new Entrance(DUBBO, Set.of(fullMethodName));
        }

        // Kafka
        if (kafkaRecord.contains(fullMethodName)){
            String topic = kafkaRecord.getKafkaTopic(fullMethodName);
            return new Entrance(KAFKA, Set.of(topic));
        }

        // grpc
        if (grpcRecord.contains(fullMethodName)) {
            return new Entrance(GRPC, Set.of(fullMethodName));
        }

        return null;
    }

    @Data
    @AllArgsConstructor
    public static class Entrance{
        private String type;
        private Set<String> value;
    }
}
