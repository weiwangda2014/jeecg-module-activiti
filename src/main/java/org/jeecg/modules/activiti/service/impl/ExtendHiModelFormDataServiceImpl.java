package org.jeecg.modules.activiti.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.collections4.CollectionUtils;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.activiti.constants.ActivitiConstant;
import org.jeecg.modules.activiti.entity.*;
import org.jeecg.modules.activiti.mapper.ExtendHiModelFormDataMapper;
import org.jeecg.modules.activiti.service.IExtendHiModelFormDataService;
import org.jeecg.modules.activiti.service.IExtendKAppendFormDataService;
import org.jeecg.modules.activiti.service.IExtendKAppendFormDeploymentService;
import org.jeecg.modules.activiti.service.IExtendKNodeDesignService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExtendHiModelFormDataServiceImpl extends ServiceImpl<ExtendHiModelFormDataMapper, ExtendHiModelFormData> implements IExtendHiModelFormDataService {

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private IExtendKNodeDesignService actKNodeDesignService;

    @Resource
    private HistoryService historyService;

    @Resource
    private IExtendKAppendFormDeploymentService iActKAppendFormDeploymentService;

    @Resource
    private ISysBaseAPI iSysBaseAPI;

    @Resource
    private IExtendKAppendFormDataService iActKAppendFormDataService;


    /**
     * 保存历史数据
     *
     * @param extendModelFormData 模型表单数据
     * @param processInstanceId   实例ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveHiFormData(ExtendModelFormData extendModelFormData, String processInstanceId) {
        ExtendHiModelFormData extendHiModelFormData = new ExtendHiModelFormData();
        extendHiModelFormData.setProcessDefinitionId(extendModelFormData.getProcessDefinitionId());
        extendHiModelFormData.setFormData(extendModelFormData.getFormData());
        extendHiModelFormData.setTableId(extendModelFormData.getTableId());
        extendHiModelFormData.setTableName(extendModelFormData.getTableName());
        extendHiModelFormData.setModelId(extendModelFormData.getModelId());
        extendHiModelFormData.setFormId(extendModelFormData.getFormId());
        extendHiModelFormData.setProcessInstanceId(processInstanceId);
        extendHiModelFormData.setBpmnStatus(ActivitiConstant.HANDLE_STATUS_HLZ);
        extendHiModelFormData.setSubmitType(extendModelFormData.getSubmitType());
        extendHiModelFormData.setDataId(extendModelFormData.getDataId());
        super.save(extendHiModelFormData);
    }

    /**
     * @param taskId            任务ID
     * @param processInstanceId 实例ID
     * @return 数据
     */
    public List<Map<String, Object>> handleNodeAppendForm(String taskId, String processInstanceId) {
        HistoricTaskInstance historicTaskInstance = historyService
                .createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .singleResult();
        List<Map<String, Object>> result = new ArrayList<>();
        if (historicTaskInstance != null) {
            ProcessDefinition processDefinition = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionId(historicTaskInstance.getProcessDefinitionId())
                    .singleResult();
            Model model = repositoryService
                    .createModelQuery()
                    .modelKey(processDefinition.getKey())
                    .singleResult();
            ExtendKNodeDesign actKNode = actKNodeDesignService
                    .getOne(new QueryWrapper<ExtendKNodeDesign>()
                            .lambda()
                            .eq(ExtendKNodeDesign::getNodeId, historicTaskInstance.getTaskDefinitionKey())
                            .eq(ExtendKNodeDesign::getModelId, model.getId()));

            if (actKNode != null && actKNode.getIsShowAppendForm().equals(1)) {
                BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());

                List<HistoricActivityInstance> activityFinishedList = historyService
                        .createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .finished()
                        .orderByHistoricActivityInstanceEndTime()
                        .desc()
                        .list();

                List<HistoricActivityInstance> tempList = new ArrayList<>();

                getReverseNodeList(historicTaskInstance.getTaskDefinitionKey(), bpmnModel, activityFinishedList, tempList);

                for (HistoricActivityInstance historicActivityInstance : tempList) {
                    Map<String, Object> tempMap = new HashMap<>();

                    String activityId = historicActivityInstance.getActivityId();
                    ExtendKAppendFormDeployment actKAppendFormDeployment = iActKAppendFormDeploymentService.getOne(
                            new QueryWrapper<ExtendKAppendFormDeployment>()
                                    .lambda()
                                    .eq(ExtendKAppendFormDeployment::getNodeId, activityId)
                                    .eq(ExtendKAppendFormDeployment::getProcessDefinitionId, historicTaskInstance.getProcessDefinitionId()));
                    if (actKAppendFormDeployment != null) {
                        tempMap.put("nodeId", activityId);
                        tempMap.put("formJson", actKAppendFormDeployment.getFormJson());
                        tempMap.put("nodeName", historicActivityInstance.getActivityName());
                        tempMap.put("assignee", iSysBaseAPI.getUserByName(historicActivityInstance.getAssignee()).getRealname());

                        ExtendKAppendFormData actKAppendFormData = iActKAppendFormDataService.getOne(new QueryWrapper<ExtendKAppendFormData>()
                                .lambda()
                                .eq(ExtendKAppendFormData::getNodeId, activityId)
                                .eq(ExtendKAppendFormData::getProcessInstanceId, processInstanceId)
                                .eq(ExtendKAppendFormData::getTaskId, historicActivityInstance.getTaskId())
                                .eq(ExtendKAppendFormData::getExecutionId, historicActivityInstance.getExecutionId()));

                        if (actKAppendFormData != null) {
                            tempMap.put("formData", actKAppendFormData.getFormData());
                        } else {
                            List<ExtendKAppendFormData> actKAppendFormDataList = iActKAppendFormDataService.list(new QueryWrapper<ExtendKAppendFormData>()
                                    .lambda()
                                    .eq(ExtendKAppendFormData::getNodeId, activityId)
                                    .eq(ExtendKAppendFormData::getProcessInstanceId, processInstanceId)
                                    .orderByDesc(ExtendKAppendFormData::getCreateTime));
                            if (CollectionUtils.isNotEmpty(actKAppendFormDataList)) {
                                tempMap.put("formData", actKAppendFormDataList.get(0).getFormData());
                            }
                        }
                        result.add(tempMap);
                    }
                }
            }
        }
        return result;
    }


    /**
     * 逆向获取执行过的任务节点：
     * 1、子流程只能获取到本子流程节点+主流程节点，不能获取兄弟流程任务节点；
     * 2、主流程任务节点不能获取子流程任务节点；
     *
     * @param currActivityId       当前活动ID
     * @param bpmnModel            模型
     * @param activityFinishedList 已完成ID
     * @param tempList             临时实例
     */
    private void getReverseNodeList(String currActivityId, BpmnModel bpmnModel, List<HistoricActivityInstance> activityFinishedList, List<HistoricActivityInstance> tempList) {
        // 获取当前节点的进线，通过进线查询上个节点
        FlowNode currFlow = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currActivityId);
        List<SequenceFlow> incomingFlows = currFlow.getIncomingFlows();

        // 找到上个任务节点
        for (SequenceFlow incomingFlow : incomingFlows) {
            String sourceNodeId = incomingFlow.getSourceRef();
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
