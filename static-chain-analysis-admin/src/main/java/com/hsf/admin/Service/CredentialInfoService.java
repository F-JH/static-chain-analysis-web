package com.hsf.admin.Service;

import com.hsf.admin.Mapper.CredentialInfoMapper;
import com.hsf.admin.Pojo.Entities.CredentialInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CredentialInfoService {

    @Resource
    private CredentialInfoMapper credentialInfoMapper;

    public List<CredentialInfo> getCredentialInfoList(String key){
        if (StringUtils.isNotBlank(key))
            return credentialInfoMapper.searchCredentialInfoList(key);
        return credentialInfoMapper.getCredentialInfoList();
    }

    public CredentialInfo getCredentialName(Integer id){
        return credentialInfoMapper.getCredentialInfo(id);
    }

    public Integer addCredentialInfo(CredentialInfo credentialInfo){
        return credentialInfoMapper.addCredentialInfo(credentialInfo);
    }

    public Integer editCredentialInfo(CredentialInfo credentialInfo){
        return credentialInfoMapper.editCredentialInfo(credentialInfo);
    }

    public Integer deleteCredentialInfo(Integer id){
        return credentialInfoMapper.deleteCredentialInfo(id);
    }
}
