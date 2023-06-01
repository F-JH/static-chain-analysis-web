package com.hsf.admin.Mapper;

import com.hsf.admin.Pojo.Entities.ProjectInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectInfoMapper {
    public ProjectInfo getProjectInfo(@Param("nodeId") Integer nodeId);

    public Integer insertProjectInfo(@Param("info") ProjectInfo info);

    public Integer updateProjectInfo(@Param("info") ProjectInfo info);

    public Boolean checkBranchDirSyncTime(@Param("nodeId") Integer nodeId, @Param("branchName") String branchName);
}
