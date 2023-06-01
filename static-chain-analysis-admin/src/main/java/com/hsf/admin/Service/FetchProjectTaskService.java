package com.hsf.admin.Service;

import com.alibaba.fastjson.JSON;
import com.hsf.admin.Code.TaskType;
import com.hsf.admin.Pojo.Entities.ProjectInfo;
import com.hsf.admin.TaskCore.CloneTask;
import com.hsf.admin.Mapper.*;
import com.hsf.admin.Pojo.Entities.FetchInfo;
import com.hsf.admin.Pojo.Entities.TaskInfo;
import com.hsf.admin.TaskCore.PullTask;
import com.hsf.tools.Utils.BasicUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
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

    @Value("${env.tmpDir}")
    private String tmpDir;

    public Integer cloneOrPull(Integer nodeId){
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setNodeId(nodeId);
        FetchInfo fetchInfo = fetchMapper.getFetchInfo(nodeId);
        // 检查是否存在项目
        ProjectInfo projectInfo = projectInfoMapper.getProjectInfo(nodeId);
        if (projectInfo != null){
            File project = new File(projectInfo.getPath());
            if (project.exists() && project.listFiles().length != 0){
                taskInfo.setType(TaskType.PULL.code);
                taskInfoMapper.initTask(taskInfo);
                threadPoolExecutor.execute(new PullTask(
                    fetchInfo.getCredentialUsername(), fetchInfo.getCredentialPassword(),
                    BasicUtil.getMasterFullPath(projectInfo.getPath()), taskInfo, taskInfoMapper,
                    projectInfo, projectInfoMapper
                ));
            }
        }else {
            log.info(JSON.toJSONString(fetchInfo));
            taskInfo.setType(TaskType.CLONE.code);
            taskInfoMapper.initTask(taskInfo);

            threadPoolExecutor.execute(new CloneTask(
                fetchInfo.getGitUrl(), tmpDir, fetchInfo.getCredentialUsername(), taskInfo, nodeId, fetchInfo.getCredentialPassword(),
                fetchMapper, taskInfoMapper, projectInfoMapper
            ));
        }
        return taskInfo.getId();
    }
}
