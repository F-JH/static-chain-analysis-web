package com.hsf.admin.Controller;

import com.hsf.admin.Code.ResultCode;
import com.hsf.admin.Code.ResultTemplate;
import com.hsf.admin.Pojo.Requests.CompareInfo;
import com.hsf.admin.Pojo.Requests.GitInfoRequest;
import com.hsf.admin.Pojo.Entities.ProjectInfo;
import com.hsf.admin.Pojo.Responses.GitTreeResponse;
import com.hsf.admin.Service.GitBranchService;
import com.hsf.admin.Service.GitTreeInfoService;
import com.hsf.admin.Service.ProjectInfoService;
import com.hsf.admin.Service.TaskService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResultTemplate<GitTreeResponse> getGitTree(){
        return new ResultTemplate<>(gitTreeInfoService.getTree());
    }

    @PostMapping("addNode")
    public ResultTemplate<Integer> addNode(@RequestBody GitInfoRequest gitInfo){
        Integer res = gitTreeInfoService.addGitProject(gitInfo);
        if (res.equals(-1))
            return new ResultTemplate<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), "请选择正确的路径！", res);
        return ResultTemplate.success(res);
    }

    @PostMapping("gitInfo")
    public ResultTemplate<ProjectInfo> gitInfoResponse(@RequestParam Integer nodeId){
        return new ResultTemplate<>(projectInfoService.gitInfoResponse(nodeId));
    }

    @PostMapping("getBranchs")
    public ResultTemplate<List<String>> getBranchs(@RequestParam Integer nodeId){
        try{
            return new ResultTemplate<>(gitBranchService.getBranchs(nodeId));
        }catch (RuntimeException e){
            return new ResultTemplate<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), e.getMessage(), null);
        }catch (IOException e){
            return new ResultTemplate<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), "打开目录出错，请重新clone/pull项目", null);
        } catch (GitAPIException e) {
            return new ResultTemplate<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), "连接到远程仓库出错！", null);
        }
    }

    @PostMapping("checkIfBranchDirExists")
    public ResultTemplate<Integer[]> checkIfBranchDirExists(@RequestParam Integer nodeId, @RequestBody CompareInfo compareInfo){
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
        return new ResultTemplate<>(result);
    }

    @PostMapping("getCommitIds")
    public ResultTemplate<List<String[]>> getCommitIds(@RequestParam Integer nodeId, @RequestParam String branchName){
        if (!projectInfoService.checkBranchDirSyncTime(nodeId, branchName)){
            return new ResultTemplate<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), "选中分支还未部署！", null);
        }

        try{
            return new ResultTemplate<>(gitBranchService.getCommitIds(nodeId, branchName));
        } catch (GitAPIException e) {
            return new ResultTemplate<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), "git链接出现错误", null);
        } catch (IOException e) {
            return new ResultTemplate<>(ResultCode.SPECIAL_INSTRUCTION.getCode(), "IO出现错误", null);
        }
    }
}
