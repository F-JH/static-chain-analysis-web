package com.analysis.core.Recorders.Entrances;

import com.analysis.core.Recorders.Recorder;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DubboRecord implements Recorder {
    private final List<String> dubboMethods;


    public DubboRecord(){
        dubboMethods = new CopyOnWriteArrayList<>();
    }
    public void putDubboMethod(String fullMethodName){
        dubboMethods.add(fullMethodName);
    }

    public boolean contains(String fullMethodName){
        return dubboMethods.contains(fullMethodName);
    }
}
