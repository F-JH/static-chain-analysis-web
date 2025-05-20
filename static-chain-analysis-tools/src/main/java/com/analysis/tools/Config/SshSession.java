package com.analysis.tools.Config;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;

public class SshSession extends JschConfigSessionFactory {
    private String name;
    private byte[] publicKey;
    private byte[] privateKey;
    private byte[] passphrase;

    public SshSession(String name, byte[] publicKey, byte[] privateKey, byte[] passphrase){
        super();
        this.name = name;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.passphrase = passphrase;
    }

    @Override
    protected void configure(OpenSshConfig.Host hc, Session session) {
//        UserInfo userInfo = new CredentialsProviderUserInfo(session, new CredentialsProvider() {
//            @Override
//            public boolean isInteractive() {
//                return false;
//            }
//
//            @Override
//            public boolean supports(CredentialItem... credentialItems) {
//                return true;
//            }
//
//            @Override
//            public boolean get(URIish urIish, CredentialItem... credentialItems) throws UnsupportedCredentialItem {
//                return false;
//            }
//        });
//        session.setUserInfo(userInfo);

        // ssh认证信息
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
    }

    @Override
    protected Session createSession(OpenSshConfig.Host hc, String user, String host, int port, FS fs) throws JSchException {
        Session session = super.getJSch(hc, fs).getSession(user, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
        return session;
    }

    @Override
    protected JSch createDefaultJSch(FS fs) throws JSchException {
        JSch jsch = super.createDefaultJSch(fs);
        jsch.removeAllIdentity();
//        jsch.addIdentity(privateKey, passphrase);
        jsch.addIdentity(name, privateKey, publicKey, passphrase);
        return jsch;
    }
}
