package com.analysis.admin.TaskCore.Base;

import com.analysis.admin.Code.Response;
import com.analysis.admin.Pojo.Requests.TaskExecutionRequest;
import com.analysis.admin.Pojo.Responses.TaskExecutionResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
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
    public Response<TaskExecutionResponse> call() {
        try{
            return taskExecutor.execute(taskExecutionRequest);
        }catch (Exception e){
            log.error("Task execution failed: {}", e.getMessage(), e);
            return Response.failed(null, e.getMessage());
        }
    }
}
