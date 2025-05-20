package com.analysis.corev2.Entitys.DTO.Handler;

import com.analysis.corev2.Entitys.DTO.RecordDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MethodHandleDTO {
    // 基本数据
    int access;
    String name;
    String descriptor;
    String signature;
    String[] exceptions;

    // 额外数据
    String className;
    RecordDTO recordDTO;
    boolean isInterface;
    boolean isAbstract;
}
