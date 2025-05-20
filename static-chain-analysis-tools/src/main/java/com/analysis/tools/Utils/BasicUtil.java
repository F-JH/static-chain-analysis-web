package com.analysis.tools.Utils;

import com.analysis.tools.Config.Code;
import org.eclipse.jetty.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.analysis.tools.Config.Code.*;

public class BasicUtil {

    /*
    * 截断git url的最后一节作为git name
    * https://github.com/F-JH/static-chain-analysis-web.git => static-chain-analysis-web
    * https://github.com/F-JH/static-chain-analysis-web => static-chain-analysis-web
    * */
    public static String getGitName(String gitUrl){
        return gitUrl.substring(
            gitUrl.lastIndexOf('/') + 1,
            gitUrl.lastIndexOf(".") > gitUrl.lastIndexOf("/") ? gitUrl.lastIndexOf(".") : gitUrl.length()
        );
    }

    public static String getMasterFullPath(String projectPath){
        return projectPath + URL_SPLIT + "master";
    }

    /*
    * "/github/xxx.git", "feat/release" => /github/xxx.git/diff/feat/release
    *
    * /github/xxx.git/diff/feat/release/new  存放该分支最新的代码
    * github/xxx.git/diff/feat/release/${commidId} 存放某个commit id对应的代码
    * */
    public static String getBranchFullPath(String basePath, String branchName, String commitId){
        basePath += Code.URL_SPLIT + "diff"; // 全部用于对比的代码放置在diff子目录
        String[] split = branchName.split("/");
        for (String item : split){
            if (StringUtil.isNotBlank(item))
                basePath += Code.URL_SPLIT + item;
        }
        if (commitId != null)
            return basePath + Code.URL_SPLIT + commitId;
        return basePath + Code.URL_SPLIT + "new";
    }


    /*
    * 为了适配javaParser的方法命名，把完整的方法名转化成javaParser格式的方法名
    * */
    public static String getMethodSignatureName(String name, String descriptor){
        StringBuilder signatureName = new StringBuilder();
        signatureName.append(name);
        signatureName.append("(");
        String patter = "(L.*?;|\\[{0,2}L.*?;|[ZCBSIFJDV]|\\[{0,2}[ZCBSIFJDV]{1})";
        Matcher parameterMatcher = Pattern.compile(patter).matcher(descriptor.substring(0, descriptor.lastIndexOf(')') + 1));
        while(parameterMatcher.find()){
            String param = parameterMatcher.group(1);
            if(param.length()==1){
                // V
                signatureName.append(descriptorMap.get(param)).append(", ");
            }else{
                String type = param.substring(0,1);
                if(descriptorMap.get(param.substring(1)) != null){
                    // [V
                    signatureName.append(descriptorMap.get(param.substring(1))).append(", ");
                }else{
                    // Ljava/lang/Object;     [Ljava/lang/Object;
                    String typeName = param.substring(param.lastIndexOf(PACKAGE_SPLIT) + 1, param.length()-1);
                    signatureName.append(typeName).append(descriptorMap.get(type)).append(", ");
                }
            }
        }
        if(!descriptor.startsWith("()"))
            signatureName.delete(signatureName.length()-2, signatureName.length());
        signatureName.append(")");
        String returnType = descriptor.substring(descriptor.lastIndexOf(')') + 1);
        if(returnType.length()==1){
            signatureName.append(descriptorMap.get(returnType));
        }else{
            String type = returnType.substring(0,1);
            if(descriptorMap.get(returnType.substring(1)) != null){
                signatureName.append(descriptorMap.get(returnType.substring(1))).append("[]");
            }else{
                String typeName = returnType.substring(returnType.lastIndexOf(PACKAGE_SPLIT) + 1, returnType.length()-1);
                signatureName.append(typeName).append(descriptorMap.get(type));
            }
        }

        return signatureName.toString();
    }
}
