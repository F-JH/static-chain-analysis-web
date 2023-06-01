package com.hsf.admin.Mapper;

import com.hsf.admin.Pojo.Entities.AnalysisSimpleReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnalysisSimpleReportMapper {
    public List<AnalysisSimpleReport> getReportByTaskId(@Param("taskId") Integer taskId);

    public Integer insertReports(@Param("results") List<AnalysisSimpleReport> results);
}
