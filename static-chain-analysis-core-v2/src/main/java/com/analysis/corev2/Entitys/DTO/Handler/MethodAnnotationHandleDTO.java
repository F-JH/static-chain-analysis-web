package com.analysis.corev2.Entitys.DTO.Handler;

import com.analysis.corev2.Entitys.DTO.RecordDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MethodAnnotationHandleDTO {
    // 基本信息
    String descriptor;
    boolean visiable;
    // 额外数据
    RecordDTO recordDTO;
    String className;
    String methodName;
}
