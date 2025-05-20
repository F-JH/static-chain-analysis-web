package com.analysis.admin.Dto.Task;

import com.analysis.admin.Mapper.TaskInfoMapper;
import com.analysis.admin.Pojo.Entities.TaskInfo;
import com.analysis.admin.TaskCore.Interface.CallBack;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CopyTaskDTO {
    private String srcPath;
    private String dstPath;
    private TaskInfoMapper taskInfoMapper;
    private TaskInfo taskInfo;
    private CallBack callBack;
}
