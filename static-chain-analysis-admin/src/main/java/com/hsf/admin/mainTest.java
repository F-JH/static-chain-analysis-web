package com.hsf.admin;

import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static com.hsf.tools.Config.Code.POM;
import static com.hsf.tools.Config.Code.URL_SPLIT;

public class mainTest {
    public static void main(String[] args) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(
            "/Users/xiaoandi/github/static-chain-analysis-web/tmp/sl-ec-shoplytics/diff/wbtest/4f2867646707fa25889c71442d76e99fbcf4fa93"
                + URL_SPLIT + POM
        ));
        request.setGoals(Arrays.asList("clean", "compile"));
        request.setUserSettingsFile(new File("/Users/xiaoandi/.m2/settings_shopline.xml"));
        request.setJavaHome(new File("/Library/Java/JavaVirtualMachines/jdk1.8.0_351.jdk/Contents/Home"));
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File("/usr/local/apache-maven-3.9.2"));
        try{
            InvocationResult result = invoker.execute(request);
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }
    }
}
