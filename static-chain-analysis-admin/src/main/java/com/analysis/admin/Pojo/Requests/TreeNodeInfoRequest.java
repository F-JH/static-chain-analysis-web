package com.analysis.admin.Pojo.Requests;

import lombok.Data;

@Data
public class TreeNodeInfoRequest {
    private Integer parentId;
    private String name;
    private Boolean isDirectory;
}
