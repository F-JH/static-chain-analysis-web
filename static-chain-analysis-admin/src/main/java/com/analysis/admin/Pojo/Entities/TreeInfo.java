package com.analysis.admin.Pojo.Entities;

import lombok.Data;

@Data
public class TreeInfo {
    private Integer nodeId;
    private Integer parentId;
    private String name;
    private Integer lft;
    private Integer rgt;
    private Boolean isDirectory;
    private String gitUrl;
    private Integer credentialId;
    private Integer status;
}
