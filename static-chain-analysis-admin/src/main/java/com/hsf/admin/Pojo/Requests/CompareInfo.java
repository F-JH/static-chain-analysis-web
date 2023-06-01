package com.hsf.admin.Pojo.Requests;

import lombok.Data;

@Data
public class CompareInfo {
    private String base;
    private String compare;
    private String baseCommitId;
    private String compareCommitId;
}
