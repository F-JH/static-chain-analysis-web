package com.analysis.admin.Mapper;

import com.analysis.admin.Pojo.Entities.FetchInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FetchMapper {
    public FetchInfo getFetchInfo(@Param("nodeId") Integer nodeId);

    public Integer updateProjectInfo();

    public Integer updateFileNodeStatus(@Param("nodeId") Integer nodeId, @Param("status") Integer status);
}
