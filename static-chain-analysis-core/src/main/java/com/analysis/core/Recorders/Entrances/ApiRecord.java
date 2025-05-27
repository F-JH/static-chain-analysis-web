package com.analysis.core.Recorders.Entrances;

import com.analysis.core.Recorders.Recorder;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ApiRecord implements Recorder {
    private final Map<String, Set<String>> record;

    public ApiRecord(){
        record = new ConcurrentHashMap<>();
    }

    public void putApi(String fullMethodName, Set<String> api){
        record.put(fullMethodName, api);
    }

    public Set<String> getApis(String fullMethodName){
        return record.get(fullMethodName);
    }
}
