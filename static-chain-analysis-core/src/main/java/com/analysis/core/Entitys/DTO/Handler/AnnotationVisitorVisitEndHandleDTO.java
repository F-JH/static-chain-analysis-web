package com.analysis.core.Entitys.DTO.Handler;

import com.analysis.core.Entitys.DTO.RecordDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnnotationVisitorVisitEndHandleDTO {
    // 额外数据
    String className;
    String methodName;
    RecordDTO recordDTO;
}
