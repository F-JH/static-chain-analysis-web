package com.analysis.core.Entitys.DTO.Handler;

import com.analysis.core.Entitys.DTO.RecordDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MethodVisitorVisitAnnotationHandleDTO {
    String descriptor;
    boolean visiable;
    RecordDTO recordDTO;
    String className;
    String methodName;
}
