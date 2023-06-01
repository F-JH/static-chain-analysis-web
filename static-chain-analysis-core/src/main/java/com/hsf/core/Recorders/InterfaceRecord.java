package com.hsf.core.Recorders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterfaceRecord implements Recorder{
    private final Map<String, List<String>> interfaceList;
    private final Map<String, Map<String, Boolean>> interfaceMethodList;

    public InterfaceRecord(){
        interfaceList = new HashMap<>();
        interfaceMethodList = new HashMap<>();
    }

    public void putInterfaceClass(String interfaceClassName){
        interfaceList.computeIfAbsent(interfaceClassName, k -> new ArrayList<>());
        interfaceMethodList.computeIfAbsent(interfaceClassName, k -> new HashMap<>());
    }

    public void putInterfaceEntry(String interfaceClassName, String entryClassName){
        interfaceList.computeIfAbsent(interfaceClassName, k -> new ArrayList<>());
        interfaceMethodList.computeIfAbsent(interfaceClassName, k -> new HashMap<>());
        interfaceList.get(interfaceClassName).add(entryClassName);
    }

    public void putMethod(String interfaceClassName, String methodName, boolean isAbstract){
        interfaceMethodList.get(interfaceClassName).put(methodName, isAbstract);
    }

    public boolean containInterface(String interfaceClassName){
        return interfaceList.containsKey(interfaceClassName);
    }

    public List<String> getEntries(String interfaceClassName){
        return interfaceList.get(interfaceClassName);
    }

    public Map<String, Boolean> getMethod(String interfaceClassName){
        return interfaceMethodList.get(interfaceClassName);
    }

    public Map<String, List<String>> getInterfaceList() {
        return interfaceList;
    }
}
