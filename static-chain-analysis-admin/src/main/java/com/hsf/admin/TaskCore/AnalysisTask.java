package com.hsf.admin.TaskCore;

import com.alibaba.fastjson.JSONObject;
import com.hsf.admin.Code.Response;
import com.hsf.admin.Code.TaskStatus;
import com.hsf.admin.Dto.Task.AnalysisTaskDTO;
import com.hsf.admin.Enums.TaskTypeEnum;
import com.hsf.admin.Pojo.Entities.AnalysisSimpleReport;
import com.hsf.admin.Pojo.Requests.TaskExecutionRequest;
import com.hsf.admin.Pojo.Responses.TaskExecutionResponse;
import com.hsf.admin.TaskCore.Base.BaseTaskExecutor;
import com.hsf.core.Enums.JdkVersionEnum;
import com.hsf.core.Recorders.*;
import com.hsf.core.Utils.ChainUtils;
import com.hsf.core.Utils.ParseUtils;
import com.hsf.tools.Utils.BasicUtil;
import com.hsf.tools.Utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.hsf.tools.Config.Code.*;

@Slf4j
@Component
public class AnalysisTask extends BaseTaskExecutor {

    public AnalysisTask() {
        super(TaskTypeEnum.ANALYSIS_TASK);
    }

    @Override
    public Response<TaskExecutionResponse> execute(TaskExecutionRequest<?> request) {
        AnalysisTaskDTO analysisTaskDTO = (AnalysisTaskDTO) request.getTaskPO();
        TaskExecutionResponse response = new TaskExecutionResponse();

        if (analysisTaskDTO.getPreRun() != null){
            analysisTaskDTO.getPreRun().run();
        }
        if (!analysisTaskDTO.getPreRun().isSuccess()){
            analysisTaskDTO.getTaskInfo().setStatus(TaskStatus.FAILD.code);
            analysisTaskDTO.getTaskInfoMapper().updateTaskInfo(analysisTaskDTO.getTaskInfo());
            return Response.failed(response);
        }
        // analysis
        String basePath = BasicUtil.getBranchFullPath(
                analysisTaskDTO.getRootPath(), analysisTaskDTO.getCompareInfo().getBase(), analysisTaskDTO.getCompareInfo().getBaseCommitId()
        );
        String comparePath = BasicUtil.getBranchFullPath(
                analysisTaskDTO.getRootPath(), analysisTaskDTO.getCompareInfo().getCompare(), analysisTaskDTO.getCompareInfo().getCompareCommitId()
        );

        try {
            Map<String, List<String>> modules = analysisTaskDTO.getScanService().getModuleDiff(basePath, comparePath);
            List<String> newModules = modules.get("newModules");
            List<String> normalModules = modules.get("normalModules");
            List<String> allModules = new ArrayList<>();
            allModules.addAll(newModules);
            allModules.addAll(normalModules);

            List<Recorder> recorders = analysisTaskDTO.getScanService().recordProjectClass(JdkVersionEnum.JDK17, allModules);
            ApiRecord apiRecord = new ApiRecord();
            ControllerRecord controllerRecord = new ControllerRecord();
            recorders.add(apiRecord);
            recorders.add(controllerRecord);
            Map<String, Map<String, List<String>>> relationShips = analysisTaskDTO.getScanService().getRelationShips(JdkVersionEnum.JDK17, allModules, recorders);
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
                            analysisSimpleReport.setTaskId(analysisTaskDTO.getTaskInfo().getId());
                            analysisSimpleReport.setType(DUBBO);
                            analysisSimpleReport.setApiName(startChain.replace(PACKAGE_SPLIT, METHOD_SPLIT));
                            analysisSimpleReports.add(analysisSimpleReport);
                        }else {
                            for (String api : apis){
                                AnalysisSimpleReport analysisSimpleReport = new AnalysisSimpleReport();
                                analysisSimpleReport.setTaskId(analysisTaskDTO.getTaskInfo().getId());
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
                analysisTaskDTO.getAnalysisSimpleReportMapper().insertReports(analysisSimpleReports.subList(
                        fromIndex, Math.min(fromIndex + 300, analysisSimpleReports.size())
                ));
                fromIndex = Math.min(fromIndex + 300, analysisSimpleReports.size());
            }
            analysisTaskDTO.getTaskInfo().setStatus(TaskStatus.SUCCESS.code);
            analysisTaskDTO.getTaskInfoMapper().updateTaskInfo(analysisTaskDTO.getTaskInfo());
        } catch (Exception e) {
            log.error("analysis error: " + e.getMessage());
            analysisTaskDTO.getTaskInfo().setStatus(TaskStatus.FAILD.code);
            analysisTaskDTO.getTaskInfoMapper().updateTaskInfo(analysisTaskDTO.getTaskInfo());
            throw new RuntimeException(e);
        }
        if (analysisTaskDTO.getCallBack() != null){
            analysisTaskDTO.getCallBack().run();
        }

        return Response.success(response);
    }
}
