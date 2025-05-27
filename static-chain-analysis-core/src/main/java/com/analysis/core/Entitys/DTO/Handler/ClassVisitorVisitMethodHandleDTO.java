package com.analysis.core.Entitys.DTO.Handler;

import com.analysis.core.Entitys.DTO.RecordDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassVisitorVisitMethodHandleDTO {
    int access;
    String name;
    String descriptor;
    String signature;
    String[] exceptions;

    String className;
    RecordDTO recordDTO;
    boolean isInterface;
    boolean isAbstract;
}
