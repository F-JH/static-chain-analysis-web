package com.hsf.admin.Controller;

import com.hsf.admin.Code.Response;
import com.hsf.admin.Dto.Task.AnalysisTaskDTO;
import com.hsf.admin.Enums.TaskTypeEnum;
import com.hsf.admin.Mapper.ProjectInfoMapper;
import com.hsf.admin.Pojo.Entities.ProjectInfo;
import com.hsf.admin.Pojo.Requests.CompareInfo;
import com.hsf.admin.Pojo.Requests.TaskExecutionRequest;
import com.hsf.admin.Service.TaskExecutionService;
import com.hsf.admin.TaskCore.Base.BaseTaskExecutor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/test")
public class TestController {
    @Resource
    TaskExecutionService taskExecutionService;

    @Resource
    ProjectInfoMapper projectInfoMapper;

    @PostMapping("analysisTask")
    public Response<Integer> callAnalysisTask(@RequestParam(name="node_id") Integer nodeId, @RequestBody CompareInfo compareInfo) {
        ProjectInfo projectInfo = projectInfoMapper.getProjectInfo(nodeId);

        TaskExecutionRequest<AnalysisTaskDTO> request = new TaskExecutionRequest<>();
        request.setTaskType(TaskTypeEnum.ANALYSIS_TASK);
        request.setTaskPO(AnalysisTaskDTO.builder()
                .callBack(null)
                .preRun(null)
                .compareInfo(compareInfo)
                .nodeId(nodeId)
                .rootPath(projectInfo.getPath())
                .build());

        taskExecutionService.createAsyncTask(request);
        return Response.success(1);
    }
}
