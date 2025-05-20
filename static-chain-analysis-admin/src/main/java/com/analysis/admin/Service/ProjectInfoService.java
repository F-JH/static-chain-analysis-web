package com.analysis.admin.Service;

import com.analysis.admin.Mapper.ProjectInfoMapper;
import com.analysis.admin.Pojo.Entities.ProjectInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ProjectInfoService {
    @Resource
    private ProjectInfoMapper projectInfoMapper;

    public ProjectInfo gitInfoResponse(Integer nodeId){
        return projectInfoMapper.getProjectInfo(nodeId);
    }

    public Boolean checkBranchDirSyncTime(Integer nodeId, String branchName){
        return projectInfoMapper.checkBranchDirSyncTime(nodeId, branchName);
    }
}
