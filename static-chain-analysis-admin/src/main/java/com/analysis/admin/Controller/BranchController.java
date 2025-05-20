package com.analysis.admin.Controller;

import com.analysis.admin.Code.ResultCode;
import com.analysis.admin.Code.Response;
import com.analysis.admin.Pojo.Requests.CompareInfo;
import com.analysis.admin.Pojo.Requests.GitInfoRequest;
import com.analysis.admin.Pojo.Entities.ProjectInfo;
import com.analysis.admin.Pojo.Responses.GitTreeResponse;
import com.analysis.admin.Service.GitBranchService;
import com.analysis.admin.Service.GitTreeInfoService;
import com.analysis.admin.Service.ProjectInfoService;
import com.analysis.admin.Service.TaskService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/branch")
public class BranchController {
    @Autowired
    private GitTreeInfoService gitTreeInfoService;

    @Autowired
    private ProjectInfoService projectInfoService;

    @Autowired
    private GitBranchService gitBranchService;

    @Autowired
    private TaskService taskService;

    @GetMapping("tree")
    public Response<GitTreeResponse> getGitTree(){
        return new Response<>(gitTreeInfoService.getTree());
    }

    @PostMapping("addNode")
    public Response<Integer> addNode(@RequestBody GitInfoRequest gitInfo){
        Integer res = gitTreeInfoService.addGitProject(gitInfo);
        if (res.equals(-1))
            return new Response<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), "请选择正确的路径！", res);
        return Response.success(res);
    }

    @PostMapping("deleteNode")
    public Response<Integer> deleteNode(@RequestParam Integer nodeId){
        return new Response<>(gitTreeInfoService.deleteNode(nodeId));
    }

    @PostMapping("gitInfo")
    public Response<ProjectInfo> gitInfoResponse(@RequestParam Integer nodeId){
        return new Response<>(projectInfoService.gitInfoResponse(nodeId));
    }

    @PostMapping("getBranchs")
    public Response<List<String>> getBranchs(@RequestParam Integer nodeId){
        try{
            return new Response<>(gitBranchService.getBranchs(nodeId));
        }catch (RuntimeException e){
            return new Response<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), e.getMessage(), null);
        }catch (IOException e){
            return new Response<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), "打开目录出错，请重新clone/pull项目", null);
        } catch (GitAPIException e) {
            return new Response<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), "连接到远程仓库出错！", null);
        }
    }

    @PostMapping("checkIfBranchDirExists")
    public Response<Integer[]> checkIfBranchDirExists(@RequestParam Integer nodeId, @RequestBody CompareInfo compareInfo){
        Integer[] result = new Integer[2];
        // 检查是否已有分支代码以及与master路径的代码更新日期是否一致
        // 1.检查base分支
        if (!projectInfoService.checkBranchDirSyncTime(nodeId, compareInfo.getBase())){
            result[0] = taskService.copyBranchDirectory(nodeId, compareInfo.getBase());
        }
        // 2.检查compare分支
        if (!projectInfoService.checkBranchDirSyncTime(nodeId, compareInfo.getCompare())){
            result[1] = taskService.copyBranchDirectory(nodeId, compareInfo.getCompare());
        }
        return new Response<>(result);
    }

    @PostMapping("getCommitIds")
    public Response<List<String[]>> getCommitIds(@RequestParam Integer nodeId, @RequestParam String branchName){
        if (!projectInfoService.checkBranchDirSyncTime(nodeId, branchName)){
            return new Response<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), "选中分支还未部署！", null);
        }

        try{
            return new Response<>(gitBranchService.getCommitIds(nodeId, branchName));
        } catch (GitAPIException e) {
            return new Response<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), "git链接出现错误", null);
        } catch (IOException e) {
            return new Response<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), "IO出现错误", null);
        }
    }
}
