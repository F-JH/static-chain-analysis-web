package com.analysis.admin.Dto.Task;

import com.analysis.admin.Mapper.ProjectInfoMapper;
import com.analysis.admin.Mapper.TaskInfoMapper;
import com.analysis.admin.Pojo.Entities.ProjectInfo;
import com.analysis.admin.Pojo.Entities.TaskInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PullTaskDTO {
    private String username;
    private String password;
    private String publicKey;
    private String privateKey;
    private String passphrase;
    private String projectDir;
    private TaskInfo taskInfo;
    private TaskInfoMapper taskInfoMapper;
    private ProjectInfo projectInfo;
    private ProjectInfoMapper projectInfoMapper;
}
