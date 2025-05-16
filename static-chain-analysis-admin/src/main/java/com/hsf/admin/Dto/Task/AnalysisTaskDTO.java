package com.hsf.admin.Dto.Task;

import com.hsf.admin.Mapper.AnalysisSimpleReportMapper;
import com.hsf.admin.Mapper.TaskInfoMapper;
import com.hsf.admin.Pojo.Entities.TaskInfo;
import com.hsf.admin.Pojo.Requests.CompareInfo;
import com.hsf.admin.TaskCore.Interface.CallBack;
import com.hsf.admin.TaskCore.Interface.PreRun;
import com.hsf.core.Services.ScanService;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalysisTaskDTO {
    private Integer nodeId;
    private String rootPath;
    private CompareInfo compareInfo;
    private PreRun preRun;
    private CallBack callBack;
    private TaskInfo taskInfo;
}
