package com.analysis.admin.TaskCore.Base;

import com.analysis.admin.Code.Response;
import com.analysis.admin.Enums.TaskTypeEnum;
import com.analysis.admin.Pojo.Requests.TaskExecutionRequest;
import com.analysis.admin.Pojo.Responses.TaskExecutionResponse;
import lombok.Data;
import org.springframework.core.task.TaskExecutor;

@Data
public abstract class BaseTaskExecutor implements TaskExecutor {

    private TaskTypeEnum taskType;

    public BaseTaskExecutor(TaskTypeEnum taskType) {
        this.taskType = taskType;
    }

    @Override
    public void execute(Runnable task) {}

    public abstract Response<TaskExecutionResponse> execute(TaskExecutionRequest<?> request);

}
