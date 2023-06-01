package com.hsf.admin.Mapper;

import com.hsf.admin.Pojo.Entities.CredentialInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CredentialInfoMapper {
    public List<CredentialInfo> getCredentialInfoList();

    public List<CredentialInfo> searchCredentialInfoList(@Param("key") String key);

    public CredentialInfo getCredentialInfo(@Param("id") Integer id);

    public Integer addCredentialInfo(@Param("info") CredentialInfo credentialInfo);

    public Integer editCredentialInfo(@Param("info") CredentialInfo credentialInfo);

    public Integer deleteCredentialInfo(@Param("id") Integer id);

    public CredentialInfo getCredentialByNodeId(@Param("nodeId") Integer nodeId);
}
