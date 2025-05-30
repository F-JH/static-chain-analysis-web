package com.analysis.admin.Pojo.Entities;

import lombok.Data;

import java.util.Date;

@Data
public class FetchInfo {
    private String gitUrl;
    private Integer status;
    private String path;
    private Date lastSyncTime;
    private String credentialName;
    private String credentialUsername;
    private String credentialPassword;
    private String publicKey;
    private String privateKey;
    private String passphrase;
}
