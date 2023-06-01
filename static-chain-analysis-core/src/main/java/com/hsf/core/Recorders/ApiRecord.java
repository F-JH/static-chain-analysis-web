package com.hsf.core.Recorders;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApiRecord implements Recorder{
    private final Map<String, Set<String>> record;

    public ApiRecord(){
        record = new HashMap<>();
    }

    public void putApi(String fullMethodName, Set<String> api){
//        record.computeIfAbsent(fullMethodName, k -> new HashSet<>());
        record.put(fullMethodName, api);
    }

    public Set<String> getApis(String fullMethodName){
        return record.get(fullMethodName);
    }
}
