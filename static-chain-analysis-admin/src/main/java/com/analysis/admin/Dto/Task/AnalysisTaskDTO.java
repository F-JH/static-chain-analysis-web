package com.analysis.admin.Dto.Task;

import com.analysis.admin.Pojo.Entities.TaskInfo;
import com.analysis.admin.Pojo.Requests.CompareInfo;
import com.analysis.admin.TaskCore.Interface.CallBack;
import com.analysis.admin.TaskCore.Interface.PreRun;
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
