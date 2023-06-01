package com.hsf.admin.TaskCore;

import com.hsf.admin.Code.TaskStatus;
import com.hsf.admin.Mapper.TaskInfoMapper;
import com.hsf.admin.Pojo.Entities.TaskInfo;
import com.hsf.admin.TaskCore.Interface.CallBack;
import com.hsf.tools.Utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
public class CopyTask implements Runnable{
    private final String srcPath;
    private final String dstPath;
    private final TaskInfoMapper taskInfoMapper;
    private final TaskInfo taskInfo;
    private final CallBack callBack;

    public CopyTask(
        String src, String dst, TaskInfoMapper taskInfoMapper,
        TaskInfo taskInfo, CallBack callBack
    ){
        this.srcPath = src;
        this.dstPath = dst;
        this.taskInfoMapper = taskInfoMapper;
        this.taskInfo = taskInfo;
        this.callBack = callBack;
    }

    @Override
    public void run() {
        log.info("copy " + srcPath + " to " + dstPath);
        File srcDir = new File(srcPath);
        File dstDir = new File(dstPath);
        if (!srcDir.exists() ){
            throw new RuntimeException(srcPath + " do not exists!");
        }
        try{
            if (dstDir.exists()){
                // 不管，先删除掉原来的
                FileUtils.deleteDirectory(dstDir);
            }
            taskInfo.setStatus(TaskStatus.RUNNING.code);
            taskInfoMapper.updateTaskInfo(taskInfo);
            FileUtil.copyDirectory(srcDir, dstDir);
            // 复制完成，更新task_info
            taskInfo.setStatus(TaskStatus.SUCCESS.code);
            taskInfoMapper.updateTaskInfo(taskInfo);
            // 回调
            if (callBack != null){
                callBack.run();
            }
        }catch (IOException e){
            throw new RuntimeException("copy error!");
        }
    }
}
