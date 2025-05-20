package com.analysis.admin.Pojo.Requests;

import com.analysis.admin.Enums.TaskTypeEnum;
import lombok.Data;

@Data
public class TaskExecutionRequest<T> {
    private T TaskPO;
    private TaskTypeEnum taskType;
}
