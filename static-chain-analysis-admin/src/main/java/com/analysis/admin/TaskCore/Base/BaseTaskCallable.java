package com.analysis.admin.TaskCore.Base;

import com.analysis.admin.Code.Response;
import com.analysis.admin.Pojo.Requests.TaskExecutionRequest;
import com.analysis.admin.Pojo.Responses.TaskExecutionResponse;

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
