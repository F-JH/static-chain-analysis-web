package com.analysis.admin.TaskCore;

import com.analysis.admin.Code.Response;
import com.analysis.admin.Code.TaskStatus;
import com.analysis.admin.Dto.Task.CloneTaskDTO;
import com.analysis.admin.Enums.TaskTypeEnum;
import com.analysis.admin.Pojo.Entities.ProjectInfo;
import com.analysis.admin.Pojo.Requests.TaskExecutionRequest;
import com.analysis.admin.Pojo.Responses.TaskExecutionResponse;
import com.analysis.admin.TaskCore.Base.BaseTaskExecutor;
import com.analysis.tools.Config.Code;
import com.analysis.tools.Utils.BasicUtil;
import com.analysis.tools.gittool.GitUtils;
import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;

@Slf4j
@Component
public class CloneTask extends BaseTaskExecutor {
    public CloneTask() {
        super(TaskTypeEnum.CLONE_TASK);
    }

    @Override
    public Response<TaskExecutionResponse> execute(TaskExecutionRequest<?> request) {
        CloneTaskDTO cloneTaskDTO = (CloneTaskDTO) request.getTaskPO();
        TaskExecutionResponse response = new TaskExecutionResponse();

        // clone 任务触发于没有拉取过的项目
        String projectPath = System.getProperty("user.dir") + Code.URL_SPLIT + cloneTaskDTO.getTmpDir() + Code.URL_SPLIT + BasicUtil.getGitName(cloneTaskDTO.getGitUrl());
        File projectMasterDir = new File(projectPath + Code.URL_SPLIT + "master");
        // 先创建路径

        if (!projectMasterDir.exists()){
            projectMasterDir.mkdirs();
        }

        // 更新路径到project_git_info表，后续不管是失败还是成功，此目录都一直存在
        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setNodeId(cloneTaskDTO.getProjectId());
        projectInfo.setPath(projectPath);
        cloneTaskDTO.getProjectInfoMapper().updateProjectInfo(projectInfo);

        // 开始任务
        cloneTaskDTO.getTaskInfo().setStatus(TaskStatus.RUNNING.code);
        cloneTaskDTO.getTaskInfoMapper().updateTaskInfo(cloneTaskDTO.getTaskInfo());
        cloneTaskDTO.getFetchMapper().updateFileNodeStatus(cloneTaskDTO.getProjectId(), 2);
        log.info("[" + cloneTaskDTO.getTaskInfo().getId() + "]" + "git clone " + cloneTaskDTO.getGitUrl());

        try{
            if (cloneTaskDTO.getGitUrl().startsWith("git") && !StringUtils.isNullOrEmpty(cloneTaskDTO.getPublicKey()) && !StringUtils.isNullOrEmpty(cloneTaskDTO.getPrivateKey())){
                GitUtils.clone(cloneTaskDTO.getGitUrl(), BasicUtil.getMasterFullPath(projectPath),
                        cloneTaskDTO.getUsername(), cloneTaskDTO.getPublicKey(), cloneTaskDTO.getPrivateKey(),
                        cloneTaskDTO.getPassphrase());
            }else if (cloneTaskDTO.getGitUrl().startsWith("http") && !StringUtils.isNullOrEmpty(cloneTaskDTO.getPassword())){
                UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(cloneTaskDTO.getUsername(), cloneTaskDTO.getPassword());
                GitUtils.clone(cloneTaskDTO.getGitUrl(), BasicUtil.getMasterFullPath(projectPath), usernamePasswordCredentialsProvider);
            }else {
                throw new IllegalArgumentException("No credentials provided for git clone");
            }
            // clone成功，结果保存至数据库
            projectInfo.setLastSyncTime(new Date());
            cloneTaskDTO.getProjectInfoMapper().updateProjectInfo(projectInfo);
            cloneTaskDTO.getTaskInfo().setStatus(TaskStatus.SUCCESS.code);
            cloneTaskDTO.getTaskInfoMapper().updateTaskInfo(cloneTaskDTO.getTaskInfo());
        } catch (GitAPIException | IllegalArgumentException e) {
            log.error("[" + cloneTaskDTO.getTaskInfo().getId() + "]" + "git clone " + cloneTaskDTO.getGitUrl() + " error: " + e.getMessage());
            cloneTaskDTO.getTaskInfo().setStatus(TaskStatus.FAILD.code);
            cloneTaskDTO.getTaskInfoMapper().updateTaskInfo(cloneTaskDTO.getTaskInfo());
            cloneTaskDTO.getFetchMapper().updateFileNodeStatus(cloneTaskDTO.getProjectId(), 1);
        }
        return Response.success(response);
    }
}
