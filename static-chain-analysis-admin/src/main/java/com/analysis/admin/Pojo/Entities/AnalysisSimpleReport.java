package com.analysis.admin.Pojo.Entities;

import lombok.Data;

import java.util.Date;

@Data
public class AnalysisSimpleReport {
    private Integer taskId;
    private String type;
    private String apiName;
    private Date createTime;
    private Date updateTime;
}
