package com.hsf.admin.Pojo.Entities;

import lombok.Data;

import java.util.Date;

@Data
public class BranchDirInfo {
    private Integer projectId;
    private String branchName;
    private String path;
    private Date createTime;
    private Date updateTime;
    private Date lastSyncTime;
    private Integer runningTaskId;

    public BranchDirInfo(){}

    public BranchDirInfo(Integer projectId, String branchName){
        this.projectId = projectId;
        this.branchName = branchName;
    }
}
