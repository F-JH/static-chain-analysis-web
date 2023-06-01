package com.hsf.admin.Controller;

import com.hsf.admin.Code.ResultTemplate;
import com.hsf.admin.Pojo.Entities.CredentialInfo;
import com.hsf.admin.Pojo.Requests.CredentialInfoRequest;
import com.hsf.admin.Service.CredentialInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/credential")
public class CredentialController {

    @Autowired
    CredentialInfoService credentialInfoService;

    @GetMapping("list")
    public ResultTemplate<List<CredentialInfo>> getCredentialList(String key){
        return new ResultTemplate<>(credentialInfoService.getCredentialInfoList(key));
    }

    @PostMapping("addCredentialInfo")
    public ResultTemplate<Integer> addCredential(@RequestBody CredentialInfoRequest credentialInfoRequest){
        CredentialInfo credentialInfo = new CredentialInfo();
        credentialInfo.setName(credentialInfoRequest.getName());
        credentialInfo.setUsername(credentialInfoRequest.getUsername());
        credentialInfo.setPassword(credentialInfoRequest.getPassword());
        credentialInfo.setPassphrase(credentialInfoRequest.getPassphrase());
        credentialInfo.setPublicKey(credentialInfoRequest.getPublicKey());
        credentialInfo.setPrivateKey(credentialInfoRequest.getPrivateKey());

        return new ResultTemplate<>(credentialInfoService.addCredentialInfo(credentialInfo));
//        return ResultTemplate.failed();
    }

    @PostMapping("editCredentialInfo")
    public ResultTemplate<Integer> editCredential(@RequestBody CredentialInfoRequest credentialInfoRequest){
        if (credentialInfoRequest.getId().equals(null)){
            return ResultTemplate.failed();
        }
        CredentialInfo credentialInfo = new CredentialInfo();
        credentialInfo.setId(credentialInfoRequest.getId());
        credentialInfo.setName(credentialInfoRequest.getName());
        credentialInfo.setUsername(credentialInfoRequest.getUsername());
        credentialInfo.setPassword(credentialInfoRequest.getPassword());
        credentialInfo.setPassphrase(credentialInfoRequest.getPassphrase());
        credentialInfo.setPublicKey(credentialInfoRequest.getPublicKey());
        credentialInfo.setPrivateKey(credentialInfoRequest.getPrivateKey());

        return new ResultTemplate<>(credentialInfoService.editCredentialInfo(credentialInfo));
    }

    @DeleteMapping("deleteCredentialInfo")
    public ResultTemplate<Integer> deleteCredential(@RequestParam Integer id){
        return new ResultTemplate<>(credentialInfoService.deleteCredentialInfo(id));
    }

    @PostMapping("credentialName")
    public ResultTemplate<String> getCredentialName(@RequestParam Integer credentialId){
        return new ResultTemplate<>(credentialInfoService.getCredentialName(credentialId).getName());
    }
}
