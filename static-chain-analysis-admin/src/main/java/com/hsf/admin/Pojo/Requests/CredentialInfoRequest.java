package com.hsf.admin.Pojo.Requests;

import lombok.Data;

@Data
public class CredentialInfoRequest {
    private Integer id;
    private String name;
    private String username;
    private String password;
    private String publicKey;
    private String privateKey;
    private String passphrase;
}
