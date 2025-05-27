package com.analysis.admin.TaskCore;

import com.analysis.admin.Code.Response;
import com.analysis.admin.Code.TaskStatus;
import com.analysis.admin.Dto.Task.CompileTaskDTO;
import com.analysis.admin.Enums.TaskTypeEnum;
import com.analysis.admin.Pojo.Requests.TaskExecutionRequest;
import com.analysis.admin.Pojo.Responses.TaskExecutionResponse;
import com.analysis.admin.TaskCore.Base.BaseTaskExecutor;
import com.analysis.tools.Utils.BasicUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.shared.invoker.*;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static com.analysis.tools.Config.Code.POM;
import static com.analysis.tools.Config.Code.URL_SPLIT;

@Slf4j
@Component
public class CompileTask extends BaseTaskExecutor {
    public CompileTask() {
        super(TaskTypeEnum.COMPILE_TASK);
    }

    @Override
    public Response<TaskExecutionResponse> execute(TaskExecutionRequest<?> request) {
        CompileTaskDTO compileTaskDTO = (CompileTaskDTO) request.getTaskPO();
        TaskExecutionResponse response = new TaskExecutionResponse();

        compileTaskDTO.getTaskInfo().setStatus(TaskStatus.RUNNING.code);
        compileTaskDTO.getTaskInfoMapper().updateTaskInfo(compileTaskDTO.getTaskInfo());
        String projectDir = BasicUtil.getBranchFullPath(compileTaskDTO.getProjectInfo().getPath(), compileTaskDTO.getBranchName(), compileTaskDTO.getCommitId());
        log.info("compile " + projectDir);
        InvocationRequest invocationRequest = new DefaultInvocationRequest();
        invocationRequest.setPomFile(new File(projectDir + URL_SPLIT + POM));
        invocationRequest.setGoals(Arrays.asList("clean", "compile", "-Dmaven.test.skip=true"));
        invocationRequest.setUserSettingsFile(new File(compileTaskDTO.getMavenSettings()));
        Invoker invoker = new DefaultInvoker();
        // 设置日志级别
        invoker.setLogger(new PrintStreamLogger(System.err, InvokerLogger.INFO));
        invoker.setOutputHandler(new InvocationOutputHandler() {
            @Override
            public void consumeLine(String s) throws IOException {
            }
        });

        if (!StringUtil.isBlank(compileTaskDTO.getMavenHome())){
            invoker.setMavenHome(new File(compileTaskDTO.getMavenHome()));
        }
        if (!StringUtil.isBlank(compileTaskDTO.getJavaHome())){
            invocationRequest.setJavaHome(new File(compileTaskDTO.getJavaHome()));
        }

        try{
            InvocationResult result = invoker.execute(invocationRequest);
            if (result.getExitCode() == 0){
                compileTaskDTO.getCallBack().setResult(true);
                log.info(projectDir + " 编译完成！");
            }else {
                compileTaskDTO.getCallBack().setResult(false);
                log.info(projectDir + " 编译失败！");
            }
            compileTaskDTO.getTaskInfo().setStatus(TaskStatus.SUCCESS.code);
            compileTaskDTO.getTaskInfoMapper().updateTaskInfo(compileTaskDTO.getTaskInfo());
        }catch (MavenInvocationException e){
            log.error("compile error: " + e.getMessage());
            compileTaskDTO.getTaskInfo().setStatus(TaskStatus.FAILD.code);
            compileTaskDTO.getTaskInfoMapper().updateTaskInfo(compileTaskDTO.getTaskInfo());
            compileTaskDTO.getCallBack().setResult(false);
        }
        return Response.success(response);
    }
}
