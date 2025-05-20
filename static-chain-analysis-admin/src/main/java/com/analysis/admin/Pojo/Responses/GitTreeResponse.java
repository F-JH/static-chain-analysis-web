package com.analysis.admin.Pojo.Responses;

import lombok.Data;

import java.util.List;

@Data
public class GitTreeResponse {
    private Integer id;
    private String name;
    private String gitUrl;
    private Boolean isDirectory;
    private Integer status;
    private Integer credentialId;
    private List<GitTreeResponse> children;
}
