package com.analysis.core.Recorders;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ControllerRecord implements Recorder{
    private final Map<String, List<String>> controllers;

    public ControllerRecord(){
        controllers = new ConcurrentHashMap<>();
    }

    public void putControlClass(String ControlClassName){
        controllers.computeIfAbsent(ControlClassName, k -> new CopyOnWriteArrayList<>());
    }

    public void putControlMethod(String ControlClassName, String ControlMethodName){
        controllers.computeIfAbsent(ControlClassName, k -> new CopyOnWriteArrayList<>());
        controllers.get(ControlClassName).add(ControlMethodName);
    }

    public List<String> getApiFromControlClassName(String ControlClassName){
        return controllers.get(ControlClassName);
    }

    public Set<String> getControllers(){
        return controllers.keySet();
    }
}
