package com.hsf.admin.TaskCore;

import com.hsf.admin.Code.TaskStatus;
import com.hsf.admin.Mapper.ProjectInfoMapper;
import com.hsf.admin.Mapper.TaskInfoMapper;
import com.hsf.admin.Pojo.Entities.ProjectInfo;
import com.hsf.admin.Pojo.Entities.TaskInfo;
import com.hsf.tools.gittool.GitUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

@Slf4j
public class PullTask implements Runnable{

    private final String username;
    private final String password;
    private final String projectDir;
    private final TaskInfo taskInfo;
    private final TaskInfoMapper taskInfoMapper;
    private final ProjectInfo projectInfo;
    private final ProjectInfoMapper projectInfoMapper;

    public PullTask(
        String username, String password, String projectDir, TaskInfo taskInfo, TaskInfoMapper taskInfoMapper,
        ProjectInfo projectInfo, ProjectInfoMapper projectInfoMapper
    ){
        this.username = username;
        this.password = password;
        this.projectDir = projectDir;
        this.taskInfo = taskInfo;
        this.taskInfoMapper = taskInfoMapper;
        this.projectInfo = projectInfo;
        this.projectInfoMapper = projectInfoMapper;
    }
    @Override
    public void run() {
        UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
        File dir = new File(projectDir);
        taskInfo.setStatus(TaskStatus.RUNNING.code);
        taskInfoMapper.updateTaskInfo(taskInfo);
        log.info("[" + taskInfo.getId() + "]" + "git pull " + projectDir);
        try {
            GitUtils.pull(dir, credentialsProvider);
            taskInfo.setStatus(TaskStatus.SUCCESS.code);
            projectInfo.setLastSyncTime(new Date());
            projectInfoMapper.updateProjectInfo(projectInfo);
            taskInfoMapper.updateTaskInfo(taskInfo);
        }catch (GitAPIException | IOException e){
            taskInfo.setStatus(TaskStatus.FAILD.code);
            taskInfoMapper.updateTaskInfo(taskInfo);
        }
    }
}
