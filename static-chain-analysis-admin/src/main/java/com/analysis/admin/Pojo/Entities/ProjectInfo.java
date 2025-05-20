package com.analysis.admin.Pojo.Entities;

import lombok.Data;

import java.util.Date;

@Data
public class ProjectInfo {
    private Integer id;
    private Integer nodeId;
    private String path;
    private Date createTime;
    private Date updateTime;
    private Date lastSyncTime;
}
