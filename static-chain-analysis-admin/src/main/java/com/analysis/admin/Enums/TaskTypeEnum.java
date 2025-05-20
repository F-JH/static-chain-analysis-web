package com.analysis.admin.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskTypeEnum {
    CLONE_TASK(0, "CloneTask", "克隆任务"),
    PULL_TASK(1, "PullTask", "拉取任务"),
    COMPILE_TASK(2, "CompileTask", "编译任务"),
    COPY_TASK(3, "CopyTask", "拷贝任务"),
    ANALYSIS_TASK(4, "AnalysisTask", "分析任务"),
    ;

    private final Integer code;
    private final String className;
    private final String desc;
}
