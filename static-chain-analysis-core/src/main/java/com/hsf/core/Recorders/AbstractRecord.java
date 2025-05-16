package com.hsf.core.Recorders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AbstractRecord implements Recorder{

    private final Map<String, List<String>> record;
    private final Map<String, Map<String, Boolean>> abstractMethodList;

    public AbstractRecord(){
        record = new ConcurrentHashMap<>();
        abstractMethodList = new ConcurrentHashMap<>();
    }

    public void putAbstractClass(String className){
        record.computeIfAbsent(className, k -> new CopyOnWriteArrayList<>());
        abstractMethodList.computeIfAbsent(className, k -> new ConcurrentHashMap<>());
    }

    public void putAbstractEntry(String abstractClassName, String entryClassName){
        record.computeIfAbsent(abstractClassName, k -> new CopyOnWriteArrayList<>());
        abstractMethodList.computeIfAbsent(abstractClassName, k -> new ConcurrentHashMap<>());
        record.get(abstractClassName).add(entryClassName);
    }

    public void putMethod(String abstractClassName, String methodName, boolean isAbstract){
        abstractMethodList.get(abstractClassName).put(methodName, isAbstract);
    }
    public boolean containAbstract(String abstractClassName){
        return record.containsKey(abstractClassName);
    }
    public List<String> getEntries(String className){
        return record.get(className);
    }
    public Map<String, Boolean> getMethod(String abstractClassName){
        return abstractMethodList.get(abstractClassName);
    }
}
