package com.analysis.admin.Service;

import com.alibaba.fastjson.JSONObject;
import com.analysis.admin.Code.TaskStatus;
import com.analysis.admin.Code.TaskType;
import com.analysis.admin.Dto.Task.AnalysisTaskDTO;
import com.analysis.admin.Dto.Task.CompileTaskDTO;
import com.analysis.admin.Dto.Task.CopyTaskDTO;
import com.analysis.admin.Enums.TaskTypeEnum;
import com.analysis.admin.Mapper.*;
import com.analysis.admin.Pojo.Entities.BranchDirInfo;
import com.analysis.admin.Pojo.Entities.CredentialInfo;
import com.analysis.admin.Pojo.Entities.ProjectInfo;
import com.analysis.admin.Pojo.Entities.TaskInfo;
import com.analysis.admin.Pojo.Requests.CompareInfo;
import com.analysis.admin.Pojo.Requests.TaskExecutionRequest;
import com.analysis.admin.TaskCore.Interface.CallBack;
import com.analysis.admin.TaskCore.Interface.PreRun;
import com.analysis.tools.Utils.BasicUtil;
import com.analysis.tools.gittool.GitUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.analysis.tools.Config.Code.URL_SPLIT;

@Service
public class TaskService {

    @Value("${env.mavenHome}")
    private String mavenHome;
    @Value("${env.javaHome}")
    private String javaHome;
    @Value("${env.mavenSettings}")
    private String mavenSettings;
    @Resource
    BranchDirInfoMapper branchDirInfoMapper;
    @Resource
    ProjectInfoMapper projectInfoMapper;
    @Resource
    TaskInfoMapper taskInfoMapper;
    @Resource
    CredentialInfoMapper credentialInfoMapper;

    @Resource
    TaskExecutionService taskExecutionService;

    private final ReentrantReadWriteLock copyBranchLock = new ReentrantReadWriteLock();
    private final ReentrantLock copyCommitLock = new ReentrantLock();
    private final ReentrantLock callAnalysisLock = new ReentrantLock();

    public TaskInfo getTaskInfo(Integer taskId){
        return taskInfoMapper.getTaskInfo(taskId);
    }

    public List<TaskInfo> getDiffTasks(Integer nodeId){
        return taskInfoMapper.getDiffTasks(nodeId);
    }

    /*
    * 从主目录复制到diff目录
    * */
    public Integer copyBranchDirectory(Integer nodeId, String branchName){
        copyBranchLock.writeLock().lock(); // 防止两个线程同时进来，发现对应的task没有在running而导致启动两个copy线程
        try{
            ProjectInfo projectInfo = projectInfoMapper.getProjectInfo(nodeId);
            BranchDirInfo check = branchDirInfoMapper.getBranchDirInfo(projectInfo.getId(), branchName);
            // 先检查是否已有同样的任务在跑
            if (check != null && check.getRunningTaskId() != null){
                copyBranchLock.writeLock().unlock();
                return check.getRunningTaskId();
            }
            System.out.println(JSONObject.toJSONString(check));

            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setType(TaskType.COPY.code);
            taskInfo.setNodeId(nodeId);
            taskInfoMapper.initTask(taskInfo);

            String branchCodePath = BasicUtil.getBranchFullPath(projectInfo.getPath(), branchName, null);

            BranchDirInfo branchDirInfo = new BranchDirInfo();
            branchDirInfo.setBranchName(branchName);
            branchDirInfo.setPath(branchCodePath);
            branchDirInfo.setProjectId(projectInfo.getId());
            branchDirInfo.setRunningTaskId(taskInfo.getId());
            branchDirInfoMapper.insertBranchDirInfo(branchDirInfo);
            copyBranchLock.writeLock().unlock();
            CallBack callBack = new CallBack() {
                @Override
                public void run() {
                    branchDirInfo.setRunningTaskId(null);
                    branchDirInfo.setLastSyncTime(projectInfo.getLastSyncTime());
                    branchDirInfoMapper.insertBranchDirInfo(branchDirInfo);
                    // 切换到该分支
                    CredentialInfo credentialInfo = credentialInfoMapper.getCredentialByNodeId(nodeId);
                    UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(credentialInfo.getUsername(), credentialInfo.getPassword());
                    GitUtils.checkoutBranch(new File(branchCodePath), branchName, usernamePasswordCredentialsProvider);
                }
                @Override
                public Boolean isSuccess(){
                    return true;
                }

                @Override
                public void setResult(Boolean isSuccess) {}
            };
            TaskExecutionRequest<CopyTaskDTO> request = new TaskExecutionRequest<>();
            request.setTaskType(TaskTypeEnum.COPY_TASK);
            request.setTaskPO(
                    CopyTaskDTO.builder()
                            .srcPath(projectInfo.getPath() + URL_SPLIT + "master")
                            .dstPath(branchCodePath)
                            .taskInfoMapper(taskInfoMapper)
                            .taskInfo(taskInfo)
                            .callBack(callBack)
                            .build()
            );
            taskExecutionService.createAsyncTask(request);
            return taskInfo.getId();
        }finally {
            if (copyBranchLock.writeLock().isHeldByCurrentThread())
                copyBranchLock.writeLock().unlock();
        }
    }

    /*
    * 从diff/branchName/new 复制到 diff/branchName/${commitId}，并reset到该commit id
    * */

    private Integer copyCommitDirectory(Integer nodeId, String branchName, String commitId, ProjectInfo projectInfo){
        // 未加锁，此任务只由callAnalysis执行
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setType(TaskType.COPY.code);
        taskInfo.setNodeId(nodeId);
        JSONObject detail = new JSONObject();
        detail.put("branch", branchName);
        detail.put("commitId", commitId);
        detail.put("type", "commmit");
        taskInfo.setDetailInfo(detail.toJSONString());
        taskInfoMapper.initTask(taskInfo);

        String fullPath = BasicUtil.getBranchFullPath(projectInfo.getPath(), branchName, commitId);
        CallBack callBack = new CallBack() {
            @Override
            public void run() {
                // reset到指定commit id
                try{
                    GitUtils.setHEAD(new File(fullPath), commitId);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
            }
            @Override
            public Boolean isSuccess(){
                return true;
            }
            @Override
            public void setResult(Boolean isSuccess) {}
        };
        TaskExecutionRequest<CopyTaskDTO> request = new TaskExecutionRequest<>();
        request.setTaskType(TaskTypeEnum.COPY_TASK);
        request.setTaskPO(
                CopyTaskDTO.builder()
                        .srcPath(BasicUtil.getBranchFullPath(projectInfo.getPath(), branchName, null))
                        .dstPath(fullPath)
                        .taskInfoMapper(taskInfoMapper)
                        .taskInfo(taskInfo)
                        .callBack(callBack)
                        .build()
        );
        taskExecutionService.createAsyncTask(request);
        return taskInfo.getId();
    }

    private CallBack compileProject(Integer nodeId, String branchName, String commitId, ProjectInfo projectInfo){
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setType(TaskType.COMPILE.code);
        taskInfo.setNodeId(nodeId);
        taskInfoMapper.initTask(taskInfo);
        CallBack callBack = new CallBack() {
            public Boolean isSuccess = null;
            @Override
            public void run() {}
            @Override
            public Boolean isSuccess() {
                return isSuccess;
            }

            @Override
            public void setResult(Boolean isSuccess) {
                this.isSuccess = isSuccess;
            }
        };
        TaskExecutionRequest<CompileTaskDTO> request = new TaskExecutionRequest<>();
        request.setTaskType(TaskTypeEnum.COMPILE_TASK);
        request.setTaskPO(
                CompileTaskDTO.builder()
                        .branchName(branchName)
                        .commitId(commitId)
                        .taskInfo(taskInfo)
                        .projectInfo(projectInfo)
                        .taskInfoMapper(taskInfoMapper)
                        .mavenHome(mavenHome)
                        .mavenSettings(mavenSettings)
                        .javaHome(javaHome)
                        .callBack(callBack)
                        .build()
        );
        taskExecutionService.createAsyncTask(request);
        return callBack;
    }

    public Integer callAnalysis(Integer nodeId, CompareInfo compareInfo){
        callAnalysisLock.lock();
        // 先检查是否有同样的任务在跑，避免两个线程同时进来触发两个任务
        try{
            Integer check = taskInfoMapper.checkAnalysisTaskExists(nodeId, compareInfo);
            if (check != null){
                callAnalysisLock.unlock();
                return check;
            }

            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setNodeId(nodeId);
            taskInfo.setType(TaskType.DIFF.code);
            taskInfo.setDetailInfo(JSONObject.toJSONString(compareInfo));

            taskInfoMapper.initTask(taskInfo);
            callAnalysisLock.unlock();

            ProjectInfo projectInfo = projectInfoMapper.getProjectInfo(nodeId);

            PreRun pre = new PreRun() {
                private Boolean isSuccess;

                @Override
                public void run() {
                    ProjectInfo projectInfo = projectInfoMapper.getProjectInfo(nodeId);
                    Integer base = copyCommitDirectory(nodeId, compareInfo.getBase(), compareInfo.getBaseCommitId(), projectInfo);
                    Integer compare = copyCommitDirectory(nodeId, compareInfo.getCompare(), compareInfo.getCompareCommitId(), projectInfo);

                    for (int check = 0x00; check != 0x11; ) {
                        if (!taskInfoMapper.checkTaskRunning(base)) {
                            check |= 0x01;
                        }
                        if (!taskInfoMapper.checkTaskRunning(compare)) {
                            check |= 0x10;
                        }
                    }

                    if (
                            taskInfoMapper.getTaskInfo(base).getStatus().equals(TaskStatus.FAILD.code) ||
                                    taskInfoMapper.getTaskInfo(compare).getStatus().equals(TaskStatus.FAILD.code)
                    ) {
                        isSuccess = false;
                    } else {
                        // 执行maven compile
                        CallBack baseCallBack = compileProject(nodeId, compareInfo.getBase(), compareInfo.getBaseCommitId(), projectInfo);
                        CallBack compareCallBack = compileProject(nodeId, compareInfo.getCompare(), compareInfo.getCompareCommitId(), projectInfo);
                        while (baseCallBack.isSuccess() == null || compareCallBack.isSuccess() == null) {}
                        if (baseCallBack.isSuccess() && compareCallBack.isSuccess()) {
                            isSuccess = true;
                        } else {
                            isSuccess = false;
                        }
                    }
                }

                @Override
                public Boolean isSuccess() {
                    return isSuccess;
                }
            };
            CallBack callBack = new CallBack() {
                @Override
                public void run() {
                    File base = new File(BasicUtil.getBranchFullPath(projectInfo.getPath(), compareInfo.getBase(), compareInfo.getBaseCommitId()));
                    File compare = new File(BasicUtil.getBranchFullPath(projectInfo.getPath(), compareInfo.getCompare(), compareInfo.getCompareCommitId()));

                    if (base.exists()){
                        try{
                            FileUtils.deleteDirectory(base);
                        }catch (IOException e){
                            throw new RuntimeException(e);
                        }
                    }
                    if (compare.exists()){
                        try{
                            FileUtils.deleteDirectory(compare);
                        }catch (IOException e){
                            throw new RuntimeException(e);
                        }
                    }
                }
                @Override
                public Boolean isSuccess() {
                    return null;
                }
                @Override
                public void setResult(Boolean isSuccess) {}
            };


            TaskExecutionRequest<AnalysisTaskDTO> request = new TaskExecutionRequest<>();
            request.setTaskType(TaskTypeEnum.ANALYSIS_TASK);
            request.setTaskPO(
                    AnalysisTaskDTO.builder()
                            .nodeId(nodeId)
                            .rootPath(projectInfo.getPath())
                            .compareInfo(compareInfo)
                            .taskInfo(taskInfo)
                            .preRun(pre)
                            .callBack(callBack)
                            .build()
            );
            taskExecutionService.createAsyncTask(request);
            return taskInfo.getId();
        }finally {
            if (callAnalysisLock.isHeldByCurrentThread())
                callAnalysisLock.unlock();
        }
    }
}
