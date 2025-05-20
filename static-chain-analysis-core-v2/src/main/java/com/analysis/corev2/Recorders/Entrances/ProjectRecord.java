package com.analysis.corev2.Recorders.Entrances;

import com.analysis.corev2.Recorders.Recorder;
import lombok.Getter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ProjectRecord implements Recorder {
    private final Set<String> projectPackage;
    private final Set<String> projectMehtods;

    public ProjectRecord(){
        projectPackage = ConcurrentHashMap.newKeySet();
        projectMehtods = ConcurrentHashMap.newKeySet();
    }

    public void addProjectPackage(String className){
        projectPackage.add(className);
    }
    public void addProjectMethod(String fullMethodName){
        projectMehtods.add(fullMethodName);
    }

    public boolean isNeedInject(String className){
        if(null == className)
            return false;
        for(String prefix : projectPackage){
            if(className.equals(prefix))
                return true;
        }
        return false;
    }

    public boolean isNeedInjectMethod(String methodName){
        if(null == methodName)
            return false;
        return projectMehtods.contains(methodName);
    }
}
