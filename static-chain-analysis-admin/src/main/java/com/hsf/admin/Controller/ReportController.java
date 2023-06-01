package com.hsf.admin.Controller;

import com.hsf.admin.Code.ResultTemplate;
import com.hsf.admin.Pojo.Entities.AnalysisSimpleReport;
import com.hsf.admin.Pojo.Entities.TaskInfo;
import com.hsf.admin.Service.ReportService;
import com.hsf.admin.Service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ReportService reportService;

    @RequestMapping("task")
    public ResultTemplate<TaskInfo> getTaskStatus(@RequestParam Integer taskId){
        return new ResultTemplate<>(taskService.getTaskInfo(taskId));
    }

    @PostMapping("taskResultDetail")
    public ResultTemplate<List<AnalysisSimpleReport>> getReports(@RequestParam Integer taskId){
        return new ResultTemplate<>(reportService.getReports(taskId));
    }

    @PostMapping("list")
    public ResultTemplate<List<TaskInfo>> getDiffTaskInfos(@RequestParam Integer nodeId){
        return new ResultTemplate<>(taskService.getDiffTasks(nodeId));
    }
}
