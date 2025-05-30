package com.analysis.admin.Controller;

import com.analysis.admin.Code.Response;
import com.analysis.admin.Service.FetchProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    FetchProjectTaskService fetchProjectTaskService;

    @PostMapping("pull")
    public Response<Integer> pullProject(@RequestParam Integer nodeId){
        return new Response<>(fetchProjectTaskService.cloneOrPull(nodeId));
    }
}
