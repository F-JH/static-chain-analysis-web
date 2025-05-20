package com.analysis.admin.Controller;

import com.analysis.admin.Code.Response;
import com.analysis.admin.Dto.Task.AnalysisTaskDTO;
import com.analysis.admin.Enums.TaskTypeEnum;
import com.analysis.admin.Mapper.ProjectInfoMapper;
import com.analysis.admin.Pojo.Entities.ProjectInfo;
import com.analysis.admin.Pojo.Requests.CompareInfo;
import com.analysis.admin.Pojo.Requests.TaskExecutionRequest;
import com.analysis.admin.Service.TaskExecutionService;
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
