package com.analysis.admin.Service;

import com.analysis.core.Entitys.DTO.RecordDTO;
import com.analysis.core.Enums.JdkVersionEnum;
import com.analysis.core.Utils.ChainUtils;
import com.analysis.tools.Utils.FileUtil;
import com.analysis.tools.Utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static com.analysis.tools.Config.Code.*;

/*
 * 负责扫描并记录
 * 包括：
 *   项目的目录结构
 *   项目的子模块存在更新、新增的
 *   项目内的各种类型，如interface/dubbo/abstract/controller等
 * */
@Slf4j
@Service
public class ScanServiceV3 {
    @Resource(name = "readClassBytesThreadPool")
    private ThreadPoolExecutor readClassBytesThreadPool;

    @Resource(name = "cpuTaskThreadPool")
    private ThreadPoolExecutor cpuTaskThreadPool;

    public RecordDTO recordProjectClass(JdkVersionEnum jdkVersionEnum, List<String> modules) throws IOException, InterruptedException {
        RecordDTO recordDTO = new RecordDTO();
        List<Future<?>> futures = new ArrayList<>();

        for (String module : modules){
            String rootDir = module + URL_SPLIT + TARGET;
            List<String> filePaths = FileUtil.scanForDirectory(rootDir);
            for (String filePath : filePaths){
                futures.add(cpuTaskThreadPool.submit(() -> {
                    try {
                        ChainUtils.scanForClassName(jdkVersionEnum, FileUtils.readFileToByteArray(new File(filePath)), recordDTO);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
        }
        ThreadUtil.waitForCompletion("记录项目入口", futures);

        return recordDTO;
    }

    public Map<String, List<String>> getModuleDiff(String baseDir, String compareDir) throws ParserConfigurationException, IOException, SAXException {
        File baseRoot = new File(baseDir);
        File compareRoot = new File(compareDir);
        if (!baseRoot.exists() || !baseRoot.isDirectory() || !compareRoot.exists() || !compareRoot.isDirectory()){
            return new HashMap<>();
        }
        File compareRootPom = new File(compareDir + URL_SPLIT + POM);
        List<String> newModules = new ArrayList<>();            // 新增的模块，不需要走parser对比，直接检索并加入 new Method 列表
        List<String> normalModules = new ArrayList<>();         // 非新增模块，需要走正常流程
        File baseRootSource = new File(baseDir + URL_SPLIT + SOURCE);
        File compareRootSource = new File(compareDir + URL_SPLIT + SOURCE);

        Stack<FileNode> start = new Stack<>();
        if(compareRootSource.exists() && compareRootSource.listFiles().length > 0){
            // 根目录有源码
            if(baseRootSource.exists() && baseRootSource.listFiles().length > 0){
                // 旧项目根目录也TM有源码
                normalModules.add(compareDir);
            }else{
                newModules.add(compareDir);
            }
        }
        for(String module:listModules(compareRootPom)){
            // 初始化start
            start.push(new FileNode(
                    new StringBuilder(""),
                    new File(compareDir + URL_SPLIT + module + URL_SPLIT + POM)
            ));
        }

        while(!start.empty()){
            FileNode topItem = start.pop();
            String currentModuleName = topItem.fileNode.getAbsolutePath();
            currentModuleName = currentModuleName.substring(0, currentModuleName.lastIndexOf(URL_SPLIT));
            currentModuleName = currentModuleName.substring(currentModuleName.lastIndexOf(URL_SPLIT)+1);
            File currentModuleSource = new File(compareDir + topItem.relativePath.toString() + URL_SPLIT + currentModuleName + URL_SPLIT + SOURCE);
            File oldModuleSource = new File(baseDir + topItem.relativePath.toString() + URL_SPLIT + currentModuleName  + URL_SPLIT + SOURCE);
            // 判断当前module是否有源码
            if(currentModuleSource.exists() && currentModuleSource.listFiles().length > 0){
                if(oldModuleSource.exists() && oldModuleSource.listFiles().length > 0){
                    normalModules.add(compareDir + topItem.relativePath.toString() + URL_SPLIT + currentModuleName);
                }else{
                    newModules.add(compareDir + topItem.relativePath.toString() + URL_SPLIT + currentModuleName);
                }
            }
            List<String> list = listModules(topItem.fileNode);

            for(String subModule:list){
                if(!URL_SPLIT.equals(PACKAGE_SPLIT)){
                    subModule = subModule.replace(PACKAGE_SPLIT, URL_SPLIT);
                }
                String currentPath = topItem.relativePath.append(URL_SPLIT).append(currentModuleName).toString();
                start.push(new FileNode(
                        new StringBuilder(currentPath),
                        new File(compareDir + currentPath + URL_SPLIT + subModule + URL_SPLIT + POM)
                ));
            }
        }
        Map<String, List<String>> result = new HashMap<>();
        result.put("newModules", newModules);
        result.put("normalModules", normalModules);
        return result;
    }

    /*
     * 根据模块列表，扫描所有调用关系
     * */
    public void scanRelationShips(JdkVersionEnum jdkVersionEnum, List<String> modules, RecordDTO recordDTO) throws IOException, InterruptedException {
        for (String module : modules){
            String rootDir = module + URL_SPLIT + TARGET;
            List<String> filePaths = FileUtil.scanForDirectory(rootDir);
            if (filePaths != null) {
                List<Future<?>> futures = new ArrayList<>();
                for (String filePath : filePaths){
                    String className = filePath.substring(rootDir.length()+1,filePath.lastIndexOf('.'));
                    if(!URL_SPLIT.equals(PACKAGE_SPLIT)){
                        className = className.replace(URL_SPLIT, PACKAGE_SPLIT);
                    }
                    // 考虑多线程扫描
                    futures.add(readClassBytesThreadPool.submit(() -> {
                        try{
                            ChainUtils.scanRelationShipFromClassBuffer(jdkVersionEnum, FileUtils.readFileToByteArray(new File(filePath)), recordDTO);
                            return 1;
                        }catch (Exception e){
                            log.error("getRelationShips, IOException: {}", e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }));
                }
                ThreadUtil.waitForCompletion("扫描调用关系", futures);
            }
        }
    }


    private List<String> listModules(File pom) throws ParserConfigurationException, IOException, SAXException {
        List<String> result = new ArrayList<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document document;
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.parse(pom);
        NodeList modules = document.getElementsByTagName("module");
        if(modules.getLength() == 0){
            return result;
        }else{
            for(int i=0; i<modules.getLength(); i++){
                result.add(modules.item(i).getFirstChild().getNodeValue());
            }
        }

        return result;
    }


    private static class FileNode{
        StringBuilder relativePath;
        File fileNode;
        public FileNode(StringBuilder relativePath, File fileNode){
            this.relativePath = relativePath;
            this.fileNode = fileNode;
        }
    }
}
