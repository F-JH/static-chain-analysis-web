package com.analysis.admin.Controller;

import com.analysis.admin.Code.Response;
import com.analysis.admin.Pojo.Entities.CredentialInfo;
import com.analysis.admin.Pojo.Requests.CredentialInfoRequest;
import com.analysis.admin.Service.CredentialInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/credential")
public class CredentialController {

    @Autowired
    CredentialInfoService credentialInfoService;

    @GetMapping("list")
    public Response<List<CredentialInfo>> getCredentialList(String key){
        return new Response<>(credentialInfoService.getCredentialInfoList(key));
    }

    @PostMapping("addCredentialInfo")
    public Response<Integer> addCredential(@RequestBody CredentialInfoRequest credentialInfoRequest){
        CredentialInfo credentialInfo = new CredentialInfo();
        credentialInfo.setName(credentialInfoRequest.getName());
        credentialInfo.setUsername(credentialInfoRequest.getUsername());
        credentialInfo.setPassword(credentialInfoRequest.getPassword());
        credentialInfo.setPassphrase(credentialInfoRequest.getPassphrase());
        credentialInfo.setPublicKey(credentialInfoRequest.getPublicKey());
        credentialInfo.setPrivateKey(credentialInfoRequest.getPrivateKey());

        return new Response<>(credentialInfoService.addCredentialInfo(credentialInfo));
//        return ResultTemplate.failed();
    }

    @PostMapping("editCredentialInfo")
    public Response<Integer> editCredential(@RequestBody CredentialInfoRequest credentialInfoRequest){
        if (credentialInfoRequest.getId().equals(null)){
            return Response.failed();
        }
        CredentialInfo credentialInfo = new CredentialInfo();
        credentialInfo.setId(credentialInfoRequest.getId());
        credentialInfo.setName(credentialInfoRequest.getName());
        credentialInfo.setUsername(credentialInfoRequest.getUsername());
        credentialInfo.setPassword(credentialInfoRequest.getPassword());
        credentialInfo.setPassphrase(credentialInfoRequest.getPassphrase());
        credentialInfo.setPublicKey(credentialInfoRequest.getPublicKey());
        credentialInfo.setPrivateKey(credentialInfoRequest.getPrivateKey());

        return new Response<>(credentialInfoService.editCredentialInfo(credentialInfo));
    }

    @DeleteMapping("deleteCredentialInfo")
    public Response<Integer> deleteCredential(@RequestParam Integer id){
        return new Response<>(credentialInfoService.deleteCredentialInfo(id));
    }

    @PostMapping("credentialName")
    public Response<String> getCredentialName(@RequestParam Integer credentialId){
        return new Response<>(credentialInfoService.getCredentialName(credentialId).getName());
    }
}
