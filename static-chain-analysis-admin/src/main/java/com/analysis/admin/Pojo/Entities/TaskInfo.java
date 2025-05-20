package com.analysis.admin.Pojo.Entities;

import lombok.Data;

import java.util.Date;

@Data
public class TaskInfo {
    private Integer id;
    private String type;
    private Integer nodeId;
    private String detailInfo;
    private Date createTime;
    private Date updateTime;
    private Integer status;
}
