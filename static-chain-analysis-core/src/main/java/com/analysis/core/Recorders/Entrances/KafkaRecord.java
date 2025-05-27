package com.analysis.core.Recorders.Entrances;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class KafkaRecord {
    private final List<String> kafkaMethods;
    private final Map<String, String> kafkaMethodMap;

    public KafkaRecord(){
        kafkaMethods = new CopyOnWriteArrayList<>();
        kafkaMethodMap = new ConcurrentHashMap<>();
    }

    public void putKafkaMethod(String fullMethodName){
        kafkaMethods.add(fullMethodName);
    }

    public void putKafkaTopic(String fullMethodName, String topicName){
        kafkaMethodMap.put(fullMethodName, topicName);
    }

    public boolean contains(String fullMethodName){
        return kafkaMethods.contains(fullMethodName);
    }

    public String getKafkaTopic(String fullMethodName){
        return kafkaMethodMap.get(fullMethodName);
    }
}
