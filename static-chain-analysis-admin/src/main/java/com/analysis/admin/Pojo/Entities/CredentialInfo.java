package com.analysis.admin.Pojo.Entities;

import lombok.Data;

@Data
public class CredentialInfo {
    // CredentialInfo表

    private Integer id;
    private String name;
    private String username;
    private String password;
    private String passphrase;
    private String publicKey;
    private String privateKey;
    private String isDelete;
}
