package com.hsf.admin.Dto.Task;

import com.hsf.admin.Mapper.TaskInfoMapper;
import com.hsf.admin.Pojo.Entities.TaskInfo;
import com.hsf.admin.TaskCore.Interface.CallBack;
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
