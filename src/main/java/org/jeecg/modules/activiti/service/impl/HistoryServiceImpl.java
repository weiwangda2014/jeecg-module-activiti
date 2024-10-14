package org.jeecg.modules.activiti.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.db.Page;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.googlecode.aviator.AviatorEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.activiti.constants.ActivitiConstant;
import org.jeecg.modules.activiti.entity.*;
import org.jeecg.modules.activiti.entity.VO.HistoryTaskVo;
import org.jeecg.modules.activiti.entity.VO.TaskVo;
import org.jeecg.modules.activiti.mapper.ExtendModelMapper;
import org.jeecg.modules.activiti.service.*;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.jeecg.modules.online.cgform.service.IOnlCgformFieldService;
import org.jeecg.modules.online.cgform.service.IOnlCgformHeadService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HistoryServiceImpl implements IHistoryService {

    @Resource
    private HistoryService historyService;

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private IExtendHiModelFormDataService extendHiModelFormDataService;

    @Resource
    private IExtendModelFormDataService extendModelFormDataService;

    @Resource
    private IExtendModelFormService extendModelFormService;

    @Resource
    private ISysBaseAPI sysBaseAPI;

    @Resource
    private ExtendModelMapper extendModelMapper;

    @Resource
    private IOnlCgformHeadService onlCgformHeadService;

    @Resource
    private IOnlCgformFieldService fieldService;

    @Resource
    private IExtendKNodeService iActKNodeService;

    @Resource
    private IExtendKNodeDesignService actKNodeDesignService;

    @Resource
    private IExtendKHandleResultService iActKHandleResultService;

    @Resource
    private IExtendKAppendFormDeploymentService iActKAppendFormDeploymentService;

    @Resource
    private IExtendKAppendFormDataService iActKAppendFormDataService;

    @Resource
    private IExtendKNodeFileService iActKNodeFileService;

    @Resource
    private TaskService taskService;

    @Override
    public Map<String, Object> doneList(TaskVo taskVo, Page page) {
        Map<String, Object> result = new HashMap<>();

        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();
        if (StringUtils.isNotEmpty(taskVo.getCreateBy())) {
            List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().startedBy(taskVo.getCreateBy()).list();
            List<String> ids = new ArrayList<>();
            historicProcessInstances.forEach(item -> ids.add(item.getId()));
            if (ids.size() > 0) {
                query.processInstanceIdIn(ids);
            }
        }

        query.taskAssignee(sysUser.getUsername())
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc();
        System.out.println("getStartPosition:" +
                page.getStartPosition() + "getEndPosition:" +
                page.getEndPosition() + "getPageSize:" +
                page.getPageSize() + "getPageNumber:" +
                page.getPageNumber() + "getStartIndex:" +
                page.getStartIndex() + "getEndIndex:" +
                page.getEndIndex());
        List<HistoricTaskInstance> taskList = query.listPage(page.getStartPosition(), page.getEndPosition());
        long total = query.count();

        List<Map<String, Object>> tasks = new ArrayList<>();

        for (HistoricTaskInstance historicTaskInstance : taskList) {
            if (StringUtils.isNotEmpty(taskVo.getTaskId()) && !historicTaskInstance.getId().equals(taskVo.getTaskId())) {
                continue;
            }
            Map<String, Object> task = new HashMap<>();
            ExtendHiModelFormData extendHiModelFormData = extendHiModelFormDataService.getOne(new QueryWrapper<ExtendHiModelFormData>().lambda()
                    .eq(ExtendHiModelFormData::getProcessInstanceId, historicTaskInstance.getProcessInstanceId()));
            if (extendHiModelFormData != null) {
                Model model = repositoryService.createModelQuery().modelId(extendHiModelFormData.getModelId()).singleResult();

                if (StringUtils.isEmpty(taskVo.getModelKey())
                        && StringUtils.isEmpty(taskVo.getModelName())) {
                    handelHistoryTask(task, model, extendHiModelFormData, historicTaskInstance);
                    tasks.add(task);
                } else if (!StringUtils.isEmpty(taskVo.getModelKey())
                        && StringUtils.isEmpty(taskVo.getModelName()) && model.getKey().contains(taskVo.getModelKey())) {
                    handelHistoryTask(task, model, extendHiModelFormData, historicTaskInstance);
                    tasks.add(task);
                } else if (StringUtils.isEmpty(taskVo.getModelKey())
                        && !StringUtils.isEmpty(taskVo.getModelName()) && model.getName().contains(taskVo.getModelName())) {
                    handelHistoryTask(task, model, extendHiModelFormData, historicTaskInstance);
                    tasks.add(task);
                } else if (StringUtils.isNotEmpty(taskVo.getModelKey())
                        && StringUtils.isNotEmpty(taskVo.getModelName())
                        && model.getKey().contains(taskVo.getModelKey())
                        && model.getName().contains(taskVo.getModelName())) {
                    handelHistoryTask(task, model, extendHiModelFormData, historicTaskInstance);
                    tasks.add(task);
                }
            }
        }

        result.put("records", tasks);
        result.put("total", total);
        return result;
    }

    @Override
    public List<HistoryTaskVo> historicFlow(String processInstanceId, String taskId) {
        List<HistoryTaskVo> list = new ArrayList<>();
        List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId).orderByHistoricTaskInstanceStartTime().desc().list();
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();

        boolean isShowAppendForm = false;
        if (historicTaskInstance != null) {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(historicTaskInstance.getProcessDefinitionId()).singleResult();
            Model model = repositoryService.createModelQuery().deploymentId(processDefinition.getDeploymentId()).singleResult();
            ExtendKNodeDesign actKNode = actKNodeDesignService.getOne(new QueryWrapper<ExtendKNodeDesign>()
                    .lambda()
                    .eq(ExtendKNodeDesign::getNodeId, historicTaskInstance.getTaskDefinitionKey())
                    .eq(ExtendKNodeDesign::getModelId, model.getId()));
            if (actKNode != null && actKNode.getIsShowAppendForm().equals(1)) {
                isShowAppendForm = actKNode.getIsShowAppendForm().equals(1) ? true : false;
            }
        }
        for (HistoricTaskInstance e : taskList) {
            HistoryTaskVo htv = new HistoryTaskVo(e);

            // 处理审批意见
            ExtendKHandleResult actKHandleResult = iActKHandleResultService.getOne(new QueryWrapper<ExtendKHandleResult>()
                    .lambda()

                    .eq(ExtendKHandleResult::getNodeId, e.getTaskDefinitionKey())
                    .eq(ExtendKHandleResult::getProcessInstanceId, processInstanceId)
                    .eq(ExtendKHandleResult::getTaskId, htv.getId())
                    .eq(ExtendKHandleResult::getExecutionId, htv.getExecutionId()));
            if (actKHandleResult != null) {
                htv.setComment(actKHandleResult.getComment());
                htv.setFlagTxt(actKHandleResult.getResult());
            }
            //if(isShowAppendForm){
            // 查询附加表单数据
            ExtendKAppendFormDeployment actKAppendForm = iActKAppendFormDeploymentService.getOne(
                    new QueryWrapper<ExtendKAppendFormDeployment>()
                            .lambda()
                            .eq(ExtendKAppendFormDeployment::getNodeId, e.getTaskDefinitionKey())
                            .eq(ExtendKAppendFormDeployment::getProcessDefinitionId, e.getProcessDefinitionId()));

            // 如果该节点存在附加表单
            if (actKAppendForm != null) {
                ExtendKAppendFormData actKAppendFormData = iActKAppendFormDataService.getOne(new QueryWrapper<ExtendKAppendFormData>()
                        .lambda()
                        .eq(ExtendKAppendFormData::getNodeId, e.getTaskDefinitionKey())
                        .eq(ExtendKAppendFormData::getProcessInstanceId, processInstanceId)
                        .eq(ExtendKAppendFormData::getTaskId, htv.getId())
                        .eq(ExtendKAppendFormData::getExecutionId, htv.getExecutionId()));

                htv.setActKAppendForm(actKAppendForm);
                htv.setActKAppendFormData(actKAppendFormData);
            }
            //}

            // 查询上传的附件
            List<ExtendKNodeFile> actKNodeFiles = iActKNodeFileService.list(new QueryWrapper<ExtendKNodeFile>()
                    .lambda()
                    .eq(ExtendKNodeFile::getNodeId, e.getTaskDefinitionKey())
                    .eq(ExtendKNodeFile::getProcessInstanceId, processInstanceId)
                    .eq(ExtendKNodeFile::getTaskId, htv.getId())
                    .eq(ExtendKNodeFile::getExecutionId, htv.getExecutionId()));
            if (CollectionUtils.isNotEmpty(actKNodeFiles)) {
                htv.setActKNodeFiles(actKNodeFiles);
            }

            // 获取节点出去的线
            String nodeId = e.getTaskDefinitionKey();
            ExtendKNode actKNode = iActKNodeService.getOne(new QueryWrapper<ExtendKNode>()
                    .lambda()
                    .eq(ExtendKNode::getNodeId, nodeId)
                    .eq(ExtendKNode::getProcessDefinitionId, e.getProcessDefinitionId()));
            htv.setOutgoing(actKNode.getOutgoing());
            try {
                htv.setAssignee(sysBaseAPI.getUserByName(htv.getAssignee()).getRealname());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            list.add(htv);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getHistoryNode(String processInstanceId, String taskId) {

        List<Map<String, Object>> resultList = new ArrayList<>();
        List<HistoricActivityInstance> tempList = new ArrayList<>();

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
            String currActivityId = task.getTaskDefinitionKey();

            // 添加发起人任务节点

            Map<String, Object> tempMap = new HashMap<>();
            tempMap.put("nodeName", ActivitiConstant.PROCESS_CREATOR_NAME);
            tempMap.put("nodeId", ActivitiConstant.PROCESS_CREATOR_KEY);
            resultList.add(tempMap);

            List<HistoricActivityInstance> activityFinishedList = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .activityType("userTask")
                    .finished()
                    .orderByHistoricActivityInstanceEndTime()
                    .desc()
                    .list();

            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());

            getReverseNodeList(currActivityId, bpmnModel, activityFinishedList, tempList);

            for (HistoricActivityInstance historicActivityInstance : tempList) {
                tempMap = new HashMap<>();
                tempMap.put("nodeName", historicActivityInstance.getActivityName());
                tempMap.put("nodeId", historicActivityInstance.getActivityId());
                if (!resultList.contains(tempMap)) {
                    resultList.add(tempMap);
                }
            }
        }

        return resultList;
    }


    /**
     * 逆向获取执行过的任务节点：
     * 1、子流程只能获取到本子流程节点+主流程节点，不能获取兄弟流程任务节点；
     * 2、主流程任务节点不能获取子流程任务节点；
     *
     * @param currActivityId       当前活动ID
     * @param bpmnModel            bpmn模型
     * @param activityFinishedList 已完成活动ID
     * @param tempList             临时变量
     */
    private void getReverseNodeList(String currActivityId, BpmnModel bpmnModel, List<HistoricActivityInstance> activityFinishedList, List<HistoricActivityInstance> tempList) {
        // 获取当前节点的进线，通过进线查询上个节点
        FlowNode currFlow = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currActivityId);
        List<SequenceFlow> incomingFlows = currFlow.getIncomingFlows();

        // 找到上个任务节点
        for (SequenceFlow incomingFlow : incomingFlows) {
            String sourceNodeId = incomingFlow.getSourceRef();
            FlowNode sourceFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(sourceNodeId);
            String gatewayType = sourceNodeId.substring(sourceNodeId
                    .lastIndexOf("_") + 1);
            if ((sourceFlowNode instanceof ParallelGateway || sourceFlowNode instanceof InclusiveGateway) && "END".equalsIgnoreCase(gatewayType)) {
                sourceNodeId = sourceNodeId.substring(0, sourceNodeId
                        .lastIndexOf("_")) + "_start";
                getReverseNodeList(sourceNodeId, bpmnModel, activityFinishedList, tempList);
            } else {
                HistoricActivityInstance tempActivityInstance = null;
                for (HistoricActivityInstance historicActivityInstance : activityFinishedList) {
                    if (historicActivityInstance.getActivityId().equals(sourceNodeId)) {
                        tempActivityInstance = historicActivityInstance;
                        break;
                    }
                }

                // 解决画回头线出现死循环的问题
                if (!tempList.contains(tempActivityInstance)) {
                    if (tempActivityInstance != null) {
                        tempList.add(tempActivityInstance);
                    }
                    getReverseNodeList(sourceNodeId, bpmnModel, activityFinishedList, tempList);
                }
            }
        }
    }

    private void handelHistoryTask(Map<String, Object> taskMap, Model model, ExtendHiModelFormData extendHiModelFormData, HistoricTaskInstance task) {

        ExtendModelFormData modelFormData = extendModelFormDataService.getOne(
                new QueryWrapper<ExtendModelFormData>()
                        .lambda()
                        .eq(ExtendModelFormData::getProcessInstanceId,
                                task.getProcessInstanceId())
        );
        ExtendModelForm modelForm = extendModelFormService.getOne(new QueryWrapper<ExtendModelForm>()
                .lambda()
                .eq(ExtendModelForm::getModelId,
                        extendHiModelFormData.getModelId()));
        taskMap.put("modelName", model.getName());
        taskMap.put("modelKey", model.getKey());
        taskMap.put("id", task.getId());
        taskMap.put("name", task.getName());
        taskMap.put("createBy", task.getOwner());
        taskMap.put("createTime", task.getCreateTime());
        taskMap.put("modelId", extendHiModelFormData.getModelId());
        taskMap.put("startTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(task.getStartTime()));
        taskMap.put("endTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(task.getEndTime()));
        taskMap.put("totalTime", DateUtil.formatBetween(task.getStartTime(), task.getEndTime()));
        taskMap.put("processDefinitionId", task.getProcessDefinitionId());
        taskMap.put("processInstanceId", task.getProcessInstanceId());
        taskMap.put("formData", extendHiModelFormData.getFormData());
        if (modelForm.getFormCategory().equals(1)) {
            taskMap.put("htmlJson", modelForm.getHtmlJson());
        }
        if (modelForm.getFormCategory().equals(2)) {
            taskMap.put("dataId", extendHiModelFormData.getDataId());
            taskMap.put("tableId", extendHiModelFormData.getTableId());
            taskMap.put("tableName", extendHiModelFormData.getTableName());
        }
        // 查询表单数据ID
        LoginUser sysUser = sysBaseAPI.getUserByName(extendHiModelFormData.getCreateBy());
        taskMap.put("instanceName", getInstanceName(modelFormData));
        taskMap.put("createBy", sysUser.getRealname());
    }


    // 获取业务标题
    private String getInstanceName(ExtendModelFormData extendModelFormData) {
        if (extendModelFormData == null) {
            return "无业务标题";
        }
        ExtendModel extendModel = extendModelMapper.selectOne(
                new QueryWrapper<ExtendModel>()
                        .lambda()
                        .eq(ExtendModel::getModelId, extendModelFormData.getModelId())
        );
        Map<String, Object> variables = new HashMap<>();
        if (extendModelFormData.getTableId() == null) {
            Map formData = JSONObject.parseObject(extendModelFormData.getFormData(), Map.class);
            variables.putAll(formData);
        } else {
            OnlCgformHead head = onlCgformHeadService.getById(extendModelFormData.getTableId());
            Map<String, Object> formData = this.fieldService.queryFormData(extendModelFormData.getTableId(), head.getTableName(), extendModelFormData.getDataId());
            variables.putAll(formData);
        }
        String expression = extendModel.getTitleExpression();
        if (StringUtils.isEmpty(expression)) {
            return "无业务标题";
        }
        String title;
        try {
            title = AviatorEvaluator.execute(expression, variables) + "";
        } catch (Exception e) {
            log.error("expression========>" + expression);
            title = "标题加载失败";
        }
        return title;
    }
}
