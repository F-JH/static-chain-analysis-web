package com.analysis.admin.Dto.Task;

import com.analysis.admin.Mapper.FetchMapper;
import com.analysis.admin.Mapper.ProjectInfoMapper;
import com.analysis.admin.Mapper.TaskInfoMapper;
import com.analysis.admin.Pojo.Entities.TaskInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CloneTaskDTO {
    private String gitUrl;
    private String path;
    private String username;
    private String password;
    private String publicKey;
    private String privateKey;
    private String passphrase;
    private TaskInfo taskInfo;
    private Integer projectId; // nodeId
    private FetchMapper fetchMapper;
    private TaskInfoMapper taskInfoMapper;
    private ProjectInfoMapper projectInfoMapper;
    private String tmpDir;
}
