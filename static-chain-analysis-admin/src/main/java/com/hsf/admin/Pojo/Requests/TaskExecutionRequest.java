package com.hsf.admin.Pojo.Requests;

import com.hsf.admin.Enums.TaskTypeEnum;
import lombok.Data;

@Data
public class TaskExecutionRequest<T> {
    private T TaskPO;
    private TaskTypeEnum taskType;
}
