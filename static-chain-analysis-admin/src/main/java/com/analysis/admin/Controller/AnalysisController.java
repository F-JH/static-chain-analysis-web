package com.analysis.admin.Controller;

import com.analysis.admin.Code.Response;
import com.analysis.admin.Pojo.Requests.CompareInfo;
import com.analysis.admin.Service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Autowired
    private TaskService taskService;

    @PostMapping("callAnalysis")
    public Response<Integer> callAnalysis(@RequestParam Integer nodeId, @RequestBody CompareInfo compareInfo){
        return new Response<>(taskService.callAnalysis(nodeId, compareInfo));
    }
}
