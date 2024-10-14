package org.jeecg.modules.activiti.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.googlecode.aviator.AviatorEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.message.BusMessageDTO;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.activiti.constants.ActivitiConstant;
import org.jeecg.modules.activiti.entity.ExtendModel;
import org.jeecg.modules.activiti.entity.ExtendModelFormData;
import org.jeecg.modules.activiti.entity.ExtendModelFormDeployment;
import org.jeecg.modules.activiti.entity.DTO.StartInstanceDTO;
import org.jeecg.modules.activiti.entity.VO.ProcessNodeVo;
import org.jeecg.modules.activiti.service.*;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.jeecg.modules.online.cgform.service.IOnlCgformFieldService;
import org.jeecg.modules.online.cgform.service.IOnlCgformHeadService;
import org.jeecg.modules.online.config.exception.BusinessException;
import org.jeecg.modules.online.config.exception.a;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程实例service实现类
 *
 * @author dousw
 * @version 1.0
 */

@Slf4j
@Service
public class ActivityInstanceServiceImpl implements IActivityInstanceService {


    @Resource
    private RepositoryService repositoryService;

    @Resource
    private HistoryService historyService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private IExtendModelFormDeploymentService extendModelFormDeploymentService;

    @Resource
    private ITaskService iTaskService;

    @Resource
    private TaskService taskService;

    @Resource
    private ISysBaseAPI sysBaseAPI;

    @Resource
    private IExtendModelService extendActModelService;

    @Resource
    private IOnlCgformHeadService formHeadService;

    @Resource
    private IOnlCgformFieldService onlFormFieldService;

    @Resource
    private IExtendModelFormDataService extendModelFormDataService;

    @Resource
    private IExtendHiModelFormDataService extendHiModelFormDataService;

    @Override
    public ProcessNodeVo getFirstNode(String processDefinitionId) {
        ProcessNodeVo node = new ProcessNodeVo();

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        List<Process> processes = bpmnModel.getProcesses();
        Collection<FlowElement> elements = processes.get(0).getFlowElements();
        // 流程开始节点
        StartEvent startEvent = null;
        for (FlowElement element : elements) {
            if (element instanceof StartEvent) {
                startEvent = (StartEvent) element;
                break;
            }
        }
        FlowElement e = null;
        // 判断开始后的流向节点
        assert startEvent != null;
        SequenceFlow sequenceFlow = startEvent.getOutgoingFlows().get(0);
        for (FlowElement element : elements) {
            if (element.getId().equals(sequenceFlow.getTargetRef())) {
                if (element instanceof UserTask) {
                    e = element;
                    node.setType(ActivitiConstant.NODE_TYPE_TASK);
                    break;
                } else if (element instanceof ExclusiveGateway) {
                    e = element;
                    node.setType(ActivitiConstant.NODE_TYPE_EG);
                    break;
                } else if (element instanceof ParallelGateway) {
                    e = element;
                    node.setType(ActivitiConstant.NODE_TYPE_PG);
                    break;
                } else {
                    throw new RuntimeException("流程设计错误，开始节点后只能是用户任务节点、排他网关、并行网关");
                }
            }
        }
        // 排他、平行网关直接返回
        if (e instanceof ExclusiveGateway || e instanceof ParallelGateway) {
            return node;
        }
        assert e != null;
        node.setTitle(e.getName());
        return node;
    }


    @Override
    public String getFlowImgByInstanceId(String processInstanceId) throws IOException {
        //  InputStream imageStream;


        if (StringUtils.isBlank(processInstanceId)) {
            throw new JeecgBootException("流程实例Id不能为空");
        }
        // 获取历史流程实例
        HistoricProcessInstance historicProcessInstance = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();

        if (historicProcessInstance == null) {
            throw new JeecgBootException("流程历史记录不存在");
        }

        // 获取流程中已经执行的节点，按照执行先后顺序排序
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceId()
                .asc().list();

        // 高亮已经执行流程节点ID集合
        List<String> highLightedActivities = new ArrayList<>();
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            // 用默认颜色
            highLightedActivities.add(historicActivityInstance.getActivityId());
        }

        List<String> currIds = historicActivityInstances.stream()
                .filter(item -> ObjectUtils.isEmpty(item.getEndTime()))
                .map(HistoricActivityInstance::getActivityId).collect(Collectors.toList());

        BpmnModel bpmnModel = repositoryService
                .getBpmnModel(historicProcessInstance.getProcessDefinitionId());
        // 高亮流程已发生流转的线id集合
        List<String> highLightedFlowIds = getHighLightedFlows(bpmnModel, historicActivityInstances);

        ProcessDiagramGenerator processDiagramGenerator = new DefaultProcessDiagramGenerator();
        InputStream imageStream = processDiagramGenerator.generateDiagram(bpmnModel,
                highLightedActivities,//所有活动过的节点，包括当前在激活状态下的节点
                currIds,//当前为激活状态下的节点
                highLightedFlowIds,//活动过的线
                new ArrayList<>(),
                "宋体",
                "微软雅黑",
                "黑体",
                false, UUIDGenerator.generate());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // 创建一个字节数组作为缓冲区
        int bytesRead;
        while ((bytesRead = imageStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        byte[] bytes = outputStream.toByteArray();

        imageStream.close();
        outputStream.close();

        final String DataURLScheme = "data:image/svg+xml;base64,";
        String ImageBase64 = DataURLScheme + Base64.getEncoder().encodeToString(bytes);
        return ImageBase64;
    }

    /**
     * 获取已经流转的线
     *
     * @param bpmnModel                 模型
     * @param historicActivityInstances 历史活动节点
     * @return 集合
     */
    private static List<String> getHighLightedFlows(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances) {
        // 高亮流程已发生流转的线id集合
        List<String> highLightedFlowIds = new ArrayList<>();
        // 全部活动节点
        List<FlowNode> historicActivityNodes = new ArrayList<>();
        // 已完成的历史活动节点
        List<HistoricActivityInstance> finishedActivityInstances = new ArrayList<>();

        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance.getActivityId(), true);
            historicActivityNodes.add(flowNode);
            if (historicActivityInstance.getEndTime() != null) {
                finishedActivityInstances.add(historicActivityInstance);
            }
        }

        FlowNode currentFlowNode;
        FlowNode targetFlowNode;
        // 遍历已完成的活动实例，从每个实例的outgoingFlows中找到已执行的
        for (HistoricActivityInstance currentActivityInstance : finishedActivityInstances) {
            // 获得当前活动对应的节点信息及outgoingFlows信息
            currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currentActivityInstance.getActivityId(), true);
            List<SequenceFlow> sequenceFlows = currentFlowNode.getOutgoingFlows();


            //遍历outgoingFlows并找到已已流转的 满足如下条件认为已已流转：
            //1.当前节点是并行网关或兼容网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已流转
            //2.当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最早的流转节点视为有效流转
            if ("parallelGateway".equals(currentActivityInstance.getActivityType())
                    || "inclusiveGateway".equals(currentActivityInstance.getActivityType())) {
                // 遍历历史活动节点，找到匹配流程目标节点的
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef(), true);
                    if (historicActivityNodes.contains(targetFlowNode)) {
                        highLightedFlowIds.add(sequenceFlow.getId());
                    }
                }
            } else {
                List<Map<String, Object>> tempMapList = new ArrayList<>();
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
                        if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("highLightedFlowId", sequenceFlow.getId());
                            map.put("highLightedFlowStartTime", historicActivityInstance.getStartTime().getTime());
                            tempMapList.add(map);
                        }
                    }
                }

                if (!CollectionUtils.isEmpty(tempMapList)) {
                    // 遍历匹配的集合，取得开始时间最早的一个
                    long earliestStamp = 0L;
                    String highLightedFlowId = null;
                    for (Map<String, Object> map : tempMapList) {
                        long highLightedFlowStartTime = Long.parseLong(map.get("highLightedFlowStartTime").toString());
                        if (earliestStamp == 0 || earliestStamp == highLightedFlowStartTime) {
                            highLightedFlowId = map.get("highLightedFlowId").toString();
                            earliestStamp = highLightedFlowStartTime;
                        }
                    }

                    highLightedFlowIds.add(highLightedFlowId);
                }

            }

        }
        return highLightedFlowIds;
    }

    /**
     * 获取流程实例XML
     *
     * @param processInstanceId 流程实例ID
     * @return String
     */

    public String getFlowXmlByInstanceId(String processInstanceId) {
        if (StringUtils.isEmpty(processInstanceId)) {
            return null;
        }

        // 查询流程xml
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstanceId);

        BpmnXMLConverter converter = new BpmnXMLConverter();
        //把bpmnModel对象转换成字符
        byte[] bytes = converter.convertToXML(bpmnModel);
        return Arrays.toString(bytes);
    }

    @Transactional(rollbackFor = {Exception.class})
    public void handleProcessDuplicate(ExtendModelFormData extendModelFormData, Map<String, Object> variables, String procInstId, LoginUser sysUser) {
        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(procInstId).list();
        Task task = taskService.createTaskQuery().executionId(executions.get(1).getId()).singleResult();

        ExtendModelFormDeployment extendModelFormDeployment = extendModelFormDeploymentService.getOne(
                new QueryWrapper<ExtendModelFormDeployment>()
                        .lambda()
                        .eq(ExtendModelFormDeployment::getModelId, extendModelFormData.getModelId())
        );
        String htmlJson = extendModelFormDeployment.getHtmlJson();
        JSONObject jsonObject = JSONObject.parseObject(htmlJson);
        if (MapUtils.isNotEmpty(jsonObject)) {
            JSONObject configs = jsonObject.getJSONObject("config");
            if (MapUtils.isNotEmpty(configs)) {
                JSONArray jsonArray = configs.getJSONArray("cclist");
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(jsonArray)) {
                    for (Object o : jsonArray) {     //创建指针，判断是否有元素
                        String formKey = o.toString();
                        if (variables.get(formKey) != null) {
                            String usernames = variables.get(formKey).toString();
                            iTaskService.handleDuplicate(usernames, procInstId, extendModelFormData.getModelId(), task, sysUser);
                        }
                    }
                }
            }
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    public void handleBacklogMsg(ProcessInstance pi, LoginUser sysUser) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(pi.getProcessDefinitionId()).singleResult();
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        ExtendModelFormData actReModelFormData = extendModelFormDataService.getOne(new QueryWrapper<ExtendModelFormData>().eq("process_instance_id", pi.getId()));
        tasks.forEach(task -> {
            String toUser = task.getAssignee();
            if (!StringUtils.isEmpty(toUser)) {
                String title = "【审核】" + processDefinition.getName();
                String content = getInstanceName(actReModelFormData);
                // iSysBaseAPI.sendSysMsg(sysUser.getUsername(),toUser, content, content, task.getId(), "url", "/task/MyTaskList");

                BusMessageDTO msg = new BusMessageDTO();
                msg.setFromUser(sysUser.getUsername());
                msg.setToUser(toUser);
                msg.setTitle(title);
                msg.setContent(content);
                msg.setCategory("2");
                msg.setBusType("bpm");
                msg.setBusId(task.getId());
                //msg.setThirdUrl("/processHandle?id=" + task.getId());
                sysBaseAPI.sendBusAnnouncement(msg);
            }
        });
    }

    @Transactional(rollbackFor = {Exception.class})
    public void startForm(StartInstanceDTO startInstanceDTO) {
        String formDataId = startInstanceDTO.getFormDataId();
        String modelId = startInstanceDTO.getModelId();
        JSONObject data = startInstanceDTO.getData();

/*
        ExtendModelFormData formData = extendModelFormDataService.getById(formDataId);
        if (!formData.getBpmnStatus().equals(0)) {
            throw new JeecgBootException("该任务不能重复提交");
        }*/

        Map<String, Object> variables = data.getInnerMap();

        // 获取当前登录用户
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        // 设置流程发起人
        Authentication.setAuthenticatedUserId(sysUser.getUsername());

        // 启动流程实例
        ExtendModelFormDeployment deployment = extendModelFormDeploymentService
                .getOne(new QueryWrapper<ExtendModelFormDeployment>()
                        .lambda()
                        .eq(ExtendModelFormDeployment::getModelId, modelId)
                        .orderByDesc(ExtendModelFormDeployment::getCreateTime)
                        .last("limit 1"));

        if (ObjectUtils.allNull(deployment)) {
            throw new JeecgBootException("该流程未发布");
        }

        // 启动流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceById(deployment.getProcessDefinitionId(), formDataId, variables);

        // 将流程实例的ID，写入流程表单数据中
        ExtendModelFormData extendModelFormData = new ExtendModelFormData();

        extendModelFormData.setHtmlJson(deployment.getHtmlJson());
        extendModelFormData.setPrintConfId(deployment.getPrintConfId());
        extendModelFormData.setBizModule(deployment.getBizModule());
        extendModelFormData.setModelId(deployment.getModelId());
        extendModelFormData.setModelKey(deployment.getModelKey());
        extendModelFormData.setModelName(deployment.getModelName());
        extendModelFormData.setFormData(JSONObject.toJSONString(variables));
        extendModelFormData.setProcessDefinitionId(deployment.getProcessDefinitionId());
        extendModelFormData.setProcessInstanceId(pi.getId());
        extendModelFormData.setSubmitType(1);
        extendModelFormData.setBpmnStatus(ActivitiConstant.HANDLE_STATUS_HLZ);
        extendModelFormData.setFormCategory(deployment.getFormCategory());
        extendModelFormDataService.save(extendModelFormData);


        // 将流程表单数据保存到历史表
        extendHiModelFormDataService.saveHiFormData(extendModelFormData, pi.getId());

        // 发送待办消息
        handleBacklogMsg(pi, sysUser);

        // 发起流程抄送处理
        handleProcessDuplicate(extendModelFormData, variables, pi.getId(), sysUser);
    }

    @Transactional(rollbackFor = {Exception.class})
    public void startOnline(StartInstanceDTO startInstanceDTO) throws a, BusinessException {

        String modelId = startInstanceDTO.getModelId();
        String tableId = startInstanceDTO.getTableId();
        String dataId = String.valueOf(IdWorker.getId());
        String tableName = startInstanceDTO.getTableName();
        JSONObject data = startInstanceDTO.getData();

        OnlCgformHead head = formHeadService.getById(tableId);
        if (head == null) {
            throw new JeecgBootException("模型实体不存在");
        }

        data.put("bpmn_status", ActivitiConstant.STATUS_DEALING);
        data.put("id", dataId);

        this.formHeadService.saveManyFormData(tableId, data, "");

        Map<String, Object> map = formHeadService.queryManyFormData(tableName, dataId);
        map = JSONObject.parseObject(JSON.toJSONString(map));
        if ("1".equals(map.get("bpmn_status"))) {
            throw new JeecgBootException("该任务不能重复提交");
        }

        // 获取当前登录用户
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // 设置流程发起人
        Authentication.setAuthenticatedUserId(sysUser.getUsername());
        // 启动流程实例
        ExtendModelFormDeployment deployment = extendModelFormDeploymentService
                .getOne(new QueryWrapper<ExtendModelFormDeployment>()
                        .lambda()
                        .eq(ExtendModelFormDeployment::getModelId, modelId)
                        .orderByDesc(ExtendModelFormDeployment::getCreateTime)
                        .last("limit 1"));

        if (ObjectUtils.allNull(deployment)) {
            throw new JeecgBootException("该流程未发布");
        }

        ProcessInstance pi = runtimeService.startProcessInstanceById(deployment.getProcessDefinitionId(), dataId, data);


        ExtendModelFormData extendModelFormData = new ExtendModelFormData();
        extendModelFormData.setHtmlJson(deployment.getHtmlJson());
        extendModelFormData.setPrintConfId(deployment.getPrintConfId());
        extendModelFormData.setBizModule(deployment.getBizModule());
        extendModelFormData.setModelId(deployment.getModelId());
        extendModelFormData.setModelKey(deployment.getModelKey());
        extendModelFormData.setModelName(deployment.getModelName());
        extendModelFormData.setFormData(JSONObject.toJSONString(data));
        extendModelFormData.setProcessDefinitionId(deployment.getProcessDefinitionId());
        extendModelFormData.setProcessInstanceId(pi.getId());
        extendModelFormData.setTableId(tableId);
        extendModelFormData.setTableName(head.getTableName());
        extendModelFormData.setSubmitType(1);
        extendModelFormData.setBpmnStatus(ActivitiConstant.STATUS_DEALING);
        extendModelFormData.setDataId(data.get("id") + "");
        extendModelFormData.setFormCategory(2);
        extendModelFormDataService.save(extendModelFormData);

        Map<String, Object> variables = JSONObject.toJavaObject(JSONObject.parseObject(extendModelFormData.getFormData()), Map.class);

        // 将流程表单数据保存到历史表
        extendHiModelFormDataService.saveHiFormData(extendModelFormData, pi.getId());

        // 发送待办消息
        handleBacklogMsg(pi, sysUser);

        // 发起流程抄送处理
        // iActivitiInstanceService.handleProcessDuplicate(extendModelFormData, variables, pi.getId(), sysUser);
    }

    // 获取业务标题
    private String getInstanceName(ExtendModelFormData actReModelFormData) {
        if (actReModelFormData == null) {
            return "无业务标题";
        }
        ExtendModel extendModel = extendActModelService.getOne(new QueryWrapper<ExtendModel>().eq("model_id", actReModelFormData.getModelId()));
        HashMap<String, Object> variables = new HashMap<>();
        if (actReModelFormData.getTableId() == null) {
            Map javaObject = JSONObject.toJavaObject(JSONObject.parseObject(actReModelFormData.getFormData()), Map.class);
            variables.putAll(javaObject);
        } else {
            OnlCgformHead head = formHeadService.getById(actReModelFormData.getTableId());
            Map<String, Object> objectMap = onlFormFieldService.queryFormData(actReModelFormData.getTableId(), head.getTableName(), actReModelFormData.getDataId());
            variables.putAll(objectMap);
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
