package com.analysis.admin.TaskCore;

import com.analysis.admin.Code.Response;
import com.analysis.admin.Code.TaskStatus;
import com.analysis.admin.Dto.Task.AnalysisTaskDTO;
import com.analysis.admin.Enums.TaskTypeEnum;
import com.analysis.admin.Mapper.AnalysisSimpleReportMapper;
import com.analysis.admin.Mapper.TaskInfoMapper;
import com.analysis.admin.Pojo.Entities.AnalysisSimpleReport;
import com.analysis.admin.Pojo.Requests.TaskExecutionRequest;
import com.analysis.admin.Pojo.Responses.TaskExecutionResponse;
import com.analysis.admin.Service.ScanServiceV3;
import com.analysis.admin.TaskCore.Base.BaseTaskExecutor;
import com.analysis.corev2.Entitys.Chain.ChainNode;
import com.analysis.corev2.Entitys.DTO.RecordDTO;
import com.analysis.corev2.Enums.JdkVersionEnum;
import com.analysis.corev2.Recorders.Entrances.*;
import com.analysis.corev2.Recorders.Relation.RelationRecord;
import com.analysis.corev2.Recorders.Relation.RelationReverseRecord;
import com.analysis.corev2.Utils.ChainUtils;
import com.analysis.corev2.Utils.ParseUtils;
import com.analysis.tools.Pojo.Response.Pair;
import com.analysis.tools.Utils.BasicUtil;
import com.analysis.tools.Utils.FileUtil;
import com.analysis.tools.Utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.analysis.tools.Config.Code.*;

@Slf4j
@Component
public class AnalysisTask extends BaseTaskExecutor {

    @Autowired
    private ScanServiceV3 scanService;

    @Resource(name = "cpuTaskThreadPool")
    private ThreadPoolExecutor taskThreadPool;

    @Resource
    private TaskInfoMapper taskInfoMapper;

    @Resource
    private AnalysisSimpleReportMapper analysisSimpleReportMapper;

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
        if (analysisTaskDTO.getPreRun() != null && !analysisTaskDTO.getPreRun().isSuccess()) {
            analysisTaskDTO.getTaskInfo().setStatus(TaskStatus.FAILD.code);
            taskInfoMapper.updateTaskInfo(analysisTaskDTO.getTaskInfo());
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
            Map<String, List<String>> modules = scanService.getModuleDiff(basePath, comparePath);
            List<String> newModules = modules.get("newModules");
            List<String> normalModules = modules.get("normalModules");
            List<String> allModules = new ArrayList<>();
            allModules.addAll(newModules);
            allModules.addAll(normalModules);

            RecordDTO recorders = scanService.recordProjectClass(JdkVersionEnum.JDK17, allModules);
            scanService.scanRelationShips(JdkVersionEnum.JDK17, allModules, recorders);

            log.info("检查更新");
            List<String> update = new ArrayList<>();
            for (String module : newModules){
                // 新模块的所有代码加入update
                List<String> scan = FileUtil.scanForDirectory(module);
                for (String file : scan){
                    List<String> scanMethods = ParseUtils.scanMethods(file);
                    update.addAll(scanMethods);
                }
            }
            update.addAll(ChainUtils.getProjectUpdateMethod(normalModules, basePath, comparePath, true));
            log.info("检查更新完毕，开始解析调用链");

            log.info("开始解析调用链");
            Map<String, Future<Pair<ChainNode, List<RecordDTO.Entrance>>>> futures = new HashMap<>();
            update.forEach(method -> {
                String className = method.substring(0, method.indexOf(METHOD_SPLIT));
                String methodName = method.substring(method.indexOf(METHOD_SPLIT) + 1);
                ChainNode startNode = ChainNode.builder()
                        .currentClassName(className)
                        .currentMethodName(methodName)
                        .build();
                futures.put(className + METHOD_SPLIT + method, taskThreadPool.submit(() -> {
                    // 解析调用链
                    return ChainUtils.getChainFromUpdateMethod(startNode, recorders);
                }));
            });
            ThreadUtil.ThreadMapResult<String, Pair<ChainNode, List<RecordDTO.Entrance>>> relationShips = ThreadUtil.getFutureResult("逆向解析调用链", futures);

            List<AnalysisSimpleReport> analysisSimpleReports = new ArrayList<>();
            if (analysisTaskDTO.getTaskInfo() == null){
                // 测试链路，直接返回
                return Response.success(response);
            }
            log.info("解析调用链完毕，开始生成报告");
            Set<String> apis = new HashSet<>();
            relationShips.getResult().forEach((key, value) -> {
//                ChainNode startNode = value.getFirst();
                List<RecordDTO.Entrance> entrances = value.getSecond();
                entrances.forEach(entrance -> {
                    String api = String.join(",", entrance.getValue());
                    // 去重
                    if (!apis.contains(api)){
                        apis.add(api);
                        AnalysisSimpleReport analysisSimpleReport = new AnalysisSimpleReport();
                        analysisSimpleReport.setTaskId(analysisTaskDTO.getTaskInfo().getId());
                        analysisSimpleReport.setType(entrance.getType());
                        analysisSimpleReport.setApiName(api);
                        analysisSimpleReports.add(analysisSimpleReport);
                    }
                });
            });
            int fromIndex = 0;
            while (fromIndex != analysisSimpleReports.size()){
                // 300条为batch，批量插入
                analysisSimpleReportMapper.insertReports(analysisSimpleReports.subList(
                        fromIndex, Math.min(fromIndex + 300, analysisSimpleReports.size())
                ));
                fromIndex = Math.min(fromIndex + 300, analysisSimpleReports.size());
            }
            analysisTaskDTO.getTaskInfo().setStatus(TaskStatus.SUCCESS.code);
            taskInfoMapper.updateTaskInfo(analysisTaskDTO.getTaskInfo());
            log.info("解析任务执行成功");
        } catch (Exception e) {
            log.error("analysis error: {}", e.getMessage());
            analysisTaskDTO.getTaskInfo().setStatus(TaskStatus.FAILD.code);
            taskInfoMapper.updateTaskInfo(analysisTaskDTO.getTaskInfo());
            throw new RuntimeException(e);
        }
        if (analysisTaskDTO.getCallBack() != null){
            analysisTaskDTO.getCallBack().run();
        }

        return Response.success(response);
    }
}
