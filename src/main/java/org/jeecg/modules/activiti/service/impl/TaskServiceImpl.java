package org.jeecg.modules.activiti.service.impl;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.googlecode.aviator.AviatorEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.core.el.juel.ExpressionFactoryImpl;
import org.activiti.core.el.juel.TreeValueExpression;
import org.activiti.core.el.juel.util.SimpleContext;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.message.BusMessageDTO;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.activiti.constants.ActivitiConstant;
import org.jeecg.modules.activiti.entity.*;
import org.jeecg.modules.activiti.entity.DTO.ApprovalRecordDTO;
import org.jeecg.modules.activiti.entity.DTO.CreateTaskDTO;
import org.jeecg.modules.activiti.entity.DTO.RejectTargetNodeDTO;
import org.jeecg.modules.activiti.entity.DTO.TaskDTO;
import org.jeecg.modules.activiti.entity.VO.HistoricProcessVo;
import org.jeecg.modules.activiti.entity.VO.TaskVo;
import org.jeecg.modules.activiti.mapper.TaskMapper;
import org.jeecg.modules.activiti.service.*;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.jeecg.modules.online.cgform.service.IOnlCgformFieldService;
import org.jeecg.modules.online.cgform.service.IOnlCgformHeadService;
import org.jeecg.modules.online.config.exception.a;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskServiceImpl implements ITaskService {

    @Resource
    private TaskService taskService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private HistoryService historyService;

    @Resource
    private ISysBaseAPI sysBaseAPI;

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private TaskMapper taskMapper;

    @Resource
    private IOnlCgformFieldService fieldService;

    @Resource
    private IOnlCgformHeadService onlCgformHeadService;

    @Resource
    private IExtendModelService extendActModelService;

    @Resource
    private IExtendModelFormDataService extendModelFormDataService;

    @Resource
    private IExtendHiModelFormDataService extendHiModelFormDataService;

    @Resource
    private IExtendKHandleResultService extendKHandleResultService;

    @Resource
    private IExtendKNodeService extendKNodeService;

    @Resource
    private IExtendKAppendFormService extendKAppendFormService;

    @Resource
    private IExtendKProcessDuplicateService extendKProcessDuplicateService;

    @Resource
    private IExtendKNodeDesignService extendKNodeDesignService;

    @Resource
    private IExtendKAppendFormDataService iExtendKAppendFormDataService;

    @Resource
    private IExtendKNodeFileService extendKNodeFileService;

    @Resource
    private IExtendModelFormService extendModelFormService;

    @Override
    public IPage<TaskVo> getMyAllTaskList(Page<TaskVo> page, TaskDTO task) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> roles = sysBaseAPI.getRolesByUsername(sysUser.getUsername());
        task.setCandidate(sysUser.getUsername());
        task.setRoleList(roles);
        task.setAssignee(sysUser.getUsername());


/*        List<Task> taskQuery = taskService
                .createTaskQuery()
                .taskCandidateOrAssigned(sysUser.getUsername())
                .taskCandidateGroupIn(roles)
                .includeProcessVariables()
                .active()
                .orderByTaskCreateTime().desc().list();*/
        return getMyTaskList(page, task);
    }

    /**
     * 获取我的任务
     *
     * @param page 分页参数
     * @param task 任务参数
     * @return 数据
     */
    @Override
    public IPage<TaskVo> getMyTaskList(Page<TaskVo> page, TaskDTO task) {
        return taskMapper.getMyTaskList(page, task);
    }

    /**
     * 撤销流程实例
     *
     * @param processInstanceId 实例ID
     */
    @Transactional(rollbackFor = {Exception.class})
    public void revocation(String processInstanceId) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        // 获取当前执行任务节点
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        List<Execution> list = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        Set<Execution> executions = list.stream().filter(execution -> execution.getActivityId() != null).collect(Collectors.toSet());
        ExtendModelFormData data = extendModelFormDataService.getOne(
                new QueryWrapper<ExtendModelFormData>()
                        .lambda()
                        .eq(ExtendModelFormData::getProcessInstanceId, processInstanceId)
        );
        for (Execution execution : executions) {
            // 获取当前执行任务
            Task task = taskService.createTaskQuery().executionId(execution.getId()).singleResult();
            String title = "【追回】" + processInstance.getProcessDefinitionName();
            String content = getInstanceName(data);
            handleResult(task.getId(), processInstanceId, ActivitiConstant.HANDLE_STATUS_YCX, title, task.getTaskDefinitionKey(), loginUser.getUsername(), execution.getId());

            BusMessageDTO msg = new BusMessageDTO();
            msg.setFromUser(loginUser.getUsername());
            msg.setToUser(task.getAssignee());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setCategory("2");
            msg.setBusType("bpm");
            msg.setBusId(task.getId());
            sysBaseAPI.sendBusAnnouncement(msg);
            System.out.println(task.getId());
        }

        runtimeService.deleteProcessInstance(processInstanceId, ActivitiConstant.HANDEL_RESULT_CX);
        // 修改用户提交流程状态为已撤回
        updateProcessStatus(ActivitiConstant.HANDLE_STATUS_YCX, processInstanceId);

        if (ObjectUtil.isAllNotEmpty(data.getTableId(), data.getTableName())) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", data.getDataId());
            jsonObject.put("bpmn_status", ActivitiConstant.HANDLE_STATUS_YCX);
            fieldService.editFormData(data.getTableId(), data.getTableName(), jsonObject, true);
        }
    }

    /**
     * 作废流程实例
     *
     * @param processInstanceId 流程实例ID
     */
    @Transactional(rollbackFor = {Exception.class})
    public void cancellation(String processInstanceId) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        // 获取当前执行任务节点
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        List<Execution> list = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        Set<Execution> executions = list.stream().filter(execution -> execution.getActivityId() != null).collect(Collectors.toSet());
        ExtendModelFormData data = extendModelFormDataService.getOne(new QueryWrapper<ExtendModelFormData>()
                .lambda()
                .eq(ExtendModelFormData::getProcessInstanceId, processInstanceId)
        );
        for (Execution execution : executions) {
            // 获取当前执行任务
            Task task = taskService.createTaskQuery().executionId(execution.getId()).singleResult();
            String title = "【作废】" + processInstance.getProcessDefinitionName();
            String content = getInstanceName(data);
            handleResult(task.getId(), processInstanceId, ActivitiConstant.HANDLE_STATUS_YZF, content, task.getTaskDefinitionKey(), sysUser.getUsername(), execution.getId());

            BusMessageDTO msg = new BusMessageDTO();
            msg.setFromUser(sysUser.getUsername());
            msg.setToUser(task.getAssignee());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setCategory("2");
            msg.setBusType("bpm");
            msg.setBusId(task.getId());
            sysBaseAPI.sendBusAnnouncement(msg);
            System.out.println(task.getId());
        }

        runtimeService.deleteProcessInstance(processInstanceId, ActivitiConstant.HANDEL_RESULT_ZF);
        // 修改用户提交流程状态为已作废
        updateProcessStatus(ActivitiConstant.HANDLE_STATUS_YZF, processInstanceId);

        if (ObjectUtil.isAllNotEmpty(data.getTableId(), data.getTableName())) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", data.getDataId());
            jsonObject.put("bpmn_status", ActivitiConstant.HANDLE_STATUS_YZF);
            fieldService.editFormData(data.getTableId(), data.getTableName(), jsonObject, true);
        }
    }


    /**
     * 退回到任意任务节点
     *
     * @param taskId
     * @param nodeId
     * @param record
     * @param variables
     * @param transientVariables
     * @param sysUser
     * @param tableId
     * @param tableName
     */
    @Transactional(rollbackFor = {Exception.class})
    public void rejectTargetNode(String taskId, String nodeId, String record, Map<String, Object> variables, Map<String, Object> transientVariables, LoginUser sysUser, String tableId, String tableName) {
        // 驳回除发起人之外的任务节点
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ExtendModelFormData actReModelFormData = extendModelFormDataService.getOne(
                new QueryWrapper<ExtendModelFormData>()
                        .lambda().eq(ExtendModelFormData::getProcessInstanceId, task.getProcessInstanceId()));

        if (nodeId.equals(ActivitiConstant.PROCESS_CREATOR_KEY)) {
            // 驳回到发起人

            runtimeService.deleteProcessInstance(task.getProcessInstanceId(), record);

            // 修改用户提交流程状态为已退回
            updateProcessStatus(ActivitiConstant.HANDLE_STATUS_YTH, task.getProcessInstanceId());
            if (tableName != null && !"".equals(tableName)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", variables.get("id"));
                jsonObject.put("bpmn_status", ActivitiConstant.HANDLE_STATUS_YTH);
                fieldService.editFormData(tableId, tableName, jsonObject, true);
            }

        } else if (nodeId.equals(ActivitiConstant.PROCESS_PREVIOUS_STEP_KEY)) {
            // 驳回上一步
            getPreOneIncomeNode(taskId);
        } else {
            // 驳回除发起人之外的任务节点

            String currActivitiesId = task.getTaskDefinitionKey();
            String processDefinitionId = task.getProcessDefinitionId();

            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

            List<String> runningNodeIds = new ArrayList<>();

            // 处理驳回
            handleRejectTargetNode(bpmnModel, taskId, variables, currActivitiesId, nodeId);

            // 推送消息
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            // String creator = iSysUserService.getUserByName(processInstance.getStartUserId()).getRealname();
            Task nextTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey(nodeId).singleResult();
            // String content = sysUser.getRealname() + "驳回给您【"+ creator +"】提交的【" + bpmnModel.getMainProcess().getName() + "】申请，待审核！";
            // iSysBaseAPI.sendSysMsg(sysUser.getUsername(), nextTask.getAssignee(), content, content, task.getId(), "url", "/task/MyTaskList");
            String title = "【驳回】" + bpmnModel.getMainProcess().getName();
            String content = getInstanceName(actReModelFormData);
            BusMessageDTO msg = new BusMessageDTO();
            msg.setFromUser(sysUser.getUsername());
            msg.setToUser(nextTask.getAssignee());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setCategory("2");
            msg.setBusType("bpm");
            msg.setBusId(task.getId());
            // msg.setThirdUrl("/approval");
            sysBaseAPI.sendBusAnnouncement(msg);
            System.out.println("taskId:" + taskId);

            // 处理兄弟流程驳回, 实现兄弟流程至结束流程
            handleRejectBrotherProcess(task.getProcessInstanceId(), currActivitiesId, nodeId, bpmnModel, variables, record, runningNodeIds);

            // 修改用户提交流程状态为处理中
            updateProcessStatus(ActivitiConstant.HANDLE_STATUS_HLZ, task.getProcessInstanceId());
        }
    }

    /**
     * 退回到任意节点
     *
     * @param rejectTargetNodeDTO 参数
     */
    @Transactional(rollbackFor = {Exception.class})
    public void rejectTargetNode(RejectTargetNodeDTO rejectTargetNodeDTO) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String taskId = rejectTargetNodeDTO.getTaskId();
        String nodeId = rejectTargetNodeDTO.getNodeId();
        String record = rejectTargetNodeDTO.getReason();

        // 驳回除发起人之外的任务节点
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ExtendModelFormData data = extendModelFormDataService.getOne(
                new QueryWrapper<ExtendModelFormData>()
                        .lambda()
                        .eq(ExtendModelFormData::getProcessInstanceId, task.getProcessInstanceId())
        );

        if (nodeId.equals(ActivitiConstant.PROCESS_CREATOR_KEY)) {
            // 驳回到发起人

            runtimeService.deleteProcessInstance(task.getProcessInstanceId(), record);

            // 修改用户提交流程状态为已退回
            updateProcessStatus(ActivitiConstant.HANDLE_STATUS_YTH, task.getProcessInstanceId());
            if (ObjectUtil.isAllNotEmpty(data.getTableId(), data.getTableName())) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", data.getDataId());
                jsonObject.put("bpmn_status", ActivitiConstant.HANDLE_STATUS_YTH);
                fieldService.editFormData(data.getTableId(), data.getTableName(), jsonObject, true);
            }

        } else if (nodeId.equals(ActivitiConstant.PROCESS_PREVIOUS_STEP_KEY)) {
            // 驳回上一步
            getPreOneIncomeNode(taskId);
        } else {
            // 驳回除发起人之外的任务节点

            String currActivitiId = task.getTaskDefinitionKey();
            String processDefinitionId = task.getProcessDefinitionId();

            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

            List<String> runningNodeIds = new ArrayList<>();

            Map<String, Object> variables = new HashMap<>();
            // 处理驳回
            handleRejectTargetNode(bpmnModel, taskId, variables, currActivitiId, nodeId);

            // 推送消息
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            Task nextTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey(nodeId).singleResult();
            String title = "【驳回】" + bpmnModel.getMainProcess().getName();
            String content = getInstanceName(data);
            BusMessageDTO msg = new BusMessageDTO();
            msg.setFromUser(sysUser.getUsername());
            msg.setToUser(nextTask.getAssignee());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setCategory("2");
            msg.setBusType("bpmn");
            msg.setBusId(task.getId());
            sysBaseAPI.sendBusAnnouncement(msg);
            System.out.println("taskId:" + taskId);

            // 处理兄弟流程驳回, 实现兄弟流程至结束流程
            handleRejectBrotherProcess(task.getProcessInstanceId(), currActivitiId, nodeId, bpmnModel, variables, record, runningNodeIds);

            // 修改用户提交流程状态为处理中
            updateProcessStatus(ActivitiConstant.HANDLE_STATUS_HLZ, task.getProcessInstanceId());
        }
    }

    /**
     * 退回到上一个节点
     *
     * @param taskId 节点ID
     */
    @Transactional(rollbackFor = {Exception.class})
    public void backProcess(String taskId) {
        Task curTask = taskService.createTaskQuery().taskId(taskId).singleResult();

        String processInstanceId = curTask.getProcessInstanceId();
        List<HistoricTaskInstance> htiList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId).orderByHistoricTaskInstanceStartTime().desc().list();

        if (CollectionUtils.isEmpty(htiList) || htiList.size() < 2) {
            return;
        }

        // list里的第二条代表上一个任务
        HistoricTaskInstance lastTask = htiList.get(1);

        // list里的第二条代表当前任务
        HistoricTaskInstance currTask = htiList.get(0);

        // 当前节点的executionId
        String curExecutionId = currTask.getExecutionId();

        // 上个节点的taskId
        String lastTaskId = lastTask.getId();

        // 上个节点的executionId
        String lastExecutionId = lastTask.getExecutionId();

        if (StringUtils.isEmpty(lastTaskId)) {
            log.error("LAST TASK IS NULL");
            return;
        }

        String processDefinitionId = lastTask.getProcessDefinitionId();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        String lastActivityId = null;
        List<HistoricActivityInstance> haiFinishedList = historyService.createHistoricActivityInstanceQuery().executionId(lastExecutionId).finished().list();

        for (HistoricActivityInstance hai : haiFinishedList) {
            if (lastTaskId.equals(hai.getTaskId())) {
                // 得到activityId, 只有historicActivityInstance对象里才有此方法
                lastActivityId = hai.getActivityId();
                break;
            }
        }

        // 得到上个节点的信息
        FlowNode lastFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(lastActivityId);

        // 获取当前节点的信息
        Execution execution = runtimeService.createExecutionQuery().executionId(curExecutionId).singleResult();
        String curActivityId = execution.getActivityId();
        FlowNode curFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(curActivityId);

        // 记录当前节点的原始活动时间
        List<SequenceFlow> oriSequenceFlows = new ArrayList<>(curFlowNode.getOutgoingFlows());

        // 清理活动方向
        curFlowNode.getOutgoingFlows().clear();

        // 建立新方向
        List<SequenceFlow> newSequenceFlowList = new ArrayList<>();
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlowId");
        newSequenceFlow.setSourceFlowElement(curFlowNode);
        newSequenceFlow.setTargetFlowElement(lastFlowNode);
        newSequenceFlowList.add(newSequenceFlow);
        curFlowNode.setOutgoingFlows(newSequenceFlowList);

        // 完成任务
        taskService.complete(curTask.getId());

        // 恢复原方向
        curFlowNode.setOutgoingFlows(oriSequenceFlows);

        Task nextTask = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();

        // 设置执行人
        if (nextTask != null) {
            taskService.setAssignee(nextTask.getId(), lastTask.getAssignee());
        }
    }

    /**
     * 分组任务查询
     *
     * @param taskDTO 参数
     * @param page    分页参数
     * @return 数据
     */
    @Override
    public IPage<Map<String, Object>> getGroupTaskList(TaskDTO taskDTO, cn.hutool.db.Page page) {

        List<Map<String, Object>> resultList = new ArrayList<>();
        long total;
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        List<String> roles = sysBaseAPI.getRolesByUsername(sysUser.getUsername());

        TaskQuery taskQuery = taskService.createTaskQuery();
        taskQuery.taskCandidateUser(sysUser.getUsername(), roles);

        List<Task> tasks = taskQuery.listPage(page.getStartPosition(), page.getPageSize());
        total = taskQuery.count();
        tasks = tasks.stream().filter(task -> (task.getAssignee() == null)).collect(Collectors.toList());

        tasks.forEach(task -> {
            Map<String, Object> tempMap = new HashMap<>();

            // 查询表单数据ID
            ExtendModelFormData actReModelFormData = extendModelFormDataService
                    .getOne(new QueryWrapper<ExtendModelFormData>()
                            .lambda()
                            .eq(ExtendModelFormData::getProcessInstanceId, task.getProcessInstanceId()));

            // 发起人
            if (actReModelFormData != null) {
                // 流程名称/流程Key
                Model model = repositoryService.createModelQuery().modelId(actReModelFormData.getModelId()).singleResult();

                if (StringUtils.isEmpty(taskDTO.getModelKey()) && StringUtils.isEmpty(taskDTO.getModelName())) {
                    handelGroupTask(tempMap, task, model, actReModelFormData);

                    resultList.add(tempMap);
                } else if (!StringUtils.isEmpty(taskDTO.getModelKey()) && StringUtils.isEmpty(taskDTO.getModelName()) && model.getKey().contains(taskDTO.getModelKey())) {
                    handelGroupTask(tempMap, task, model, actReModelFormData);

                    resultList.add(tempMap);
                } else if (StringUtils.isEmpty(taskDTO.getModelKey()) && !StringUtils.isEmpty(taskDTO.getModelName()) && model.getName().contains(taskDTO.getModelName())) {
                    handelGroupTask(tempMap, task, model, actReModelFormData);

                    resultList.add(tempMap);
                } else if (!StringUtils.isEmpty(taskDTO.getModelKey()) && !StringUtils.isEmpty(taskDTO.getModelName())
                        && model.getKey().contains(taskDTO.getModelKey())
                        && model.getName().contains(taskDTO.getModelName())) {
                    handelGroupTask(tempMap, task, model, actReModelFormData);

                    resultList.add(tempMap);
                }
            }
        });
        IPage<Map<String, Object>> result = new Page<>();
        result.setRecords(resultList);
        result.setTotal(total);
        return result;
    }

    /**
     * 激活实例
     *
     * @param processInstanceId 实例ID
     */
    @Transactional(rollbackFor = {Exception.class})
    public void activateProcess(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    /**
     * 挂起实例
     *
     * @param processInstanceId 实例ID
     */
    @Transactional(rollbackFor = {Exception.class})
    public void suspendProcess(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }


    /**
     * 查询流程进度
     *
     * @param approvalRecordDTO 参数
     * @param page              分页参数
     * @return 数据
     */
    public IPage<Map<String, Object>> getApprovalRecord(ApprovalRecordDTO approvalRecordDTO, cn.hutool.db.Page page) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        long total = 0;
        List<ExtendModelFormData> dataList = extendModelFormDataService.list(
                new QueryWrapper<ExtendModelFormData>()
                        .lambda()
                        .eq(ExtendModelFormData::getDataId, approvalRecordDTO.getDataId())
                        .orderByDesc(ExtendModelFormData::getCreateTime)
        );
        if (!CollectionUtils.isEmpty(dataList)) {
            ExtendModelFormData actReModelFormData = dataList.get(0);
            List<HistoricProcessInstance> historicProcessInstanceList = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(actReModelFormData.getProcessInstanceId())
                    .orderByProcessInstanceStartTime()
                    .desc()
                    .listPage(page.getStartPosition(), page.getPageSize());

            total = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(actReModelFormData.getProcessInstanceId())
                    .count();
            historicProcessInstanceList.forEach(item -> {
                ProcessDefinition processDefinition = repositoryService
                        .createProcessDefinitionQuery()
                        .processDefinitionId(item.getProcessDefinitionId())
                        .singleResult();
                Map<String, Object> tempMap = new HashMap<>();

                tempMap.put("modelKey", processDefinition.getKey());
                tempMap.put("modelName", processDefinition.getName());
                tempMap.put("processInstanceId", item.getId());
                tempMap.put("startTime", item.getStartTime());
                tempMap.put("endTime", item.getEndTime());
                if (item.getEndTime() != null) {
                    tempMap.put("totalTime", DateUtil.betweenDay(item.getStartTime(), item.getEndTime(), false));
                    tempMap.put("endTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getEndTime()));
                }
                tempMap.put("startTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getStartTime()));

                ExtendHiModelFormData actH = extendHiModelFormDataService.getOne(new QueryWrapper<ExtendHiModelFormData>()
                        .lambda()
                        .eq(ExtendHiModelFormData::getProcessInstanceId, item.getId()));

                tempMap.put("formId", actH == null ? "" : actH.getFormId());
                tempMap.put("formDataId", actH == null ? "" : actH.getId());
                tempMap.put("formData", actH == null ? "" : actH.getFormData());
                tempMap.put("tableId", actH == null ? "" : actH.getTableId());
                tempMap.put("tableName", actH == null ? "" : actH.getTableName());
                tempMap.put("bpmnStatus", actH == null ? "" : actH.getBpmnStatus());
                resultList.add(tempMap);
            });
        }
        IPage<Map<String, Object>> result = new Page<>();
        result.setRecords(resultList);
        result.setTotal(total);
        return result;
    }


    /**
     * 查询我的任务
     *
     * @param createTaskDTO 参数
     * @param page          分页参数
     * @return 数据
     */
    public IPage<Map<String, Object>> getMyCreateTask(CreateTaskDTO createTaskDTO, cn.hutool.db.Page page) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        IPage<Map<String, Object>> result = new Page<>();
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        String processInstanceId = createTaskDTO.getProcessInstanceId();
        String active = createTaskDTO.getActive();

        if (StringUtils.isNotBlank(processInstanceId)) {
            query.processInstanceId(processInstanceId);
        }
        if (StringUtils.isNotBlank(active)) {
            if ("0".equals(active)) {
                query.finished();
            } else {
                query.unfinished();
            }
        }
        query.startedBy(sysUser.getUsername()).orderByProcessInstanceStartTime().desc();
        long count = query.count();
        result.setTotal(count);
        List<HistoricProcessInstance> historicProcessInstanceList = query.listPage(page.getStartPosition(), page.getPageSize());
        List<Map<String, Object>> resultList = new ArrayList<>();
        historicProcessInstanceList.forEach(item -> {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(item.getProcessDefinitionId()).singleResult();
            ExtendModelFormData actReModelFormData = extendModelFormDataService.getOne(
                    new QueryWrapper<ExtendModelFormData>()
                            .lambda()
                            .eq(ExtendModelFormData::getProcessInstanceId, item.getId())
            );
            ExtendModelForm modelForm = extendModelFormService.getOne(new QueryWrapper<ExtendModelForm>()
                    .lambda()
                    .eq(ExtendModelForm::getModelId,
                            actReModelFormData.getModelId()));
            Map<String, Object> tempMap = new HashMap<>();

            tempMap.put("modelKey", processDefinition.getKey());
            tempMap.put("modelName", processDefinition.getName());
            tempMap.put("processInstanceId", item.getId());
            tempMap.put("startTime", item.getStartTime());
            if (item.getEndTime() != null) {
                tempMap.put("totalTime", DateUtil.formatBetween(item.getStartTime(), item.getEndTime()));
                tempMap.put("endTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getEndTime()));
            }

            ExtendHiModelFormData actH = extendHiModelFormDataService.getOne(new QueryWrapper<ExtendHiModelFormData>()
                    .lambda()
                    .eq(ExtendHiModelFormData::getProcessInstanceId, item.getId()));

            if (modelForm.getFormCategory().equals(1)) {
                tempMap.put("htmlJson", modelForm.getHtmlJson());
            }
            if (modelForm.getFormCategory().equals(2)) {
                tempMap.put("dataId", actH.getDataId());
                tempMap.put("tableId", actH.getTableId());
                tempMap.put("tableName", actH.getTableName());
            }
            tempMap.put("formCategory",modelForm.getFormCategory());
            tempMap.put("formData", actH.getFormData());
            tempMap.put("startBy", item.getStartUserId());
            ExtendModel modelExtend = extendActModelService.getOne(
                    new QueryWrapper<ExtendModel>()
                            .lambda()
                            .eq(ExtendModel::getModelId, actH.getModelId())
            );
            tempMap.put("printConfId", modelExtend == null ? "" : modelExtend.getPrintConfId());
            // 任务名称
            // tempMap.put("instanceName", sysUser.getRealname() + "发起了【" + processDefinition.getName() + "】申请");
            tempMap.put("instanceName", getInstanceName(actReModelFormData));
            tempMap.put("bpmnStatus_dictText", sysBaseAPI.translateDict("bpmn_status", actH.getBpmnStatus() + ""));
            tempMap.put("bpmnStatus", actH.getBpmnStatus());
            resultList.add(tempMap);
        });
        result.setRecords(resultList);
        return result;
    }

    /**
     * 处理抄送人
     *
     * @param usernames         用户名
     * @param processInstanceId 流程实例ID
     * @param modelId           模型ID
     * @param task              任务
     * @param sysUser           用户
     */
    @Transactional(rollbackFor = {Exception.class})
    public void handleDuplicate(String usernames, String processInstanceId, String modelId, Task task, LoginUser sysUser) {
        if (!StringUtils.isEmpty(usernames)) {
            ProcessDefinition processDefinition = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ExtendModelFormData extendModelFormData = extendModelFormDataService.getOne(
                    new QueryWrapper<ExtendModelFormData>()
                            .lambda()
                            .eq(ExtendModelFormData::getProcessInstanceId, processInstanceId)
            );
            Model model = repositoryService.createModelQuery().modelId(extendModelFormData.getModelId()).singleResult();

            String[] users = usernames.split(",");
            for (String username : users) {
                ExtendKProcessDuplicate actKProcessDuplicate = new ExtendKProcessDuplicate();
                actKProcessDuplicate.setCreateTime(new Date());
                actKProcessDuplicate.setCreateBy(sysUser.getUsername());
                actKProcessDuplicate.setModelId(extendModelFormData.getModelId());
                actKProcessDuplicate.setProcessInstanceId(processInstanceId);
                actKProcessDuplicate.setUserId(sysBaseAPI.getUserByName(username).getId());
                actKProcessDuplicate.setUserName(username);
                actKProcessDuplicate.setTaskId(task.getId());
                actKProcessDuplicate.setTaskName(task.getName());
                actKProcessDuplicate.setStartTime(simpleDateFormat.format(task.getCreateTime()));
                actKProcessDuplicate.setEndTime(simpleDateFormat.format(new Date()));
                actKProcessDuplicate.setTotalTime(DateUtil.formatBetween(DateUtil.betweenMs(task.getCreateTime(), new Date()), BetweenFormatter.Level.SECOND));
                actKProcessDuplicate.setModelName(model.getName());
                actKProcessDuplicate.setModelKey(model.getKey());
                actKProcessDuplicate.setInitiator(extendModelFormData.getCreateBy());
                actKProcessDuplicate.setTitle(getInstanceName(extendModelFormData));
                actKProcessDuplicate.setFormCategory(extendModelFormData.getFormCategory());
                extendKProcessDuplicateService.save(actKProcessDuplicate);

                String title = "【抄送】" + processDefinition.getName();
                String content = getInstanceName(extendModelFormData);
                BusMessageDTO msg = new BusMessageDTO();
                msg.setFromUser(sysUser.getUsername());
                msg.setToUser(username);
                msg.setTitle(title);
                msg.setContent(content);
                msg.setCategory("2");
                msg.setBusType("bpm");
                msg.setBusId(task.getId());
                //msg.setThirdUrl("/approval");
                sysBaseAPI.sendBusAnnouncement(msg);
            }
        }
    }


    /**
     * 获取下一个节点
     *
     * @param taskId 任务ID
     * @return 数据
     */
    public Map<String, Object> getNextTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        FlowElement flowElement = getNextUserFlowElement(task);
        UserTask userTask = (UserTask) flowElement;
        Map<String, Object> nextNodeMap = new HashMap<>();
        nextNodeMap.put("name", userTask.getName());

        List<LoginUser> loginUsers = new ArrayList<>();

        if (userTask.getAssignee() != null) {
            LoginUser sysUser = sysBaseAPI.getUserByName(userTask.getAssignee());
            loginUsers.add(sysUser);
        } else {
            List<String> candidateUsers = userTask.getCandidateUsers();
            List<String> candidateGroups = userTask.getCandidateGroups();

            if (!CollectionUtils.isEmpty(candidateUsers)) {
                candidateUsers.forEach(item -> {
                    LoginUser sysUser = sysBaseAPI.getUserByName(item);
                    loginUsers.add(sysUser);
                });
            }

            if (!CollectionUtils.isEmpty(candidateGroups)) {
/*                candidateGroups.forEach(item -> {
                    List<LoginUser> users = sysBaseAPI.queryUserIdsByRoleds(item);
                    loginUsers.addAll(users);
                });*/

                List<String> ids = sysBaseAPI.queryUserIdsByRoleds(candidateGroups);
                ids.forEach(item -> {
                    LoginUser user = sysBaseAPI.getUserById(item);
                    loginUsers.add(user);
                });
            }
        }

        nextNodeMap.put("users", loginUsers);

        return nextNodeMap;
    }


    /**
     * 根据活动节点和流程定义ID获取该活动节点的组件信息
     *
     * @param processDefinitionId 实例定义ID
     * @param flowElementId       流程节点ID
     * @return 数据
     */
    public FlowNode getFlowNode(String processDefinitionId, String flowElementId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        return (FlowNode) bpmnModel.getMainProcess().getFlowElement(flowElementId);
    }


    /**
     * 根据el表达式取得满足条件的下一个activityId
     *
     * @param executionId        执行ID
     * @param processInstanceId  流程实例ID
     * @param outgoingFlows      流程
     * @param transientVariables 转换数据
     * @return 数据
     */
    public String getNextActivityId(String executionId,
                                    String processInstanceId,
                                    List<SequenceFlow> outgoingFlows, Map<String, Object> transientVariables) {
        String activityId = null;
        // 遍历出线
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            // 取得线上的条件
            String conditionExpression = outgoingFlow.getConditionExpression();
            // 取得所有变量
            Map<String, Object> variables = runtimeService.getVariables(executionId);

            if (!MapUtils.isEmpty(transientVariables)) {
                variables.putAll(transientVariables);
            }

            String variableName = "";
            // 判断网关条件里是否包含变量名
            for (String s : variables.keySet()) {
                if (conditionExpression.contains(s)) {
                    // 找到网关条件里的变量名
                    variableName = s;
                }
            }
            String conditionVal = getVariableValue(variableName, processInstanceId);
            // 判断el表达式是否成立
            if (isCondition(variableName, conditionExpression, conditionVal)) {
                // 取得目标节点
                FlowElement targetFlowElement = outgoingFlow.getTargetFlowElement();
                activityId = targetFlowElement.getId();
            }
        }
        return activityId;
    }

    /**
     * 取得流程变量的值
     *
     * @param variableName      变量名
     * @param processInstanceId 流程实例Id
     * @return
     */
    public String getVariableValue(String variableName, String processInstanceId) {
        Execution execution = runtimeService
                .createExecutionQuery().processInstanceId(processInstanceId).list().get(0);
        Object object = runtimeService.getVariable(execution.getId(), variableName);
        return object == null ? "" : object.toString();
    }

    /**
     * 根据key和value判断el表达式是否通过
     *
     * @param key   el表达式key
     * @param el    el表达式
     * @param value el表达式传入值
     * @return 数据
     */
    public static boolean isCondition(String key, String el, String value) {
        ExpressionFactoryImpl factory = new ExpressionFactoryImpl();
        SimpleContext context = new SimpleContext();
        context.setVariable(key, factory.createValueExpression(value, String.class));
        TreeValueExpression e = factory.createValueExpression(context, el, boolean.class);
        return (Boolean) e.getValue(context);
    }

    /**
     * 获取当前任务节点的下一个任务节点
     *
     * @param task 当前任务节点
     * @return 下个任务节点
     */
    public FlowElement getNextUserFlowElement(Task task) {
        // 取得已提交的任务
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                .taskId(task.getId()).singleResult();

        // 获得流程定义
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(historicTaskInstance.getProcessDefinitionId());

        //获得当前流程的活动ID
        ExecutionQuery executionQuery = runtimeService.createExecutionQuery();
        Execution execution = executionQuery.executionId(historicTaskInstance.getExecutionId()).singleResult();
        String activityId = execution.getActivityId();
        UserTask userTask;
        while (true) {
            //根据活动节点获取当前的组件信息
            FlowNode flowNode = getFlowNode(processDefinition.getId(), activityId);
            //获取该节点之后的流向
            List<SequenceFlow> sequenceFlowListOutGoing = flowNode.getOutgoingFlows();

            // 获取的下个节点不一定是userTask的任务节点，所以要判断是否是任务节点
            if (sequenceFlowListOutGoing.size() > 1) {
                // 如果有1条以上的出线，表示有分支，需要判断分支的条件才能知道走哪个分支
                // 遍历节点的出线得到下个activityId
                activityId = getNextActivityId(execution.getId(),
                        task.getProcessInstanceId(), sequenceFlowListOutGoing, null);
            } else if (sequenceFlowListOutGoing.size() == 1) {
                // 只有1条出线,直接取得下个节点
                SequenceFlow sequenceFlow = sequenceFlowListOutGoing.get(0);
                // 下个节点
                FlowElement flowElement = sequenceFlow.getTargetFlowElement();
                if (flowElement instanceof UserTask) {
                    // 下个节点为UserTask时
                    userTask = (UserTask) flowElement;
                    return userTask;
                } else if (flowElement instanceof ExclusiveGateway) {
                    // 下个节点为排它网关时
                    ExclusiveGateway exclusiveGateway = (ExclusiveGateway) flowElement;
                    List<SequenceFlow> outgoingFlows = exclusiveGateway.getOutgoingFlows();
                    // 遍历网关的出线得到下个activityId
                    activityId = getNextActivityId(execution.getId(), task.getProcessInstanceId(), outgoingFlows, null);
                }

            } else {
                // 没有出线，则表明是结束节点
                return null;
            }
        }

    }

    /**
     * 拾取任务
     *
     * @param taskId 任务ID
     */
    @Transactional(rollbackFor = {Exception.class})
    public void claimTask(String taskId) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //校验该用户有没有拾取任务的资格
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
            if (task.getAssignee() != null) {
                throw new JeecgBootException("该任务已被其他同事拾取");
            } else {
                taskService.claim(taskId, sysUser.getUsername());
            }
        } else {
            throw new JeecgBootException("没有权限拾取该任务");
        }
    }

    /**
     * 审批完成任务
     *
     * @param params 参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(JSONObject params) {
        //任务id
        String taskId = params.getString("taskId");
        //流程实例id
        String processInstanceId = params.getString("processInstanceId");
        //表单数据id
        String formDataId = params.getString("formDataId");
        //抄送人username（多个用英文逗号隔开）
        String usernames = params.getString("usernames");
        //意见评论
        String comment = params.getString("comment");
        //审批类型(0:同意、1:不同意、2:驳回)
        int opinion = params.getInteger("opinion") != null ? params.getInteger("opinion") : 0;
        //附加表单数据Id
        String appendFormDataId = params.getString("appendFormDataId");
        //附加表单Id
        String appendFormId = params.getString("appendFormId");
        //流程模型Id
        String modelId = params.getString("modelId");
        //附加表单数据
        String formData = params.getString("formData");
        //驳回目标任务节点ID
        String nodeId = params.getString("nodeId");
        //上传的附件
        String files = params.getString("files");

        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        // 处理流程表单数据
        ExtendModelFormData modelFormData = extendModelFormDataService.getById(formDataId);
        Map<String, Object> variables = new HashMap<>();
        String tableId = "";
        String tableName = "";
        String recordId = "";
        if (modelFormData != null) {
            tableName = modelFormData.getTableName();
            tableId = modelFormData.getTableId();
            if (StringUtils.isNotBlank(tableId) && StringUtils.isNotBlank(tableName)) {
                try {
                    Map<String, Object> queryManyFormData = onlCgformHeadService.queryManyFormData(modelFormData.getTableId(), modelFormData.getDataId());
                    Map<String, Object> manyFormData = new HashMap<>(queryManyFormData);
                    String data = JSONObject.toJSONString(manyFormData);
                    variables.putAll(JSONObject.parseObject(data, Map.class));

                    recordId = variables.get("id") + "";
                    variables.put("bpmn$tableName", tableName);
                    variables.put("bpmn$tableId", tableId);
                } catch (a e) {
                    throw new JeecgBootException(e.getMessage());
                }
            } else {
                String formData1 = modelFormData.getFormData();
                if (StringUtils.isNotBlank(formData1)) {
                    Map parseObject = JSONObject.parseObject(formData1, Map.class);
                    variables.putAll(parseObject);// JSONObject.toJavaObject(JSONObject.parseObject(modelFormData.getFormData()), Map.class);
                }
            }
        }


        // 查询流程任务
        Task myTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (myTask == null) {
            throw new JeecgBootException("该任务已办理");
        }

        String userId = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(myTask.getProcessInstanceId())
                .singleResult()
                .getStartUserId();
        LoginUser user = sysBaseAPI.getUserByName(userId);
        if (user != null) {
            variables.put("bpmn$user", user);
        }
        // 处理附加表单数据
        if (!StringUtils.isEmpty(formData)) {
            Map<String, Object> tempMap = JSONObject.parseObject(formData, Map.class);
            if (MapUtils.isNotEmpty(tempMap)) {
                for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
                    if (entry.getValue() != null) {
                        try {
                            String date = entry.getValue().toString();
                            DateUtils.parseDate(date, "yyyy-MM-dd");
                            tempMap.put(entry.getKey(), date + "T23:59:59");
                        } catch (ParseException e) {
                            throw new JeecgBootException(e.getMessage());
                        }
                    }
                }
            }
            variables.putAll(tempMap);
        }

        variables.put("opinion", opinion);

        Map<String, Object> transientVariables = taskService.getVariables(taskId);
        transientVariables.put("opinion", opinion);


        // 处理意见
        handleResult(taskId, processInstanceId, opinion, comment, myTask.getTaskDefinitionKey(), loginUser.getUsername(), myTask.getExecutionId());

        // 保存附加表单数据
        saveActKAppendFormData(taskId, loginUser.getUsername(), formData, appendFormId, processInstanceId, modelId, myTask.getTaskDefinitionKey(), appendFormDataId, myTask.getExecutionId());

        // 保存上传的附件
        saveFiles(myTask, processInstanceId, files, loginUser.getUsername());

        // 处理抄送人
        handleDuplicate(usernames, processInstanceId, modelId, myTask, loginUser);


        if (opinion != 2) {
            handleCompleteTask(taskId, processInstanceId, comment, variables, transientVariables, myTask, modelFormData);

            // 更新流程状态
            boolean b = handleProcessStatus(processInstanceId, formDataId, opinion);
            if (b && tableName != null && !"".equals(tableName)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", recordId);
                jsonObject.put("bpmn_status", (opinion == 1 ? ActivitiConstant.HANDLE_STATUS_WTG : ActivitiConstant.HANDLE_STATUS_YWC));
                //isCrazy=true，按提供字段修改，false表示所有字断修改
                fieldService.editFormData(tableId, tableName, jsonObject, true);
            }

        } else if (StringUtils.isEmpty(nodeId)) {
            handleCompleteTask(taskId, processInstanceId, comment, variables, transientVariables, myTask, modelFormData);

            // 更新流程状态
            boolean b = handleProcessStatus(processInstanceId, formDataId, opinion);
            if (b && tableName != null && !"".equals(tableName)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", recordId);
                jsonObject.put("bpmn_status", ActivitiConstant.HANDLE_STATUS_YWC);
                fieldService.editFormData(tableId, tableName, jsonObject, true);
            }
        } else if (StringUtils.isNotEmpty(nodeId)) {
            //设置审核人信息
            Authentication.setAuthenticatedUserId(loginUser.getUsername());
            taskService.addComment(taskId, processInstanceId, StringUtils.defaultIfBlank(comment, ""));

            RejectTargetNodeDTO rejectTargetNodeDTO = new RejectTargetNodeDTO();
            rejectTargetNodeDTO.setNodeId(nodeId);
            rejectTargetNodeDTO.setTaskId(taskId);
            rejectTargetNodeDTO.setReason(StringUtils.defaultIfBlank(comment, ""));
            rejectTargetNode(taskId, nodeId, StringUtils.defaultIfBlank(comment, ""), variables, transientVariables, loginUser, tableId, tableName);
        }

        // todo  解析节点表达式
        assert modelFormData != null;
        extendKNodeDesignService.postProcessFormDataByScript(modelFormData.getModelId(), myTask.getTaskDefinitionKey(), variables);
    }

    /**
     * 获取所有任务
     *
     * @param taskDTO 参数
     * @param page    分页参数
     * @return 数据
     */
    public IPage<HistoricProcessVo> getAllTask(TaskDTO taskDTO, cn.hutool.db.Page page) {

        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        if (!StringUtils.isEmpty(taskDTO.getCreateBy())) {
            query.startedBy(taskDTO.getCreateBy());
        }
        if (!StringUtils.isEmpty(taskDTO.getModelName())) {
            query.processDefinitionName(taskDTO.getModelName());
        }
        if (!StringUtils.isEmpty(taskDTO.getModelKey())) {
            query.processDefinitionKey(taskDTO.getModelKey());
        }
        long count = query.count();

        List<HistoricProcessInstance> historicProcessInstanceList = query
                .orderByProcessInstanceStartTime().desc().listPage(page.getStartPosition(), page.getEndPosition());
        List<HistoricProcessVo> resultList = new ArrayList<>();

        historicProcessInstanceList.forEach(item -> {
            ProcessDefinition processDefinition = repositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionId(item.getProcessDefinitionId())
                    .singleResult();
            ExtendModelFormData actReModelFormData = extendModelFormDataService
                    .getOne(
                            new QueryWrapper<ExtendModelFormData>()
                                    .lambda()
                                    .eq(ExtendModelFormData::getProcessInstanceId, item.getId())
                    );

            HistoricProcessVo historicProcessVo = new HistoricProcessVo();


            historicProcessVo.setModelKey(processDefinition.getKey());
            historicProcessVo.setModelName(processDefinition.getName());
            historicProcessVo.setProcessInstanceId(item.getId());

            if (ObjectUtils.allNotNull(item, item.getStartTime())) {
                historicProcessVo.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getStartTime()));
            }
            if (ObjectUtils.allNotNull(item, item.getEndTime())) {
                historicProcessVo.setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getEndTime()));
            }

            if (ObjectUtils.allNotNull(item, item.getStartTime(), item.getEndTime())) {
                long betweenDay = DateUtil.betweenMs(item.getStartTime(), item.getEndTime());
                String totalTime = DateUtil.formatBetween(betweenDay, BetweenFormatter.Level.MILLISECOND);
                historicProcessVo.setTotalTime(totalTime);
            }


            ExtendHiModelFormData extendHiModelFormData = extendHiModelFormDataService.getOne(new QueryWrapper<ExtendHiModelFormData>()
                    .lambda()
                    .eq(ExtendHiModelFormData::getProcessInstanceId, item.getId()));


            if (extendHiModelFormData != null) {
                LoginUser user = sysBaseAPI.getUserByName(extendHiModelFormData.getCreateBy());
                historicProcessVo.setFormId(extendHiModelFormData.getFormId());
                historicProcessVo.setFormDataId(extendHiModelFormData.getId());
                historicProcessVo.setFormData(extendHiModelFormData.getFormData());
                historicProcessVo.setTableId(extendHiModelFormData.getTableId());
                historicProcessVo.setTaskName(extendHiModelFormData.getTableName());
                historicProcessVo.setCreateBy(user.getRealname());
                historicProcessVo.setBpmnStatus(extendHiModelFormData.getBpmnStatus());
                historicProcessVo.setInstanceName(getInstanceName(actReModelFormData));
            }

            List<Task> tasks = taskService.createTaskQuery().processInstanceId(item.getId()).active().list();

            if (CollectionUtils.isEmpty(tasks)) {
                List<HistoricTaskInstance> taskInstanceList = historyService.createHistoricTaskInstanceQuery()
                        .finished()
                        .processInstanceId(item.getId()).list();

                StringBuilder taskName = new StringBuilder();
                StringBuilder assignee = new StringBuilder();
                StringBuilder assigneeName = new StringBuilder();
                StringBuilder taskId = new StringBuilder();

                int index = 0;
                for (HistoricTaskInstance task : taskInstanceList) {
                    taskName.append(task.getName());
                    assignee.append(task.getAssignee());
                    LoginUser sysUser = sysBaseAPI.getUserByName(task.getAssignee());
                    assigneeName.append((sysUser == null ? "" : sysUser.getRealname()));
                    taskId.append(task.getId()).append(",");
                    if (index < taskInstanceList.size() - 1) {
                        taskName.append(",");
                        assignee.append(",");
                        assigneeName.append(",");
                        taskId.append(",");
                    }
                    index++;
                }


                historicProcessVo.setTaskName(taskName.toString());
                historicProcessVo.setAssignee(assignee.toString());
                historicProcessVo.setAssigneeName(assigneeName.toString());
                historicProcessVo.setTaskId(taskId.toString());
                historicProcessVo.setMulti(1);
            } else {


                StringBuffer taskName = new StringBuffer();
                StringBuffer assignee = new StringBuffer();
                StringBuffer assigneeName = new StringBuffer();
                StringBuffer taskId = new StringBuffer();

                int index = 0;
                for (Task task : tasks) {
                    taskName = taskName.append(task.getName());
                    assignee = assignee.append(task.getAssignee());
                    LoginUser sysUser = sysBaseAPI.getUserByName(task.getAssignee());
                    assigneeName = assigneeName.append((sysUser == null ? "" : sysUser.getRealname()));
                    taskId = taskId.append(task.getId()).append(",");
                    if (index < tasks.size() - 1) {
                        taskName.append(",");
                        assignee.append(",");
                        assigneeName.append(",");
                        taskId.append(",");
                    }
                    index++;
                }


                historicProcessVo.setTaskName(taskName.toString());
                historicProcessVo.setAssignee(assignee.toString());
                historicProcessVo.setAssigneeName(assigneeName.toString());
                historicProcessVo.setTaskId(taskId.toString());
                historicProcessVo.setMulti(1);

            }
            resultList.add(historicProcessVo);
        });
        IPage<HistoricProcessVo> result = new Page<>();
        result.setRecords(resultList);
        result.setTotal(count);
        return result;
    }

    /**
     * 催办流程实例
     * @param procInstId 流程实例ID
     */
    public void urging(String procInstId) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // 获取当前执行任务节点
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId).singleResult();
        List<Execution> list = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
        Set<Execution> executions = list.stream().filter(execution -> execution.getActivityId() != null).collect(Collectors.toSet());
        ExtendModelFormData extendModelFormData = extendModelFormDataService
                .getOne(new QueryWrapper<ExtendModelFormData>()
                        .lambda()
                        .eq(ExtendModelFormData::getProcessInstanceId, procInstId));
        Iterator<Execution> iterator = executions.iterator();
        while (iterator.hasNext()) {
            Execution execution = iterator.next();
            // 获取当前执行任务
            Task task = taskService.createTaskQuery().executionId(execution.getId()).singleResult();
            String title = "【催办】" + processInstance.getProcessDefinitionName();
            String content = getInstanceName(extendModelFormData);
            BusMessageDTO msg = new BusMessageDTO();
            msg.setFromUser(sysUser.getUsername());
            msg.setToUser(task.getAssignee());
            msg.setTitle(title);
            msg.setContent(content);
            msg.setCategory("2");
            msg.setBusType("bpm");
            msg.setBusId(task.getId());
            //msg.setThirdUrl("/approval");
            sysBaseAPI.sendBusAnnouncement(msg);
        }

    }

    /**
     * 完成任务发送消息
     *
     * @param taskId             任务ID
     * @param procInstId         流程实例ID
     * @param comment            审批信息
     * @param variables          变量
     * @param transientVariables 转换变量
     * @param task               任务
     * @param actReModelFormData 流程表单数据
     */

    @Transactional(rollbackFor = Exception.class)
    public void handleCompleteTask(String taskId, String procInstId, String comment, Map<String, Object> variables, Map<String, Object> transientVariables, Task task, ExtendModelFormData actReModelFormData) {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String fromUser = task.getAssignee();
        String executeId = task.getExecutionId();
        String processDefinitionId = task.getProcessDefinitionId();
        Authentication.setAuthenticatedUserId(loginUser.getUsername());
        taskService.addComment(taskId, procInstId, StringUtils.defaultIfBlank(comment, ""));
        //taskService.setVariables(taskId, variables);
        taskService.complete(taskId, variables);
        handleCompleteTaskSendMsg(fromUser, executeId, procInstId, processDefinitionId);
    }

    /**
     * 处理完成任务向下个任务节点发送待办消息
     *
     * @param fromUser            来源用户
     * @param executeId           执行ID
     * @param procInstId          流程实例ID
     * @param processDefinitionId 流程定义ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleCompleteTaskSendMsg(String fromUser, String executeId, String procInstId, String processDefinitionId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId).singleResult();
        ExtendModelFormData actReModelFormData = extendModelFormDataService.getOne(new QueryWrapper<ExtendModelFormData>()
                .lambda()
                .eq(ExtendModelFormData::getProcessInstanceId, procInstId));
        if (processInstance == null) {
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(procInstId).singleResult();
            String toUser = historicProcessInstance.getStartUserId();
            String title = "【完成】" + processDefinition.getName();
            String content = getInstanceName(actReModelFormData);
            BusMessageDTO msg = new BusMessageDTO();
            msg.setFromUser(fromUser);
            msg.setToUser(toUser);
            msg.setTitle(title);
            msg.setContent(content);
            msg.setCategory("2");
            msg.setBusType("bpm");
            msg.setBusId(procInstId);
            //msg.setThirdUrl("/approval");
            sysBaseAPI.sendBusAnnouncement(msg);
        } else {
            List<Task> tasks = taskService.createTaskQuery().executionId(executeId).list();
            tasks.forEach(item -> {
                String toUser = item.getAssignee();
                if (StringUtils.isNotEmpty(toUser)) {
                    String title = "【审核】" + processDefinition.getName();
                    String content = getInstanceName(actReModelFormData);
                    BusMessageDTO msg = new BusMessageDTO();
                    msg.setFromUser(fromUser);
                    msg.setToUser(toUser);
                    msg.setTitle(title);
                    msg.setContent(content);
                    msg.setCategory("2");
                    msg.setBusType("bpm");
                    msg.setBusId(item.getId());
                    //msg.setThirdUrl("/approval");
                    sysBaseAPI.sendBusAnnouncement(msg);
                    System.out.println(item.getId());
                } else {
                    BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
                    String title = "【拾取】" + processDefinition.getName();
                    String content = getInstanceName(actReModelFormData);
                    // 获取流程节点审批人
                    UserTask userTask = (UserTask) model.getMainProcess().getFlowElement(item.getTaskDefinitionKey());
                    List<String> candidateUsers = userTask.getCandidateUsers();
                    if (!CollectionUtils.isEmpty(candidateUsers)) {
                        String userStr = String.join(",", candidateUsers);
                        BusMessageDTO msg = new BusMessageDTO();
                        msg.setFromUser(fromUser);
                        msg.setToUser(userStr);
                        msg.setTitle(title);
                        msg.setContent(content);
                        msg.setCategory("2");
                        msg.setBusType("bpm");
                        msg.setBusId(item.getId());
                        //msg.setThirdUrl("/approval");
                        sysBaseAPI.sendBusAnnouncement(msg);


                    } else {
                        List<String> candidateGroups = userTask.getCandidateGroups();
                        if (!CollectionUtils.isEmpty(candidateGroups)) {
                            List<String> userIds = sysBaseAPI.queryUserIdsByRoleds(candidateGroups);
                            String userStr = String.join(",", userIds);
                            BusMessageDTO msg = new BusMessageDTO();
                            msg.setFromUser(fromUser);
                            msg.setToUser(userStr);
                            msg.setTitle(title);
                            msg.setContent(content);
                            msg.setCategory("2");
                            msg.setBusType("bpm");
                            msg.setBusId(item.getId());
                            //msg.setThirdUrl("/approval");
                            sysBaseAPI.sendBusAnnouncement(msg);
                            System.out.println(item.getId());
                        }
                    }
                }
            });
        }
    }

    /**
     * @param taskId
     * @param username
     * @param formData
     * @param appendFormId
     * @param procInstId
     * @param modelId
     * @param nodeId
     * @param appendFormDataId
     * @param executionId
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveActKAppendFormData(String taskId,
                                       String username,
                                       String formData,
                                       String appendFormId,
                                       String procInstId,
                                       String modelId,
                                       String nodeId,
                                       String appendFormDataId,
                                       String executionId) {
        if (!StringUtils.isEmpty(formData)) {
            ExtendKAppendFormData actKAppendFormData = new ExtendKAppendFormData();
            actKAppendFormData.setUserName(username);
            actKAppendFormData.setFormData(formData);
            actKAppendFormData.setFormId(appendFormId);
            actKAppendFormData.setProcessInstanceId(procInstId);
            actKAppendFormData.setModelId(modelId);
            actKAppendFormData.setNodeId(nodeId);
            actKAppendFormData.setCreateBy(username);
            actKAppendFormData.setCreateTime(new Date());
            actKAppendFormData.setExecutionId(executionId);
            actKAppendFormData.setTaskId(taskId);
            iExtendKAppendFormDataService.save(actKAppendFormData);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveFiles(Task myTask, String processInstanceId, String files, String username) {
        if (!StringUtils.isEmpty(files)) {
            List<ExtendKNodeFile> extendKNodeFiles = new ArrayList<>();

            String[] fileArr = files.split(",");
            for (String file : fileArr) {
                ExtendKNodeFile actKNodeFile = new ExtendKNodeFile();
                actKNodeFile.setFilePath(file);
                actKNodeFile.setProcessInstanceId(processInstanceId);
                actKNodeFile.setNodeId(myTask.getTaskDefinitionKey());
                actKNodeFile.setTaskId(myTask.getId());
                actKNodeFile.setExecutionId(myTask.getExecutionId());
                actKNodeFile.setCreateBy(username);
                actKNodeFile.setCreateTime(new Date());
                extendKNodeFiles.add(actKNodeFile);


                Authentication.setAuthenticatedUserId(username);
                taskService.createAttachment(file.substring(file.lastIndexOf(".") + 1), myTask.getId(), processInstanceId, "附件",
                        "审批附件", file);
            }

            extendKNodeFileService.saveBatch(extendKNodeFiles);
        }
    }

    /**
     * @param processInstanceId
     * @param formDataId
     * @param flag
     * @return
     */
    @Transactional(rollbackFor = {Exception.class})
    public boolean handleProcessStatus(String processInstanceId, String formDataId, int flag) {
        // 判断流程是否结束，如果结束更新流程状态
        ProcessInstanceQuery processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId);
        ProcessInstance singleResult = processInstance.singleResult();
        if (singleResult == null) {
            updateProcessStatus(flag == 1 ? ActivitiConstant.HANDLE_STATUS_WTG : ActivitiConstant.HANDLE_STATUS_YWC, processInstanceId);
            return true;
        }
        return false;
    }

    /**
     * 拾取任务
     *
     * @param taskId
     * @param userId
     */
    @Transactional(rollbackFor = {Exception.class})
    public void setAssignee(String taskId, String userId) {
        taskService.setAssignee(taskId, userId);
    }


    /**
     * 更新流程状态
     *
     * @param status     状态码
     * @param procInstId 流程实例ID
     */
    @Transactional(rollbackFor = {Exception.class})
    public void updateProcessStatus(int status, String procInstId) {
        // 更新实时
        extendModelFormDataService.update(new UpdateWrapper<ExtendModelFormData>()
                .lambda()
                .set(ExtendModelFormData::getBpmnStatus, status)
                .eq(ExtendModelFormData::getProcessInstanceId, procInstId));

        // 更新历史
        extendHiModelFormDataService.update(new UpdateWrapper<ExtendHiModelFormData>()
                .lambda()
                .set(ExtendHiModelFormData::getBpmnStatus, status)
                .eq(ExtendHiModelFormData::getProcessInstanceId, procInstId));
    }


    /**
     * @param taskId
     * @param procInstId
     * @param flag
     * @param comment
     * @param taskDefinitionKey
     * @param username
     * @param executionId
     */
    @Transactional(rollbackFor = {Exception.class})
    public void handleResult(String taskId,
                             String procInstId,
                             int flag,
                             String comment,
                             String taskDefinitionKey,
                             String username,
                             String executionId) {
        ExtendKHandleResult extendKHandleResult = new ExtendKHandleResult();
        extendKHandleResult.setProcessInstanceId(procInstId);
        extendKHandleResult.setComment(comment);
        extendKHandleResult.setNodeId(taskDefinitionKey);
        extendKHandleResult.setTaskId(taskId);
        extendKHandleResult.setCreateBy(username);
        extendKHandleResult.setCreateTime(new Date());
        extendKHandleResult.setExecutionId(executionId);
        if (flag == 0) {
            extendKHandleResult.setResult(ActivitiConstant.HANDEL_RESULT_TY);
        } else if (flag == 1) {
            extendKHandleResult.setResult(ActivitiConstant.HANDEL_RESULT_BTY);
        } else if (flag == 2) {
            extendKHandleResult.setResult(ActivitiConstant.HANDEL_RESULT_BH);
        } else if (flag == 5) {
            extendKHandleResult.setResult(ActivitiConstant.HANDEL_RESULT_CX);
        } else if (flag == 6) {
            extendKHandleResult.setResult(ActivitiConstant.HANDEL_RESULT_ZF);
        }
        extendKHandleResultService.save(extendKHandleResult);
    }

    /**
     * @param taskId
     */
    @Transactional(rollbackFor = {Exception.class})
    public void getPreOneIncomeNode(String taskId) {
        List<Map<String, String>> incomeNodes = new ArrayList<>();

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String currActivityId = task.getTaskDefinitionKey();

        // 获取当前用户任务节点
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        Process process = bpmnModel.getProcesses().get(0);

        getIncomeNodesRecur(currActivityId, incomeNodes, process, false);

        FlowNode currFlow = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currActivityId);
        if (currFlow == null) {
            List<SubProcess> subProcessList = bpmnModel.getMainProcess().findFlowElementsOfType(SubProcess.class, true);
            for (SubProcess subProcess : subProcessList) {
                FlowElement flowElement = subProcess.getFlowElement(currActivityId);

                if (flowElement != null) {
                    currFlow = (FlowNode) flowElement;
                    break;
                }
            }
        }

        // 记录原活动方向
        assert currFlow != null;
        List<SequenceFlow> oriSequenceFlows = new ArrayList<>(currFlow.getOutgoingFlows());

        // 清理活动方向
        currFlow.getOutgoingFlows().clear();

        List<SequenceFlow> newSequenceFlows = Lists.newArrayList();

        for (Map<String, String> item : incomeNodes) {
            String nodeId = item.get("id");

            // 获取目标节点
            FlowNode target = (FlowNode) bpmnModel.getFlowElement(nodeId);

            //如果不是同一个流程（子流程）不能驳回
            if (!(currFlow.getParentContainer().equals(target.getParentContainer()))) {
                continue;
            }

            // 建立新方向
            SequenceFlow newSequenceFlow = new SequenceFlow();
            String uuid = UUID.randomUUID().toString().replace("-", "");
            newSequenceFlow.setId(uuid);
            newSequenceFlow.setSourceFlowElement(currFlow);// 原节点
            newSequenceFlow.setTargetFlowElement(target);// 目标节点
            newSequenceFlows.add(newSequenceFlow);
        }

        currFlow.setOutgoingFlows(newSequenceFlows);

        // 拒接、通过、驳回指定节点
        taskService.complete(taskId);

        // 修改用户提交流程状态为处理中
        updateProcessStatus(ActivitiConstant.HANDLE_STATUS_HLZ, task.getProcessInstanceId());

        //恢复原方向
        currFlow.setOutgoingFlows(oriSequenceFlows);
    }

    /**
     * 处理兄弟流程至结束任务节点
     *
     * @param proInsId
     * @param currActivitiId
     * @param targetActivitiId
     * @param bpmnModel
     * @param variables
     */
    @Transactional(rollbackFor = {Exception.class})
    public void handleRejectBrotherProcess(String proInsId, String currActivitiId, String targetActivitiId,
                                           BpmnModel bpmnModel, Map<String, Object> variables, String comment, List<String> runningNodeIds) {
        // 处理消息
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(proInsId).singleResult();
        ExtendModelFormData actReModelFormData = extendModelFormDataService.getOne(
                new QueryWrapper<ExtendModelFormData>()
                        .lambda()
                        .eq(ExtendModelFormData::getProcessInstanceId,
                                processInstance.getId())
        );
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        // 组装处理意见
        FlowElement targetFlowElement = bpmnModel.getMainProcess().getFlowElement(targetActivitiId);
        String record = sysUser.getRealname() + "退回至" + targetFlowElement.getName();

        //消息内容
        // String content = sysUser.getRealname() + "将【"+ creator +"】提交的【" + bpmnModel.getMainProcess().getName() + "】申请驳回到" +  targetFlowElement.getName() + "了！";
        String title = "【退回】" + bpmnModel.getMainProcess().getName();
        String content = getInstanceName(actReModelFormData);

        List<Execution> brotherExecutions = getRunningBrotherProcessNode(proInsId, currActivitiId, targetActivitiId, bpmnModel, runningNodeIds);

        String endNodeId = "";
        List<FlowElement> flowElements = (List<FlowElement>) bpmnModel.getMainProcess().getFlowElements();
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof EndEvent) {
                endNodeId = flowElement.getId();
            }
        }
        String finalEndNodeId = endNodeId;
        brotherExecutions.forEach(execution -> {
            String nodeId = execution.getActivityId();
            if (runningNodeIds.contains(nodeId)) {
                Task tempTask = taskService.createTaskQuery().executionId(execution.getId()).singleResult();
                if (tempTask != null) {
                    handleRejectTargetNode(bpmnModel, tempTask.getId(), variables, execution.getActivityId(), finalEndNodeId);

                    // 处理意见
                    handleResult(tempTask.getId(), proInsId, 2, record, tempTask.getTaskDefinitionKey(), sysUser.getUsername(), execution.getId());

                    BusMessageDTO msg = new BusMessageDTO();
                    msg.setFromUser(sysUser.getUsername());
                    msg.setToUser(tempTask.getAssignee());
                    msg.setTitle(title);
                    msg.setContent(content);
                    msg.setCategory("2");
                    msg.setBusType("bpmn");
                    msg.setBusId(tempTask.getId());
                    sysBaseAPI.sendBusAnnouncement(msg);
                    System.out.println(tempTask.getId());
                }
            }
        });
    }

    /**
     * 处理驳回目标节点
     *
     * @param bpmnModel
     * @param taskId
     * @param variables
     * @param currActivitiesId
     * @param targetActivitiesId
     */
    @Transactional(rollbackFor = {Exception.class})
    public void handleRejectTargetNode(BpmnModel bpmnModel, String taskId, Map<String, Object> variables, String currActivitiesId, String targetActivitiesId) {
        FlowNode currFlow = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currActivitiesId);
        if (currFlow == null) {
            List<SubProcess> subProcessList = bpmnModel.getMainProcess().findFlowElementsOfType(SubProcess.class, true);
            for (SubProcess subProcess : subProcessList) {
                FlowElement flowElement = subProcess.getFlowElement(currActivitiesId);

                if (flowElement != null) {
                    currFlow = (FlowNode) flowElement;
                    break;
                }
            }
        }

        // 获取目标节点
        FlowNode target = (FlowNode) bpmnModel.getFlowElement(targetActivitiesId);

        //如果不是同一个流程（子流程）不能驳回
        assert currFlow != null;
        if (!(currFlow.getParentContainer().equals(target.getParentContainer()))) {
            log.error("不是同一个流程（子流程）不能驳回！");
        } else {
            // 记录原活动方向
            List<SequenceFlow> oriSequenceFlows = Lists.newArrayList();
            oriSequenceFlows.addAll(currFlow.getOutgoingFlows());

            // 清理活动方向
            currFlow.getOutgoingFlows().clear();

            // 建立新方向
            List<SequenceFlow> newSequenceFlows = Lists.newArrayList();
            SequenceFlow newSequenceFlow = new SequenceFlow();
            String uuid = UUID.randomUUID().toString().replace("-", "");
            newSequenceFlow.setId(uuid);
            newSequenceFlow.setSourceFlowElement(currFlow);// 原节点
            newSequenceFlow.setTargetFlowElement(target);// 目标节点
            newSequenceFlows.add(newSequenceFlow);
            currFlow.setOutgoingFlows(newSequenceFlows);

            // 拒接、通过、驳回指定节点
            taskService.setVariables(taskId, variables);
            taskService.complete(taskId);

            //恢复原方向
            currFlow.setOutgoingFlows(oriSequenceFlows);
        }
    }

    /**
     * 获取兄弟流程节点
     *
     * @param processInstanceId  流程实例ID
     * @param currActivitiesId   当前任务节点ID
     * @param targetActivitiesId 目标任务节点ID
     * @return
     */
    public List<Execution> getRunningBrotherProcessNode(String processInstanceId, String currActivitiesId, String targetActivitiesId, BpmnModel bpmnModel, List<String> runningNodeIds) {

        // 根据实例ID查询进行中的执行任务，并排除当前任务节点、目标任务节点的执行任务
        List<Execution> executionList = runtimeService.createExecutionQuery()
                .processInstanceId(processInstanceId)
                .list()
                .stream()
                .filter(execution -> !StringUtils.isEmpty(execution.getActivityId()) && !execution.getActivityId().equals(currActivitiesId) && !execution.getActivityId().equals(targetActivitiesId))
                .collect(Collectors.toList());

        // 根据正在执行的任务节点判断是否是目标任务节点的兄弟子流程，如果是流程结束，如果不是流程继续执行
        FlowNode currFlow = (FlowNode) bpmnModel.getMainProcess().getFlowElement(targetActivitiesId);
        List<SequenceFlow> outgoingFlows = currFlow.getOutgoingFlows();

        executionList.forEach(execution -> {
            String nodeId = execution.getActivityId();
            handelBpmn(outgoingFlows, nodeId, bpmnModel, runningNodeIds);
        });

        return executionList;
    }


    /**
     * 获取与驳回任务节点在一条线上运行中的任务节点ID
     *
     * @param outgoingFlows
     * @param endActivityId
     * @param bpmnModel
     * @param runningNodeIds
     */
    public void handelBpmn(List<SequenceFlow> outgoingFlows, String endActivityId, BpmnModel bpmnModel, List<String> runningNodeIds) {
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            String targetNodeId = outgoingFlow.getTargetRef();
            FlowNode targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(targetNodeId);
            if (targetNodeId.equals(endActivityId)) {
                runningNodeIds.add(targetNodeId);
                break;
            } else {
                handelBpmn(targetFlowNode.getOutgoingFlows(), endActivityId, bpmnModel, runningNodeIds);
            }
        }
    }

    /**
     * @param currentNodeId
     * @param incomeNodes
     * @param process
     * @param isAll
     */
    public void getIncomeNodesRecur(String currentNodeId, List<Map<String, String>> incomeNodes, Process process, boolean isAll) {
        FlowElement currentFlowElement = process.getFlowElement(currentNodeId);
        List<SequenceFlow> incomingFlows = null;
        if (currentFlowElement instanceof UserTask) {
            incomingFlows = ((UserTask) currentFlowElement).getIncomingFlows();
        } else if (currentFlowElement instanceof Gateway) {
            incomingFlows = ((Gateway) currentFlowElement).getIncomingFlows();
        } else if (currentFlowElement instanceof StartEvent) {
            incomingFlows = ((StartEvent) currentFlowElement).getIncomingFlows();
        }
        if (incomingFlows != null && incomingFlows.size() > 0) {
            incomingFlows.forEach(incomingFlow -> {
                // String expression = incomingFlow.getConditionExpression();
                // 出线的上一节点
                String sourceFlowElementID = incomingFlow.getSourceRef();
                // 查询上一节点的信息
                FlowElement preFlowElement = process.getFlowElement(sourceFlowElementID);

                //用户任务
                if (preFlowElement instanceof UserTask) {
                    Map<String, String> tempMap = new HashMap<>();
                    tempMap.put("id", preFlowElement.getId());
                    tempMap.put("name", preFlowElement.getName());
                    incomeNodes.add(tempMap);
                    if (isAll) {
                        getIncomeNodesRecur(preFlowElement.getId(), incomeNodes, process, true);
                    }
                }
                //排他网关
                else if (preFlowElement instanceof ExclusiveGateway) {
                    getIncomeNodesRecur(preFlowElement.getId(), incomeNodes, process, isAll);
                }
                //并行网关
                else if (preFlowElement instanceof ParallelGateway) {
                    getIncomeNodesRecur(preFlowElement.getId(), incomeNodes, process, isAll);
                }
            });
        }
    }

    /**
     * @param dataMap
     * @param task
     * @param model
     * @param actReModelFormData
     */
    private void handelGroupTask(Map<String, Object> dataMap, Task task, Model model, ExtendModelFormData actReModelFormData) {
        dataMap.put("modelName", model.getName());
        dataMap.put("modelKey", model.getKey());

        dataMap.put("formDataId", actReModelFormData == null ? "" : actReModelFormData.getId());

        assert actReModelFormData != null;
        LoginUser user = sysBaseAPI.getUserByName(actReModelFormData.getCreateBy());
        if (user != null) {
            dataMap.put("creator", user.getRealname());
            // 任务名称
            // dataMap.put("instanceName", user.getRealname() + "发起了【" + model.getName() + "】申请");
            dataMap.put("instanceName", getInstanceName(actReModelFormData));
        }

        //任务ID
        dataMap.put("id", task.getId());
        //任务名称
        dataMap.put("name", task.getName());
        // 创建时间
        dataMap.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(task.getCreateTime()));
        // 任务的办理人
        dataMap.put("assignee", task.getAssignee());
        // 流程实例ID
        dataMap.put("processInstanceId", task.getProcessInstanceId());
        // 执行对象ID
        dataMap.put("executionId", task.getExecutionId());
        // 流程定义ID
        dataMap.put("processDefinitionId", task.getProcessDefinitionId());

        // 父任务ID
        dataMap.put("parentTaskId", task.getParentTaskId());

        // 获取流程节点附加表单
        ExtendKAppendForm actKAppendForm = extendKAppendFormService.getOne(
                new QueryWrapper<ExtendKAppendForm>()
                        .lambda()
                        .eq(ExtendKAppendForm::getNodeId, task.getTaskDefinitionKey())
        );
        dataMap.put("appendForm", actKAppendForm);

        // 查询节点类型
        ExtendKNode actKNode = extendKNodeService.getOne(new QueryWrapper<ExtendKNode>()
                .lambda()
                .eq(ExtendKNode::getNodeId, task.getTaskDefinitionKey())
                .eq(ExtendKNode::getProcessDefinitionId, task.getProcessDefinitionId()));
        dataMap.put("nodeType", actKNode.getNodeType());
    }

    /**
     * 获取业务标题
     *
     * @param data 数据
     * @return 数据
     */
    private String getInstanceName(ExtendModelFormData data) {
        if (ObjectUtil.isNull(data)) {
            return "无业务标题";
        }
        ExtendModel model = extendActModelService.getOne(
                new QueryWrapper<ExtendModel>()
                        .lambda()
                        .eq(ExtendModel::getModelId, data.getModelId())
        );
        Map<String, Object> variables = new HashMap<>();
        if (data.getTableId() == null) {
            variables.putAll(JSONObject.toJavaObject(JSONObject.parseObject(data.getFormData()), Map.class));
        } else {
            OnlCgformHead head = onlCgformHeadService.getById(data.getTableId());
            variables.putAll(fieldService.queryFormData(data.getTableId(), head.getTableName(), data.getDataId()));
        }
        String expression = model.getTitleExpression();
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
