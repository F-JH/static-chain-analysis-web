package com.hsf.admin.Dto.Task;

import com.hsf.admin.Mapper.ProjectInfoMapper;
import com.hsf.admin.Mapper.TaskInfoMapper;
import com.hsf.admin.Pojo.Entities.ProjectInfo;
import com.hsf.admin.Pojo.Entities.TaskInfo;
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
