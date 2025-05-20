package com.analysis.corev2.Recorders.Entrances;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class KafkaRecord {
    private final List<String> kafkaMethods;

    public KafkaRecord(){
        kafkaMethods = new CopyOnWriteArrayList<>();
    }

    public void putKafkaMethod(String fullMethodName){
        kafkaMethods.add(fullMethodName);
    }

    public boolean contains(String fullMethodName){
        return kafkaMethods.contains(fullMethodName);
    }
}
