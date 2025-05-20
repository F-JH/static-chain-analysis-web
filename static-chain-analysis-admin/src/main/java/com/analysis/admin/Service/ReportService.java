package com.analysis.admin.Service;

import com.analysis.admin.Mapper.AnalysisSimpleReportMapper;
import com.analysis.admin.Pojo.Entities.AnalysisSimpleReport;
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
