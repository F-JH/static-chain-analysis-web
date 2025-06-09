package com.analysis.core.Entitys.DTO.Handler;

import com.analysis.core.Entitys.DTO.RecordDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class ClassVisitorVisitAnnotationHandleDTO {
    // 基本信息
    String descriptor;
    boolean visiable;
    // 额外数据
    String className;
    RecordDTO recordDTO;
    boolean isInterface;
    boolean isAbstract;
    private Set<String> requestMappingValue;
}
