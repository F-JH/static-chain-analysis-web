package com.hsf.admin.TaskCore;

import com.hsf.admin.Code.TaskStatus;
import com.hsf.admin.Mapper.ProjectInfoMapper;
import com.hsf.admin.Mapper.TaskInfoMapper;
import com.hsf.admin.Pojo.Entities.ProjectInfo;
import com.hsf.admin.Pojo.Entities.TaskInfo;
import com.hsf.admin.TaskCore.Interface.CallBack;
import com.hsf.tools.Utils.BasicUtil;
import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.shared.invoker.*;
import org.eclipse.jetty.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static com.hsf.tools.Config.Code.POM;
import static com.hsf.tools.Config.Code.URL_SPLIT;

@Slf4j
public class CompileTask implements Runnable{
    private final TaskInfo taskInfo;
    private final TaskInfoMapper taskInfoMapper;
    private final String branchName;
    private final String commitId;
    private final ProjectInfo projectInfo;
    private final CallBack callBack;
    private final String mavenHome ;
    private final String javaHome;
    private final String mavenSettings;

    public CompileTask(
        String branchName, String commitId, TaskInfo taskInfo, ProjectInfo projectInfo,
        TaskInfoMapper taskInfoMapper, CallBack callBack, String mavenHome, String mavenSettings,
        String javaHome
    ){
        this.branchName = branchName;
        this.commitId = commitId;
        this.taskInfo = taskInfo;
        this.taskInfoMapper = taskInfoMapper;
        this.projectInfo = projectInfo;
        this.callBack = callBack;
        this.mavenHome = mavenHome;
        this.mavenSettings = mavenSettings;
        this.javaHome = javaHome;
    }
    @Override
    public void run() {
        taskInfo.setStatus(TaskStatus.RUNNING.code);
        taskInfoMapper.updateTaskInfo(taskInfo);
        String projectDir = BasicUtil.getBranchFullPath(projectInfo.getPath(), branchName, commitId);
        log.info("compile " + projectDir);
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(projectDir + URL_SPLIT + POM));
        request.setGoals(Arrays.asList("clean", "compile"));
        request.setUserSettingsFile(new File(mavenSettings));
        Invoker invoker = new DefaultInvoker();
        // 设置日志级别
        invoker.setLogger(new PrintStreamLogger(System.err, InvokerLogger.ERROR));
        invoker.setOutputHandler(new InvocationOutputHandler() {
            @Override
            public void consumeLine(String s) throws IOException {
            }
        });

        if (!StringUtil.isBlank(mavenHome)){
            invoker.setMavenHome(new File(mavenHome));
        }
        if (!StringUtil.isBlank(javaHome)){
            request.setJavaHome(new File(javaHome));
        }

        try{
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() == 0){
                callBack.setResult(true);
            }else {
                callBack.setResult(false);
            }
            log.info(projectDir + " 编译成功！");
            taskInfo.setStatus(TaskStatus.SUCCESS.code);
            taskInfoMapper.updateTaskInfo(taskInfo);
        }catch (MavenInvocationException e){
            taskInfo.setStatus(TaskStatus.FAILD.code);
            callBack.setResult(false);
        }
    }
}
