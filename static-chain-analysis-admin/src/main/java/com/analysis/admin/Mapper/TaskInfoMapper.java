package com.analysis.admin.Mapper;

import com.analysis.admin.Pojo.Entities.TaskInfo;
import com.analysis.admin.Pojo.Requests.CompareInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskInfoMapper {
    public TaskInfo getTaskInfo(@Param("taskId") Integer taskId);
    public List<TaskInfo> getDiffTasks(@Param("nodeId") Integer nodeId);
    public Integer initTask(@Param("task") TaskInfo task);
    public Integer updateTaskInfo(@Param("task") TaskInfo task);
    public Boolean checkTaskRunning(@Param("taskId") Integer taskId);
    public Integer checkAnalysisTaskExists(@Param("nodeId") Integer nodeId, @Param("compareInfo") CompareInfo compareInfo);
}
