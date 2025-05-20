package com.analysis.admin.TaskCore;

import com.analysis.admin.Code.Response;
import com.analysis.admin.Code.TaskStatus;
import com.analysis.admin.Dto.Task.PullTaskDTO;
import com.analysis.admin.Enums.TaskTypeEnum;
import com.analysis.admin.Pojo.Requests.TaskExecutionRequest;
import com.analysis.admin.Pojo.Responses.TaskExecutionResponse;
import com.analysis.admin.TaskCore.Base.BaseTaskExecutor;
import com.analysis.tools.gittool.GitUtils;
import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Slf4j
@Component
public class PullTask extends BaseTaskExecutor {

    public PullTask() {
        super(TaskTypeEnum.PULL_TASK);
    }

    @Override
    public Response<TaskExecutionResponse> execute(TaskExecutionRequest<?> request) {
        PullTaskDTO pullTaskDTO = (PullTaskDTO) request.getTaskPO();
        TaskExecutionResponse response = new TaskExecutionResponse();

        File dir = new File(pullTaskDTO.getProjectDir() + File.separator + "master");
        pullTaskDTO.getTaskInfo().setStatus(TaskStatus.RUNNING.code);
        pullTaskDTO.getTaskInfoMapper().updateTaskInfo(pullTaskDTO.getTaskInfo());
        log.info("[" + pullTaskDTO.getTaskInfo().getId() + "]" + "git pull " + pullTaskDTO.getProjectDir());

        try{
            if (!StringUtils.isNullOrEmpty(pullTaskDTO.getPublicKey())
                            && !StringUtils.isNullOrEmpty(pullTaskDTO.getPrivateKey())){
                // ssh
                GitUtils.pullWithSshKey(dir, pullTaskDTO.getUsername(),
                        pullTaskDTO.getPublicKey(), pullTaskDTO.getPrivateKey(), pullTaskDTO.getPassphrase()
                );
            } else if (!StringUtils.isNullOrEmpty(pullTaskDTO.getPassword())) {
                // user-password
                UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                        pullTaskDTO.getUsername(), pullTaskDTO.getPassword()
                );
                GitUtils.pull(dir, credentialsProvider);
            } else {
                throw new IllegalArgumentException("No credentials provided for git pull");
            }
            pullTaskDTO.getTaskInfo().setStatus(TaskStatus.SUCCESS.code);
            pullTaskDTO.getProjectInfo().setLastSyncTime(new Date());
            pullTaskDTO.getProjectInfoMapper().updateProjectInfo(pullTaskDTO.getProjectInfo());
            pullTaskDTO.getTaskInfoMapper().updateTaskInfo(pullTaskDTO.getTaskInfo());
            log.info("[" + pullTaskDTO.getTaskInfo().getId() + "]" + "git pull " + pullTaskDTO.getProjectDir() + " success");
        }catch (GitAPIException | IOException | IllegalArgumentException e){
            log.error("[" + pullTaskDTO.getTaskInfo().getId() + "]" + "git pull " + pullTaskDTO.getProjectDir() + " error: " + e.getMessage());
            pullTaskDTO.getTaskInfo().setStatus(TaskStatus.FAILD.code);
            pullTaskDTO.getTaskInfoMapper().updateTaskInfo(pullTaskDTO.getTaskInfo());
        }
        return Response.success(response);
    }
}
