package com.analysis.admin.Dto.Task;

import com.analysis.admin.Mapper.TaskInfoMapper;
import com.analysis.admin.Pojo.Entities.ProjectInfo;
import com.analysis.admin.Pojo.Entities.TaskInfo;
import com.analysis.admin.TaskCore.Interface.CallBack;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompileTaskDTO {
    private TaskInfo taskInfo;
    private TaskInfoMapper taskInfoMapper;
    private String branchName;
    private String commitId;
    private ProjectInfo projectInfo;
    private CallBack callBack;
    private String mavenHome ;
    private String javaHome;
    private String mavenSettings;
}
