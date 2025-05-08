package com.hsf.admin.TaskCore;

import com.hsf.admin.Code.Response;
import com.hsf.admin.Code.TaskStatus;
import com.hsf.admin.Dto.Task.CopyTaskDTO;
import com.hsf.admin.Enums.TaskTypeEnum;
import com.hsf.admin.Pojo.Requests.TaskExecutionRequest;
import com.hsf.admin.Pojo.Responses.TaskExecutionResponse;
import com.hsf.admin.TaskCore.Base.BaseTaskExecutor;
import com.hsf.tools.Utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class CopyTask extends BaseTaskExecutor {
    public CopyTask() {
        super(TaskTypeEnum.COPY_TASK);
    }

    @Override
    public Response<TaskExecutionResponse> execute(TaskExecutionRequest<?> request) {
        CopyTaskDTO copyTaskDTO = (CopyTaskDTO) request.getTaskPO();
        TaskExecutionResponse response = new TaskExecutionResponse();

        log.info("copy " + copyTaskDTO.getSrcPath() + " to " + copyTaskDTO.getDstPath());
        File srcDir = new File(copyTaskDTO.getSrcPath());
        File dstDir = new File(copyTaskDTO.getDstPath());
        if (!srcDir.exists() ){
            throw new RuntimeException(copyTaskDTO.getSrcPath() + " do not exists!");
        }
        try{
            if (dstDir.exists()){
                // 不管，先删除掉原来的
                FileUtils.deleteDirectory(dstDir);
            }
            copyTaskDTO.getTaskInfo().setStatus(TaskStatus.RUNNING.code);
            copyTaskDTO.getTaskInfoMapper().updateTaskInfo(copyTaskDTO.getTaskInfo());
            FileUtil.copyDirectory(srcDir, dstDir);
            // 复制完成，更新task_info
            copyTaskDTO.getTaskInfo().setStatus(TaskStatus.SUCCESS.code);
            copyTaskDTO.getTaskInfoMapper().updateTaskInfo(copyTaskDTO.getTaskInfo());
            // 回调
            if (copyTaskDTO.getCallBack() != null){
                copyTaskDTO.getCallBack().run();
            }
        }catch (IOException e){
            throw new RuntimeException("copy error!");
        }
        return Response.success(response);
    }
}
