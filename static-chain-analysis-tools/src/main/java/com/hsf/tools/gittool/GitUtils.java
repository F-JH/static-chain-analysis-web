package com.hsf.tools.gittool;

import com.hsf.tools.Config.SshSession;
import com.hsf.tools.Utils.BasicUtil;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GitUtils {

    public static void main(String[] args) {
        File test = new File("/Users/xiaoandi/github/static-chain-analysis-web/tmp/sl-ec-shoplytics/diff/test/new");
        UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("dw_fujuhong", "fGbgrW8Ea43hhM6yMjmC");
        checkoutBranch(test, "test", credentialsProvider);
    }
    @Deprecated
    public static Git clone(String gitUrl, String privateKey, byte[] passphrase){
        // 有你妈大问题，垃圾jgit
        CloneCommand cloneCommand = Git.cloneRepository()
            .setURI(gitUrl)
            .setDirectory(new File(System.getProperty("user.dir") + "/" + BasicUtil.getGitName(gitUrl)));

        SshSessionFactory sshSessionFactory = new SshSession("xiaoandi", null , privateKey.getBytes(), passphrase);
        cloneCommand.setTransportConfigCallback(transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        });

        Git git = null;
        try{
            git = cloneCommand.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return git;
    }

    public static void clone(String gitUrl, String dstDir, UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider) throws GitAPIException {
        CloneCommand cloneCommand = Git.cloneRepository()
            .setURI(gitUrl)
            .setRemote("origin")
            .setDirectory(new File(dstDir));
        cloneCommand.setCredentialsProvider(usernamePasswordCredentialsProvider);
//        cloneCommand.setCloneAllBranches(true); // clone 所有分支

        cloneCommand.call();
    }

    public static void pull(File dir, UsernamePasswordCredentialsProvider credentialsProvider) throws IOException, GitAPIException {
        Git git = Git.open(dir);
        git.pull()
            .setCredentialsProvider(credentialsProvider)
            .call();
    }

    public static void setHEAD(File dir, String HEAD) throws IOException, GitAPIException {
        Git git = Git.open(dir);
        git.reset()
            .setMode(ResetCommand.ResetType.HARD)
            .setRef(HEAD)
            .call();
        git.close();
    }

    public static List<String> getAllBranchs(File dir, UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider) throws IOException, GitAPIException {
        Git git = Git.open(dir);
        List<Ref> refs =  git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call(); // 只列出远程仓库里的分支
        List<String> result = new ArrayList<>();
        for (Ref ref : refs){
            String tmpName = ref.getName();
            if (tmpName.startsWith("refs/remotes/origin"))
                tmpName = tmpName.substring(20);
            result.add(tmpName);
        }
        git.close();
        return result;
    }

    /*
    * 返回按commit time倒序排列的commit列表
    * */
    public static List<String[]> getAllCommitId(File dir) throws IOException, GitAPIException {
        Git git = Git.open(dir);
        List<String[]> result = new ArrayList<>();
        List<RevCommit> revs = new ArrayList<>();
        Iterable<RevCommit> logs = git.log().call();
        for (RevCommit rev : logs){
            revs.add(rev);
        }
        Collections.sort(revs, new Comparator<RevCommit>() {
            @Override
            public int compare(RevCommit o1, RevCommit o2) {
                return o1.getCommitTime() < o2.getCommitTime() ? 1 : (o1.getCommitTime() == o2.getCommitTime() ? 0 : -1);
            }
        });

        int max = Math.min(100, revs.size());
        for (int i=0; i<max; i++){
            result.add(new String[]{revs.get(i).getName(), revs.get(i).getFullMessage()});
        }
        git.close();
        return result;
    }

    public static void checkoutBranch(File dir, String branchName, CredentialsProvider credentialsProvider) {
        Git git = null;
        try{
            git = Git.open(dir);
            git.checkout()
                .setCreateBranch(true)
                .setName(branchName)
                .setStartPoint("origin/" + branchName)
                .call();

            git.pull().setCredentialsProvider(credentialsProvider).call();
            git.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (git != null)
                git.close();
        }
    }

    public static Git getGit(File dir) throws IOException {
        return Git.open(dir);
    }
}
