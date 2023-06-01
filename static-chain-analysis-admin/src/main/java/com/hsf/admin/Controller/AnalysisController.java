package com.hsf.admin.Controller;

import com.hsf.admin.Code.ResultTemplate;
import com.hsf.admin.Mapper.TreeInfoMapper;
import com.hsf.admin.Pojo.Entities.TreeInfo;
import com.hsf.admin.Pojo.Requests.CompareInfo;
import com.hsf.admin.Service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Autowired
    private TaskService taskService;

    @PostMapping("callAnalysis")
    public ResultTemplate<Integer> callAnalysis(@RequestParam Integer nodeId, @RequestBody CompareInfo compareInfo){
        return new ResultTemplate<>(taskService.callAnalysis(nodeId, compareInfo));
    }
}
