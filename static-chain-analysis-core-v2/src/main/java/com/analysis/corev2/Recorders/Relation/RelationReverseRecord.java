package com.analysis.corev2.Recorders.Relation;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/*
    调用关系逆向记录
 */
@Data
public class RelationReverseRecord {
    private final Map<String, Map<String, List<String>>> relationReverseRecord = new ConcurrentHashMap<>();

    public void addRelationReverseRecord(String cls, String method, String relationMethod) {
        relationReverseRecord.computeIfAbsent(cls, k -> new ConcurrentHashMap<>());
        relationReverseRecord.get(cls).computeIfAbsent(method, k -> new CopyOnWriteArrayList<>()).add(relationMethod);
    }

    public List<String> getRelationReverseRecord(String cls, String method) {
        if (relationReverseRecord.get(cls) == null) {
            return null;
        }
        return relationReverseRecord.get(cls).getOrDefault(method, null);
    }
}
