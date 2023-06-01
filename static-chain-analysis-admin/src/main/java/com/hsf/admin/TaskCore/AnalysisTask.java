package com.hsf.admin.TaskCore;

import com.alibaba.fastjson.JSONObject;
import com.hsf.admin.Code.TaskStatus;
import com.hsf.admin.Mapper.AnalysisSimpleReportMapper;
import com.hsf.admin.Mapper.TaskInfoMapper;
import com.hsf.admin.Pojo.Entities.AnalysisSimpleReport;
import com.hsf.admin.Pojo.Entities.TaskInfo;
import com.hsf.admin.Pojo.Requests.CompareInfo;
import com.hsf.admin.TaskCore.Interface.CallBack;
import com.hsf.admin.TaskCore.Interface.PreRun;
import com.hsf.core.Recorders.*;
import com.hsf.core.Services.ScanService;
import com.hsf.core.Utils.ChainUtils;
import com.hsf.core.Utils.ParseUtils;
import com.hsf.tools.Utils.BasicUtil;
import com.hsf.tools.Utils.FileUtil;

import java.util.*;

import static com.hsf.tools.Config.Code.*;

public class AnalysisTask implements Runnable {

    private Integer nodeId;
    private final String rootPath;
    private final CompareInfo compareInfo;
    private final PreRun preRun;
    private final CallBack callBack;
    private final TaskInfoMapper taskInfoMapper;
    private final TaskInfo taskInfo;
    private final ScanService scanService;
    private AnalysisSimpleReportMapper analysisSimpleReportMapper;

    public AnalysisTask(
        Integer nodeId, String rootPath, CompareInfo compareInfo, TaskInfo taskInfo, ScanService scanService,
        TaskInfoMapper taskInfoMapper, AnalysisSimpleReportMapper analysisSimpleReportMapper,
        PreRun preRun, CallBack callBack
    ){
        this.nodeId = nodeId;
        this.rootPath = rootPath;
        this.compareInfo = compareInfo;
        this.taskInfo = taskInfo;
        this.scanService = scanService;
        this.taskInfoMapper = taskInfoMapper;
        this.analysisSimpleReportMapper = analysisSimpleReportMapper;
        this.preRun = preRun;
        this.callBack = callBack;
    }
    @Override
    public void run() {
        if (preRun != null){
            preRun.run();
        }
        if (!preRun.isSuccess()){
            taskInfo.setStatus(TaskStatus.FAILD.code);
            taskInfoMapper.updateTaskInfo(taskInfo);
            return;
        }
        // analysis
        String basePath = BasicUtil.getBranchFullPath(rootPath, compareInfo.getBase(), compareInfo.getBaseCommitId());
        String comparePath = BasicUtil.getBranchFullPath(rootPath, compareInfo.getCompare(), compareInfo.getCompareCommitId());

        try {
            Map<String, List<String>> modules = scanService.getModuleDiff(basePath, comparePath);
            List<String> newModules = modules.get("newModules");
            List<String> normalModules = modules.get("normalModules");
            List<String> allModules = new ArrayList<>();
            allModules.addAll(newModules);
            allModules.addAll(normalModules);

            List<Recorder> recorders = scanService.recordProjectClass(allModules);
            ApiRecord apiRecord = new ApiRecord();
            ControllerRecord controllerRecord = new ControllerRecord();
            recorders.add(apiRecord);
            recorders.add(controllerRecord);
            Map<String, Map<String, List<String>>> relationShips = scanService.getRelationShips(allModules, recorders);
            Map<String, JSONObject> chain = new HashMap<>();

            for (String controllerName : controllerRecord.getControllers()){
                for(String methodName:controllerRecord.getApiFromControlClassName(controllerName)){
                    String fullMethodName = controllerName + METHOD_SPLIT + methodName;
                    chain.put(fullMethodName, ChainUtils.getJSONChainFromRelationShip(
                        relationShips, fullMethodName,
                        (InterfaceRecord) recorders.get(0),
                        (AbstractRecord) recorders.get(1)
                    ));
                }
            }
            for(String dubboMethodName : ((DubboRecord) recorders.get(2)).getList()){
                chain.put(dubboMethodName, ChainUtils.getJSONChainFromRelationShip(
                    relationShips, dubboMethodName,
                    (InterfaceRecord) recorders.get(0),
                    (AbstractRecord) recorders.get(1)
                ));
            }

            // 检查更新
            List<String> update = new ArrayList<>();
            for (String module : newModules){
                // 新模块的所有代码加入update
                List<String> scan = FileUtil.scanForDirectory(module);
                for (String file : scan){
                    List<String> scanMethods = ParseUtils.scanMethods(file);
                    update.addAll(scanMethods);
                }
            }
            update.addAll(ChainUtils.getProjectUpdateMethod(normalModules, basePath, comparePath));

            List<AnalysisSimpleReport> analysisSimpleReports = new ArrayList<>();
            for (String updateMethod : update){
                for (String startChain : chain.keySet()){
                    if (startChain.equals(updateMethod) || chain.get(startChain).toJSONString().contains(updateMethod)){
                        Set<String> apis = apiRecord.getApis(startChain);
                        if (apis == null){
                            AnalysisSimpleReport analysisSimpleReport = new AnalysisSimpleReport();
                            analysisSimpleReport.setTaskId(taskInfo.getId());
                            analysisSimpleReport.setType(DUBBO);
                            analysisSimpleReport.setApiName(startChain.replace(PACKAGE_SPLIT, METHOD_SPLIT));
                            analysisSimpleReports.add(analysisSimpleReport);
                        }else {
                            for (String api : apis){
                                AnalysisSimpleReport analysisSimpleReport = new AnalysisSimpleReport();
                                analysisSimpleReport.setTaskId(taskInfo.getId());
                                analysisSimpleReport.setType(HTTP);
                                analysisSimpleReport.setApiName(api);
                                analysisSimpleReports.add(analysisSimpleReport);
                            }
                        }
                    }
                }
            }
            int fromIndex = 0;
            while (fromIndex != analysisSimpleReports.size()){
                // 300条为batch，批量插入
                analysisSimpleReportMapper.insertReports(analysisSimpleReports.subList(
                    fromIndex, Math.min(fromIndex + 300, analysisSimpleReports.size())
                ));
                fromIndex = Math.min(fromIndex + 300, analysisSimpleReports.size());
            }
            taskInfo.setStatus(TaskStatus.SUCCESS.code);
            taskInfoMapper.updateTaskInfo(taskInfo);
        } catch (Exception e) {
            taskInfo.setStatus(TaskStatus.FAILD.code);
            taskInfoMapper.updateTaskInfo(taskInfo);
            throw new RuntimeException(e);
        }

        if (callBack != null){
            callBack.run();
        }
    }
}
