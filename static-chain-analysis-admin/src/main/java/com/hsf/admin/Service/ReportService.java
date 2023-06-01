package com.hsf.admin.Service;

import com.hsf.admin.Mapper.AnalysisSimpleReportMapper;
import com.hsf.admin.Pojo.Entities.AnalysisSimpleReport;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ReportService {

    @Resource
    private AnalysisSimpleReportMapper analysisSimpleReportMapper;

    public List<AnalysisSimpleReport> getReports(Integer taskId){
        return analysisSimpleReportMapper.getReportByTaskId(taskId);
    }
}
