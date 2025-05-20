package com.analysis.admin.Service;

import com.analysis.admin.Code.Response;
import com.analysis.admin.Pojo.Requests.TaskExecutionRequest;
import com.analysis.admin.Pojo.Responses.TaskExecutionResponse;
import com.analysis.admin.TaskCore.Base.BaseTaskCallable;
import com.analysis.admin.TaskCore.Base.BaseTaskExecutor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class TaskExecutionService {
    @Resource
    private ApplicationContext applicationContext;
    @Resource(name = "taskThreadPool")
    private ThreadPoolExecutor taskThreadPool;

    private final Map<String, BaseTaskExecutor> taskExecutors = new LinkedHashMap<>();

    @PostConstruct
    public void init(){
        applicationContext.getBeansOfType(BaseTaskExecutor.class)
                .forEach((name, taskExecutor) -> {
                    if (taskExecutors.containsKey(taskExecutor.getTaskType().getClassName())){
                        throw new RuntimeException("Task type already exists: " + taskExecutor.getTaskType().getClassName());
                    }
                    taskExecutors.put(taskExecutor.getTaskType().getClassName(), taskExecutor);
                });
    }

    public void createAsyncTask(TaskExecutionRequest<?> request){
        String taskType = request.getTaskType().getClassName();
        BaseTaskExecutor taskExecutor = taskExecutors.get(taskType);
        if (taskExecutor == null) {
            throw new RuntimeException("No task executor found for type: " + taskType);
        }
        Future<Response<TaskExecutionResponse>> future = taskThreadPool.submit(new BaseTaskCallable(request, taskExecutor));
        // 暂时先不管异步任务返回
    }
}
