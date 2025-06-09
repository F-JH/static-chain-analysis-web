package com.analysis.core.Entitys.DTO.Handler;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class MethodVisitorVisitEndHandleDTO {
    String className;
    String methodName;
    final Set<String> parentPaths;

}
