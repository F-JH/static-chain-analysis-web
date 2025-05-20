package com.analysis.corev2.Recorders.Relation;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class RelationRecord {
    private final Map<String, Map<String, List<String>>> relationRecord = new ConcurrentHashMap<>();

    public void addRelationRecord(String cls, String method, List<String> relationMethod) {
        relationRecord.computeIfAbsent(cls, k -> new ConcurrentHashMap<>());
        relationRecord.get(cls).put(method, relationMethod);
    }

    public List<String> getRelationRecord(String cls, String method) {
        if (relationRecord.get(cls) == null) {
            return null;
        }
        return relationRecord.get(cls).getOrDefault(method, null);
    }
}
