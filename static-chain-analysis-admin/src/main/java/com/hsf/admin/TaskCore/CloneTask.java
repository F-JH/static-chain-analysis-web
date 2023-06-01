package com.hsf.admin.TaskCore;

import com.hsf.admin.Code.TaskStatus;
import com.hsf.admin.Mapper.FetchMapper;
import com.hsf.admin.Mapper.ProjectInfoMapper;
import com.hsf.admin.Mapper.TaskInfoMapper;
import com.hsf.admin.Pojo.Entities.ProjectInfo;
import com.hsf.admin.Pojo.Entities.TaskInfo;
import com.hsf.tools.Config.Code;
import com.hsf.tools.Utils.BasicUtil;
import com.hsf.tools.gittool.GitUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.Date;

@Slf4j
public class CloneTask implements Runnable{
    private String gitUrl;
    private String path;
    private String username;
    private String password;
    private TaskInfo taskInfo;
    private Integer projectId; // nodeId
    private FetchMapper fetchMapper;
    private TaskInfoMapper taskInfoMapper;
    private ProjectInfoMapper projectInfoMapper;
    private String tmpDir;

    public CloneTask(
        String gitUrl, String tmpDir, String username, TaskInfo taskInfo, Integer projectId, String password, FetchMapper fetchMapper,
        TaskInfoMapper taskInfoMapper, ProjectInfoMapper projectInfoMapper
    ){
        this.tmpDir = tmpDir;
        this.gitUrl = gitUrl;
        this.username = username;
        this.password = password;
        this.taskInfo = taskInfo;
        this.projectId = projectId;
        this.fetchMapper = fetchMapper;
        this.taskInfoMapper = taskInfoMapper;
        this.projectInfoMapper = projectInfoMapper;
    }
    @Override
    public void run() {
        // clone 任务触发于没有拉取过的项目
        UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
        try {
            String projectPath = System.getProperty("user.dir") + Code.URL_SPLIT + tmpDir + Code.URL_SPLIT + BasicUtil.getGitName(gitUrl);
            File projectMasterDir = new File(projectPath + Code.URL_SPLIT + "master");
            // 先创建路径

            if (!projectMasterDir.exists()){
                projectMasterDir.mkdirs();
            }

            // 更新路径到project_git_info表，后续不管是失败还是成功，此目录都一直存在
            ProjectInfo projectInfo = new ProjectInfo();
            projectInfo.setNodeId(projectId);
            projectInfo.setPath(projectPath);
            projectInfoMapper.updateProjectInfo(projectInfo);

            // 开始任务
            taskInfo.setStatus(TaskStatus.RUNNING.code);
            taskInfoMapper.updateTaskInfo(taskInfo);
            fetchMapper.updateFileNodeStatus(projectId, 2);

            log.info("[" + taskInfo.getId() + "]" + "git clone " + gitUrl);
            GitUtils.clone(gitUrl, BasicUtil.getMasterFullPath(projectPath), usernamePasswordCredentialsProvider);
            // clone成功，结果保存至数据库
            projectInfo.setLastSyncTime(new Date());
            projectInfoMapper.updateProjectInfo(projectInfo);
            taskInfo.setStatus(TaskStatus.SUCCESS.code);
            taskInfoMapper.updateTaskInfo(taskInfo);
        } catch (GitAPIException e) {
            // clone失败，结果保存至数据库
            taskInfo.setStatus(TaskStatus.FAILD.code);
            taskInfoMapper.updateTaskInfo(taskInfo);
            fetchMapper.updateFileNodeStatus(projectId, 1);
        }
    }
}
