package com.hsf.admin.Pojo.Requests;

import lombok.Data;

@Data
public class GitInfoRequest {
    private TreeNodeInfoRequest tree;
    private String name;
    private String gitUrl;
    private Integer credentialsProviderId;
}
