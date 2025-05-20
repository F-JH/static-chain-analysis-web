package com.analysis.corev2.Utils;

import com.alibaba.fastjson.JSONObject;
import com.analysis.corev2.Entitys.Chain.ChainNode;
import com.analysis.corev2.Entitys.DTO.RecordDTO;
import com.analysis.corev2.Enums.JdkVersionEnum;
import com.analysis.corev2.Recorders.Relation.RelationRecord;
import com.analysis.corev2.Recorders.Relation.RelationReverseRecord;
import com.analysis.corev2.Visitors.Record.RecordClassVisitor;
import com.analysis.corev2.Visitors.Relation.RelationClassVisitor;
import com.analysis.tools.Pojo.Response.Pair;
import com.analysis.tools.Utils.FileUtil;
import com.analysis.tools.Utils.ThreadUtil;
import com.analysis.tools.Utils.XmlDiffUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.*;
import java.util.concurrent.*;

import static com.analysis.tools.Config.Code.*;

@Slf4j
public class ChainUtils {
    private static final ThreadPoolExecutor taskThreadPool = new ThreadPoolExecutor(50, 100, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    /*
    * 利用visitor检索classBuffer，将interface、dubbo、abstract等类型记录下来
    * */
    public static void scanForClassName(JdkVersionEnum jdkVersionEnum, byte[] classfileBuffer, RecordDTO recordDTO){
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new RecordClassVisitor(jdkVersionEnum, cw, recordDTO);
        cr.accept(cv, ClassReader.SKIP_FRAMES);
    }

    public static Integer scanRelationShipFromClassBuffer(JdkVersionEnum jdkVersionEnum, byte[] classfileBuffer, RecordDTO recordDTO){
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        RelationClassVisitor cv = new RelationClassVisitor(jdkVersionEnum, cw, recordDTO);
        cr.accept(cv, ClassReader.SKIP_FRAMES);

        return 0;
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
    public static JSONObject getJSONChainFromRelationShip(Map<String, Map<String, List<String>>> relationShips, String startFullMethodName, RecordDTO recordDTO){
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
            if(recordDTO.getInterfaceRecord().containInterface(currentClassName) || recordDTO.getAbstractRecord().containAbstract(currentClassName)){
                List<String> entries = recordDTO.getInterfaceRecord().getEntries(currentClassName);
                Map<String, Boolean> abstractMethod = recordDTO.getInterfaceRecord().getMethod(currentClassName);
                if(entries==null){
                    entries = recordDTO.getAbstractRecord().getEntries(currentClassName);
                    abstractMethod = recordDTO.getAbstractRecord().getMethod(currentClassName);
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

    /*
        从更新的method处逆向解析调用链
     */
    public static Pair<ChainNode, List<RecordDTO.Entrance>> getChainFromUpdateMethod(ChainNode node, RecordDTO recordDTO){
        List<RecordDTO.Entrance> entrances = new ArrayList<>();
        RelationRecord relationRecord = recordDTO.getRelationRecord();
        RelationReverseRecord relationReverseRecord = recordDTO.getRelationReverseRecord();
        List<String> relationReverseMethod = relationReverseRecord.getRelationReverseRecord(node.getCurrentClassName(), node.getCurrentMethodName());
        if (relationReverseMethod == null || relationReverseMethod.isEmpty()){
            // 逆向记录数据为空，无法解析
            return new Pair<>(node, entrances);
        }
        Stack<ChainNode> stack = new Stack<>();
        stack.push(node);
        List<String> exitMethods = new ArrayList<>();
        while (!stack.empty()){
            ChainNode current = stack.pop();
            Map<String, ChainNode> prevs = new HashMap<>();
            current.setPrevs(prevs);
            List<String> currentReverseMethod = relationReverseRecord.getRelationReverseRecord(current.getCurrentClassName(), current.getCurrentMethodName());
            RecordDTO.Entrance entrance = recordDTO.checkEntrance(current.getCurrentClassName(), current.getCurrentMethodName());
            log.debug("当前方法：{}， 入口函数：{}", current.getCurrentClassName() + METHOD_SPLIT + current.getCurrentMethodName(), entrance);
            if (entrance != null){
                entrances.add(entrance);
            }else if (currentReverseMethod != null && !exitMethods.contains(current.getCurrentClassName() + METHOD_SPLIT + current.getCurrentMethodName())){
                exitMethods.add(current.getCurrentClassName() + METHOD_SPLIT + current.getCurrentMethodName());
                currentReverseMethod.forEach(method -> {
                    String className = method.substring(0, method.indexOf(METHOD_SPLIT));
                    String methodName = method.substring(method.indexOf(METHOD_SPLIT)+1);
                    ChainNode chainNode = ChainNode.builder()
                            .currentClassName(className)
                            .currentMethodName(methodName)
                            .nexts(Map.of(current.getCurrentClassName() + METHOD_SPLIT + current.getCurrentMethodName(), current))
                            .build();
                    prevs.put(method, chainNode);
                    stack.push(chainNode);
                });
            }
        }
        return new Pair<>(node, entrances);
    }

    public static List<String> getProjectUpdateMethod(List<String> modules, String baseDir, String compareDir, boolean isCheckXml) throws NotDirectoryException, FileNotFoundException, DocumentException, InterruptedException {
        List<String> result = new CopyOnWriteArrayList<>();
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
            List<Future<?>> compireFutures = new ArrayList<>();
            for(String file:modifyFiles){
                String moduleName = module.equals(compareDir) ? "" : module.substring(compareDir.length()+1);
                compireFutures.add(taskThreadPool.submit(() -> {
                    try{
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
                    }catch (FileNotFoundException e){
                        throw new RuntimeException(e);
                    }
                }));
            }
            ThreadUtil.waitForCompletion("获取更新方法", compireFutures);
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

            if(new File(oldResource).exists() && new File(newResource).exists() && isCheckXml){
                Map<String, String> oldMybatisXml = FileUtil.scanMybatisXml(oldResource);
                Map<String, String> newMybatisXml = FileUtil.scanMybatisXml(newResource);
                List<Future<?>> futures = new ArrayList<>();
                for(String xml : newMybatisXml.keySet()){
                    if(oldMybatisXml.containsKey(xml)){
                        futures.add(taskThreadPool.submit(() -> {
                            List<String> xmlDiff = null;
                            try {
                                xmlDiff = XmlDiffUtil.compireXml(oldMybatisXml.get(xml), newMybatisXml.get(xml));
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
                            } catch (DocumentException | FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }));
                    }
                }
                ThreadUtil.waitForCompletion("检索xml", futures);
            }
        }
        return result;
    }
}
