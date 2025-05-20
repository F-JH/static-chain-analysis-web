package com.analysis.admin.Mapper;

import com.analysis.admin.Pojo.Entities.TreeInfo;
import com.analysis.admin.Pojo.Requests.GitInfoRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TreeInfoMapper {
    public List<TreeInfo> getTreeInfo();

    public Boolean isDirectory(@Param("nodeId") Integer nodeId);

    public Integer addNode(@Param("node")GitInfoRequest gitInfoRequest, @Param("result")TreeInfo treeInfo);
    public Integer addNodeV2(@Param("node")GitInfoRequest gitInfoRequest, @Param("result")TreeInfo treeInfo);
    public Integer deleteNode(@Param("nodeId") Integer nodeId);
}
