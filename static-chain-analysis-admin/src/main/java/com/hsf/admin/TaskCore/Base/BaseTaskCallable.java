package com.hsf.admin.TaskCore.Base;

import com.hsf.admin.Code.Response;
import com.hsf.admin.Pojo.Requests.TaskExecutionRequest;
import com.hsf.admin.Pojo.Responses.TaskExecutionResponse;

import java.util.concurrent.Callable;

public class BaseTaskCallable implements Callable<Response<TaskExecutionResponse>> {
    private final TaskExecutionRequest taskExecutionRequest;
    private final BaseTaskExecutor taskExecutor;

    public BaseTaskCallable(
            TaskExecutionRequest request,
            BaseTaskExecutor taskExecutor
    ) {
        taskExecutionRequest = request;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public Response<TaskExecutionResponse> call() throws Exception {
        return taskExecutor.execute(taskExecutionRequest);
    }
}
