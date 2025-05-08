package com.hsf.admin.Dto.Task;

import com.hsf.admin.Mapper.TaskInfoMapper;
import com.hsf.admin.Pojo.Entities.ProjectInfo;
import com.hsf.admin.Pojo.Entities.TaskInfo;
import com.hsf.admin.TaskCore.Interface.CallBack;
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
