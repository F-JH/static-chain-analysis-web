package com.hsf.admin.Service;

import com.alibaba.fastjson.JSON;
import com.hsf.admin.Code.TaskType;
import com.hsf.admin.Dto.Task.CloneTaskDTO;
import com.hsf.admin.Dto.Task.PullTaskDTO;
import com.hsf.admin.Enums.TaskTypeEnum;
import com.hsf.admin.Pojo.Entities.ProjectInfo;
import com.hsf.admin.Pojo.Requests.TaskExecutionRequest;
import com.hsf.admin.Mapper.*;
import com.hsf.admin.Pojo.Entities.FetchInfo;
import com.hsf.admin.Pojo.Entities.TaskInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;


@Service
@Slf4j
public class FetchProjectTaskService {

    private

    @Autowired
    CredentialInfoMapper credentialInfoMapper;

    @Autowired
    ProjectInfoMapper projectInfoMapper;

    @Autowired
    TreeInfoMapper treeInfoMapper;

    @Autowired
    FetchMapper fetchMapper;

    @Autowired
    TaskInfoMapper taskInfoMapper;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Resource
    TaskExecutionService taskExecutionService;

    @Value("${env.tmpDir}")
    private String tmpDir;

    public void pullCode(FetchInfo fetchInfo, ProjectInfo projectInfo, TaskInfo taskInfo){
        TaskExecutionRequest<PullTaskDTO> request = new TaskExecutionRequest<>();
        request.setTaskType(TaskTypeEnum.PULL_TASK);
        request.setTaskPO(
                PullTaskDTO.builder()
                        .username(fetchInfo.getCredentialUsername())
                        .password(fetchInfo.getCredentialPassword())
                        .publicKey(fetchInfo.getPublicKey())
                        .privateKey(fetchInfo.getPrivateKey())
                        .passphrase(fetchInfo.getPassphrase())
                        .projectDir(projectInfo.getPath())
                        .projectInfo(projectInfo)
                        .projectInfoMapper(projectInfoMapper)
                        .taskInfo(taskInfo)
                        .taskInfoMapper(taskInfoMapper)
                        .build()
        );
        taskExecutionService.createAsyncTask(request);
    }

    public void cloneCode(FetchInfo fetchInfo, TaskInfo taskInfo, Integer nodeId){
        TaskExecutionRequest<CloneTaskDTO> request = new TaskExecutionRequest<>();
        request.setTaskType(TaskTypeEnum.CLONE_TASK);
        request.setTaskPO(
                CloneTaskDTO.builder()
                        .gitUrl(fetchInfo.getGitUrl())
                        .tmpDir(tmpDir)
                        .username(fetchInfo.getCredentialUsername())
                        .password(fetchInfo.getCredentialPassword())
                        .publicKey(fetchInfo.getPublicKey())
                        .privateKey(fetchInfo.getPrivateKey())
                        .passphrase(fetchInfo.getPassphrase())
                        .taskInfo(taskInfo)
                        .projectId(nodeId)
                        .fetchMapper(fetchMapper)
                        .taskInfoMapper(taskInfoMapper)
                        .projectInfoMapper(projectInfoMapper)
                        .build()
        );
        taskExecutionService.createAsyncTask(request);
    }

    public Integer cloneOrPull(Integer nodeId){
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setNodeId(nodeId);
        FetchInfo fetchInfo = fetchMapper.getFetchInfo(nodeId);
        // 检查是否存在项目
        ProjectInfo projectInfo = projectInfoMapper.getProjectInfo(nodeId);
        if (projectInfo != null && projectInfo.getPath() != null){
            File project = new File(projectInfo.getPath());
            if (project.exists() && Objects.requireNonNull(project.listFiles()).length != 0){
                taskInfo.setType(TaskType.PULL.code);
                taskInfoMapper.initTask(taskInfo);
                pullCode(fetchInfo, projectInfo, taskInfo);
            }else{
                taskInfo.setType(TaskType.CLONE.code);
                taskInfoMapper.initTask(taskInfo);
                cloneCode(fetchInfo, taskInfo, nodeId);
            }
        }else {
            log.info(JSON.toJSONString(fetchInfo));
            taskInfo.setType(TaskType.CLONE.code);
            taskInfoMapper.initTask(taskInfo);

            cloneCode(fetchInfo, taskInfo, nodeId);
        }
        return taskInfo.getId();
    }
}
