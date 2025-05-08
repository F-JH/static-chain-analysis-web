package com.hsf.admin.Service;

import com.hsf.admin.Mapper.ProjectInfoMapper;
import com.hsf.admin.Mapper.TreeInfoMapper;
import com.hsf.admin.Pojo.Entities.ProjectInfo;
import com.hsf.admin.Pojo.Entities.TreeInfo;
import com.hsf.admin.Pojo.Requests.GitInfoRequest;
import com.hsf.admin.Pojo.Responses.GitTreeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class GitTreeInfoService {
    @Resource
    private TreeInfoMapper treeInfoMapper;

    @Resource
    private ProjectInfoMapper projectInfoMapper;

    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public GitTreeResponse getTree(){
        readWriteLock.readLock().lock();
        List<TreeInfo> trees = treeInfoMapper.getTreeInfo();
        readWriteLock.readLock().unlock();
        Stack<GitTreeResponse> stack = new Stack<>();

        for (TreeInfo treeItem : trees){

            GitTreeResponse node = new GitTreeResponse();
            node.setId(treeItem.getNodeId());
            node.setName(treeItem.getName());
            node.setIsDirectory(treeItem.getIsDirectory());
            node.setStatus(treeItem.getStatus());
            node.setChildren(new ArrayList<>());
            node.setGitUrl(treeItem.getGitUrl());
            node.setCredentialId(treeItem.getCredentialId());

            if (!stack.empty()){
                // 判断是否是此节点的父节点再添加
                while (!treeItem.getParentId().equals(stack.peek().getId()))
                    stack.pop();
                stack.peek().getChildren().add(node);
            }
            if (treeItem.getIsDirectory()){
                stack.push(node);
            }
        }

        GitTreeResponse result = null;
        while (!stack.empty())
            result = stack.pop();
        return result;
    }

    @Transactional
    public Integer addGitProject(GitInfoRequest gitInfo){
        if (!treeInfoMapper.isDirectory(gitInfo.getTree().getParentId()))
            return -1;
        readWriteLock.writeLock().lock();
        TreeInfo res = new TreeInfo();
        treeInfoMapper.addNodeV2(gitInfo, res);
        if (gitInfo.getTree().getIsDirectory()){
            readWriteLock.writeLock().unlock();
            return res.getNodeId();
        }
        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setNodeId(res.getNodeId());
        Integer result = projectInfoMapper.insertProjectInfo(projectInfo);
        readWriteLock.writeLock().unlock();

        return result;
    }

    @Transactional
    public Integer deleteNode(Integer nodeId){
        readWriteLock.writeLock().lock();
        Integer result = treeInfoMapper.deleteNode(nodeId);
        if (result == 0){
            readWriteLock.writeLock().unlock();
            return -1;
        }
        projectInfoMapper.deleteProjectInfo(nodeId);
        readWriteLock.writeLock().unlock();
        return result;
    }
}
