package com.analysis.corev2.Utils;

import com.analysis.corev2.Visitors.Parse.ParseMethodVisitor;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.analysis.tools.Config.Code.*;

public class ParseUtils {
    /**
     * 用于检索新项目中的java方法
     * @param classFilePath
     * @return
     * @throws FileNotFoundException
     */
    public static List<String> scanMethods(String classFilePath)throws FileNotFoundException{
        File file = new File(classFilePath);
        CompilationUnit compilationUnit = StaticJavaParser.parse(file);
        ParseMethodVisitor methodVisitor = new ParseMethodVisitor();
        String arg = classFilePath.substring(
            classFilePath.indexOf(SOURCE) + SOURCE.length() + 1,
            classFilePath.lastIndexOf(URL_SPLIT) + 1
        );
        if(!URL_SPLIT.equals(PACKAGE_SPLIT))
            arg = arg.replace(URL_SPLIT, PACKAGE_SPLIT);
        methodVisitor.visit(compilationUnit, arg);
        return new ArrayList<>(methodVisitor.getMds().keySet());
    }

    /**
     * 对比两个方法之间是否存在差异
     * @param oldFile
     * @param newFile
     * @return
     * @throws FileNotFoundException
     */
    public static Map<String, Boolean> compireToMethod(
        File oldFile, File newFile, String moduleName, String baseDir, String compareDir
    ) throws FileNotFoundException {
        CompilationUnit oldCompilationUnit = StaticJavaParser.parse(oldFile);
        CompilationUnit newCompilationUnit = StaticJavaParser.parse(newFile);

        ParseMethodVisitor oldMethodVisitor = new ParseMethodVisitor();
        ParseMethodVisitor newMethodVisitor = new ParseMethodVisitor();
        // 清除注释
        oldCompilationUnit.getAllContainedComments().forEach(Node::remove);
        newCompilationUnit.getAllContainedComments().forEach(Node::remove);

        int offset = moduleName.equals("") ? 2:3;
        String oldArg = oldFile.getAbsolutePath().substring(
            (baseDir + moduleName + SOURCE).length() + offset,
            oldFile.getAbsolutePath().lastIndexOf(URL_SPLIT) + 1
        );
        String newArg = newFile.getAbsolutePath().substring(
            (compareDir + moduleName + SOURCE).length() + offset,
            newFile.getAbsolutePath().lastIndexOf(URL_SPLIT) + 1
        );
        if(!URL_SPLIT.equals(PACKAGE_SPLIT)){
            // 兼容windows的路径，arg需要输入包名
            oldArg = oldArg.replace(URL_SPLIT, PACKAGE_SPLIT);
            newArg = newArg.replace(URL_SPLIT, PACKAGE_SPLIT);
        }

        oldMethodVisitor.visit(oldCompilationUnit, oldArg);
        newMethodVisitor.visit(newCompilationUnit, newArg);

        Map<String, Boolean> compireResult = new HashMap<>();
        for(String name:newMethodVisitor.getMds().keySet()){
            MethodDeclaration methodDeclaration = oldMethodVisitor.getMds().get(name);
            if(methodDeclaration != null)
                compireResult.put(name, methodDeclaration.equals(newMethodVisitor.getMds().get(name)));
            else
                compireResult.put(name, Boolean.FALSE);
        }

        return compireResult;
    }
}
