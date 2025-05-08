package com.hsf.admin.TaskCore.Base;

import com.hsf.admin.Code.Response;
import com.hsf.admin.Enums.TaskTypeEnum;
import com.hsf.admin.Pojo.Requests.TaskExecutionRequest;
import com.hsf.admin.Pojo.Responses.TaskExecutionResponse;
import lombok.Data;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.Callable;

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
