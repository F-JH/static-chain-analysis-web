package com.hsf.core.Recorders;

import java.util.*;

public class ControllerRecord implements Recorder{
    private final Map<String, List<String>> controllers;

    public ControllerRecord(){
        controllers = new HashMap<>();
    }

    public void putControlClass(String ControlClassName){
        controllers.computeIfAbsent(ControlClassName, k -> new ArrayList<>());
    }

    public void putControlMethod(String ControlClassName, String ControlMethodName){
        controllers.computeIfAbsent(ControlClassName, k -> new ArrayList<>());
        controllers.get(ControlClassName).add(ControlMethodName);
    }

    public List<String> getApiFromControlClassName(String ControlClassName){
        return controllers.get(ControlClassName);
    }

    public Set<String> getControllers(){
        return controllers.keySet();
    }
}
