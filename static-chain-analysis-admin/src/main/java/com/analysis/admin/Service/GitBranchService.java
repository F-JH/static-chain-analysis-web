package com.analysis.admin.Service;

import com.analysis.admin.Mapper.CredentialInfoMapper;
import com.analysis.admin.Mapper.ProjectInfoMapper;
import com.analysis.admin.Pojo.Entities.CredentialInfo;
import com.analysis.admin.Pojo.Entities.ProjectInfo;
import com.analysis.tools.Config.Code;
import com.analysis.tools.Utils.BasicUtil;
import com.analysis.tools.gittool.GitUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class GitBranchService {

    @Resource
    private ProjectInfoMapper projectInfoMapper;

    @Resource
    private CredentialInfoMapper credentialInfoMapper;

    public List<String> getBranchs(Integer nodeId) throws RuntimeException, IOException, GitAPIException {
        ProjectInfo projectInfo = projectInfoMapper.getProjectInfo(nodeId);
        File projectFile = new File(projectInfo.getPath() + Code.URL_SPLIT + "master"); // 默认去master拿代码，master是必须会有的
        if (!projectFile.exists() || projectFile.listFiles().length == 0)
            throw new RuntimeException("项目未下载，请先执行clone");
        CredentialInfo credentialInfo = credentialInfoMapper.getCredentialByNodeId(nodeId);
        UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider(
            credentialInfo.getUsername(), credentialInfo.getPassword()
        );
        return GitUtils.getAllBranchs(projectFile, user);
    }

    public List<String[]> getCommitIds(Integer nodeId, String branchName) throws RuntimeException, GitAPIException, IOException {
        // 此方法仅仅是从已有的分支路径中获取commitId，不会负责copy代码
        ProjectInfo projectInfo = projectInfoMapper.getProjectInfo(nodeId);
        File projectFile = new File(BasicUtil.getBranchFullPath(projectInfo.getPath(), branchName, null));
        if (!projectFile.exists() || projectFile.listFiles().length == 0)
            throw new RuntimeException("出错，路径不存在: " + projectFile.getAbsolutePath());
        return GitUtils.getAllCommitId(projectFile);
    }
}
