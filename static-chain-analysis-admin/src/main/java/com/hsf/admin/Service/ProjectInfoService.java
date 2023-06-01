package com.hsf.admin.Service;

import com.hsf.admin.Mapper.ProjectInfoMapper;
import com.hsf.admin.Pojo.Entities.ProjectInfo;
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
