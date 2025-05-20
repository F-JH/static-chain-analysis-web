package com.analysis.core.Utils;

import com.alibaba.fastjson.JSONObject;
import com.analysis.core.Enums.JdkVersionEnum;
import com.analysis.core.Recorders.*;
import com.analysis.core.Visitors.RecordClassVisitor;
import com.analysis.core.Visitors.RelationClassVisitor;
import com.analysis.tools.Utils.FileUtil;
import com.analysis.tools.Utils.XmlDiffUtil;
import org.dom4j.DocumentException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static com.analysis.tools.Config.Code.*;

public class ChainUtils {

    /*
    * 利用visitor检索classBuffer，将interface、dubbo、abstract等类型记录下来
    * */
    public static void scanForClassName(JdkVersionEnum jdkVersionEnum,
                                        byte[] classfileBuffer, InterfaceRecord interfaceRecord, AbstractRecord abstractRecord, DubboRecord dubboRecord,
                                        ProjectRecord projectRecord
    ){
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new RecordClassVisitor(jdkVersionEnum, cw, interfaceRecord, abstractRecord, dubboRecord, projectRecord);
        cr.accept(cv, ClassReader.SKIP_FRAMES);
    }

    public static Map<String, List<String>> getRelationShipFromClassBuffer(JdkVersionEnum jdkVersionEnum,
        byte[] classfileBuffer, InterfaceRecord interfaceRecord, AbstractRecord abstractRecord, DubboRecord dubboRecord,
        ProjectRecord projectRecord, ControllerRecord controllerRecord, ApiRecord apiRecord
    ){
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        RelationClassVisitor cv = new RelationClassVisitor(jdkVersionEnum,
            cw, interfaceRecord, abstractRecord, projectRecord, controllerRecord, apiRecord
        );
        cr.accept(cv, ClassReader.SKIP_FRAMES);

        return cv.getMethodRelations();
    }

    /**
     * {
     *     "com/bot/server/qqBot/envGet:getByEnv|(Ljava/lang/String;)Ljava/lang/String;":{
     *         "com/bot/server/qqBot/mapper/postMethod:run|(Lcom/alibaba/fastjson/JSONObject;Lcom/alibaba/fastjson/JSONObject;Ljava/lang/Integer;)Ljava/lang/String":{
     *             ...
     *         }
     *     },
     *     "com/bot/server/qqBot/envGet:test|(Ljava/lang/String;)Ljava/lang/String;":{
     *         ...
     *     }
     * }
     * @param relationShips
     * @param startFullMethodName
     * @return
     */
    public static JSONObject getJSONChainFromRelationShip(
        Map<String, Map<String, List<String>>> relationShips, String startFullMethodName, InterfaceRecord interfaceRecord,
        AbstractRecord abstractRecord
    ){
        String className = startFullMethodName.substring(0, startFullMethodName.indexOf(METHOD_SPLIT));
        String methodName = startFullMethodName.substring(startFullMethodName.indexOf(METHOD_SPLIT)+1);
        Stack<Object[]> stack = new Stack<>();
        JSONObject relation = new JSONObject();
        List<String> startChain = new ArrayList<>();
        startChain.add(startFullMethodName);
        Object[] initNode = new Object[]{className, methodName, startChain, relation};
        stack.push(initNode);
        while(!stack.empty()){
            Object[] currentNode = stack.pop();
            String currentClassName = (String) currentNode[0];
            String currentMethodName = (String) currentNode[1];
            List<String> currentChain = (List<String>) currentNode[2];
            List<String> methodRelationShip = relationShips.get(currentClassName).getOrDefault(currentMethodName, new ArrayList<>());
            JSONObject currentRelation = (JSONObject) currentNode[3];
            // 处理接口或抽象类
            if(interfaceRecord.containInterface(currentClassName) || abstractRecord.containAbstract(currentClassName)){
                List<String> entries = interfaceRecord.getEntries(currentClassName);
                Map<String, Boolean> abstractMethod = interfaceRecord.getMethod(currentClassName);
                if(entries==null){
                    entries = abstractRecord.getEntries(currentClassName);
                    abstractMethod = abstractRecord.getMethod(currentClassName);
                }
                // abstract implements interface 的情况，需要考虑下
                for(String entryClassName:entries){
                    // 存在default方法，需要判断是否被实体类复写
                    if(abstractMethod.get(currentMethodName) != null && !abstractMethod.get(currentMethodName)){
                        // default或者抽象类中有实体的方法
                        if(relationShips.get(entryClassName).containsKey(currentMethodName)){
                            // 实体类有复写这个方法，但是静态分析下无法判断实际跑的是哪一个方法，所以都要加进去
                            methodRelationShip.add(entryClassName + METHOD_SPLIT + currentMethodName);
                        }
                    }else{
                        methodRelationShip.add(entryClassName + METHOD_SPLIT + currentMethodName);
                    }
                }
            }
            if(methodRelationShip != null && !methodRelationShip.isEmpty()){
                for(String fullMethodName : methodRelationShip){
                    // 解开methodRelationShip，加到stack中，并与往json里添加
                    JSONObject tmpRelation = new JSONObject();
                    currentRelation.put(fullMethodName, tmpRelation);
                    if(!currentChain.contains(fullMethodName)){
                        String tmpClassName = fullMethodName.substring(0, fullMethodName.indexOf(METHOD_SPLIT));
                        String tmpMethodName = fullMethodName.substring(fullMethodName.indexOf(METHOD_SPLIT)+1);
                        List<String> tmpChain = new ArrayList<String>(currentChain);
                        tmpChain.add(fullMethodName);
                        stack.add(new Object[]{tmpClassName, tmpMethodName, tmpChain, tmpRelation});
                    }
                }
            }
        }
        return relation;
    }

    public static List<String> getProjectUpdateMethod(
        List<String> modules, String baseDir, String compareDir
    ) throws NotDirectoryException, FileNotFoundException, DocumentException {
        List<String> result = new ArrayList<>();
        for(String module:modules){
            String baseModule = baseDir + module.substring(compareDir.length());
            Map<String, List<String>> classDiff = FileUtil.compireToPath(
                baseModule + URL_SPLIT + SOURCE,
                module + URL_SPLIT + SOURCE
            );
            List<String> newDirectorys = classDiff.get("newDirectorys");
            List<String> newFiles = classDiff.get("newFiles");
            List<String> modifyFiles = classDiff.get("modifyFiles");
            List<String> scanFiles = new ArrayList<>();

            for(String directory:newDirectorys){
                List<String> files = FileUtil.scanForDirectory(module + URL_SPLIT + SOURCE + directory);
                if(files != null)
                    scanFiles.addAll(files);
            }
            // 对比 modifyFiles 获取有修改的method
            for(String file:modifyFiles){
                String moduleName = module.equals(compareDir) ? "" : module.substring(compareDir.length()+1);
                Map<String, Boolean> modifyMethods = ParseUtils.compireToMethod(
                    new File(baseModule + URL_SPLIT + SOURCE + file),
                    new File(module + URL_SPLIT + SOURCE + file),
                    moduleName, baseDir, compareDir
                );
                for(String method:modifyMethods.keySet()){
                    if(!modifyMethods.get(method)){
                        result.add(method);
                    }
                }
            }
            // 新目录下的所有method
            for(String file:scanFiles){
                List<String> scanMethods = ParseUtils.scanMethods(file);
                result.addAll(scanMethods);
            }
            // 新java文件的所有method
            for(String file:newFiles){
                List<String> newMethods = ParseUtils.scanMethods(
                    module + URL_SPLIT + SOURCE + file
                );
                result.addAll(newMethods);
            }
            // 检索所有mybatis xml配置
            String oldResource = baseModule + URL_SPLIT + RESOURCES;
            String newResource = module + URL_SPLIT + RESOURCES;

            if(new File(oldResource).exists() && new File(newResource).exists()){
                Map<String, String> oldMybatisXml = FileUtil.scanMybatisXml(oldResource);
                Map<String, String> newMybatisXml = FileUtil.scanMybatisXml(newResource);

                for(String xml:newMybatisXml.keySet()){
                    if(oldMybatisXml.containsKey(xml)){
                        List<String> xmlDiff =  XmlDiffUtil.compireXml(oldMybatisXml.get(xml), newMybatisXml.get(xml));
                        for(String methodName:xmlDiff){
                            List<String> methods = ParseUtils.scanMethods(
                                module + URL_SPLIT + SOURCE + URL_SPLIT + methodName.substring(0, methodName.lastIndexOf(METHOD_SPLIT)) + ".java"
                            );
                            for(String fullMethodName:methods){
                                if(fullMethodName.startsWith(methodName)){
                                    result.add(fullMethodName);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
