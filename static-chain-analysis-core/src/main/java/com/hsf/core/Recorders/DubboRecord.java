package com.hsf.core.Recorders;

import java.util.ArrayList;
import java.util.List;

public class DubboRecord implements Recorder{
    private final List<String> dubboMethods;


    public DubboRecord(){
        dubboMethods = new ArrayList<>();
    }
    public void putDubboMethod(String fullMethodName){
        dubboMethods.add(fullMethodName);
    }

    public boolean contains(String fullMethodName){
        return dubboMethods.contains(fullMethodName);
    }

    public List<String> getList(){
        return dubboMethods;
    }
}
