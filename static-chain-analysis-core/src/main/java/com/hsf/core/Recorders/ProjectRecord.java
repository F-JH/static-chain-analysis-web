package com.hsf.core.Recorders;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectRecord implements Recorder{
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

    public Set<String> getProjectPackage() {
        return projectPackage;
    }

    public Set<String> getProjectMehtods() {
        return projectMehtods;
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
