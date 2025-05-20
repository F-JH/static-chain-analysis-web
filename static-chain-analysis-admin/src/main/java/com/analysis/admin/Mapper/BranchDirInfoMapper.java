package com.analysis.admin.Mapper;

import com.analysis.admin.Pojo.Entities.BranchDirInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BranchDirInfoMapper {
    public BranchDirInfo getBranchDirInfo(@Param("projectId") Integer projectId, @Param("branchName") String branchName);

    public Integer insertBranchDirInfo(@Param("branchDirInfo") BranchDirInfo branchDirInfo);
}
