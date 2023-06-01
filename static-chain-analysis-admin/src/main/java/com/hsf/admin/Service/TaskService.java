package com.hsf.admin.Service;

import com.alibaba.fastjson.JSONObject;
import com.hsf.admin.Code.TaskStatus;
import com.hsf.admin.Code.TaskType;
import com.hsf.admin.Mapper.*;
import com.hsf.admin.Pojo.Entities.BranchDirInfo;
import com.hsf.admin.Pojo.Entities.CredentialInfo;
import com.hsf.admin.Pojo.Entities.ProjectInfo;
import com.hsf.admin.Pojo.Entities.TaskInfo;
import com.hsf.admin.Pojo.Requests.CompareInfo;
import com.hsf.admin.TaskCore.*;
import com.hsf.admin.TaskCore.Interface.CallBack;
import com.hsf.admin.TaskCore.Interface.PreRun;
import com.hsf.core.Services.ScanService;
import com.hsf.tools.Utils.BasicUtil;
import com.hsf.tools.gittool.GitUtils;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.hsf.tools.Config.Code.URL_SPLIT;

@Service
public class TaskService {

    @Value("${env.mavenHome}")
    private String mavenHome;
    @Value("${env.javaHome}")
    private String javaHome;
    @Resource
    FetchMapper fetchMapper;
    @Resource
    BranchDirInfoMapper branchDirInfoMapper;
    @Resource
    ProjectInfoMapper projectInfoMapper;
    @Resource
    TaskInfoMapper taskInfoMapper;
    @Resource
    CredentialInfoMapper credentialInfoMapper;
    @Resource
    AnalysisSimpleReportMapper analysisSimpleReportMapper;
    @Autowired
    ScanService scanService;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

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

            threadPoolExecutor.execute(new CopyTask(
                projectInfo.getPath() + URL_SPLIT + "master", branchCodePath,
                taskInfoMapper, taskInfo,
                new CallBack() {
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
                }
            ));
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

//        ProjectInfo projectInfo = projectInfoMapper.getProjectInfo(nodeId);
        String fullPath = BasicUtil.getBranchFullPath(projectInfo.getPath(), branchName, commitId);

        threadPoolExecutor.execute(new CopyTask(
            BasicUtil.getBranchFullPath(projectInfo.getPath(), branchName, null), fullPath,
            taskInfoMapper, taskInfo,
            new CallBack() {
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
            }
        ));
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

        threadPoolExecutor.execute(new CompileTask(
            nodeId, branchName, commitId, taskInfo, projectInfo, projectInfoMapper, taskInfoMapper, callBack,
            mavenHome, javaHome
        ));
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

            threadPoolExecutor.execute(new AnalysisTask(
                nodeId, projectInfo.getPath(), compareInfo, taskInfo, scanService, taskInfoMapper,
                analysisSimpleReportMapper,
                new PreRun() {
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
                            while (baseCallBack.isSuccess() == null || compareCallBack.isSuccess() == null) {
                            }
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
                },
                new CallBack() {
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
                }
            ));

            return taskInfo.getId();
        }finally {
            if (callAnalysisLock.isHeldByCurrentThread())
                callAnalysisLock.unlock();
        }
    }
}
