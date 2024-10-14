package org.jeecg.modules.activiti.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.activiti.entity.*;
import org.jeecg.modules.activiti.mapper.ExtendModelMapper;
import org.jeecg.modules.activiti.service.*;
import org.jeecg.modules.activiti.utils.ActivityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExtendModelServiceImpl extends ServiceImpl<ExtendModelMapper, ExtendModel> implements IExtendModelService {

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private IExtendModelFormService extendModelFormService;

    @Resource
    private IExtendKAppendFormService extendKAppendFormService;

    @Resource
    private IExtendModelFormDeploymentService extendModelFormDeploymentService;

    @Resource
    private IExtendKNodeService extendKNodeService;

    @Resource
    private IExtendKAppendFormDeploymentService extendKAppendFormDeploymentService;

    @Resource
    private IExtendKNodeDesignService extendKNodeDesignService;


    @Transactional(rollbackFor = {Exception.class})
    public boolean add(ExtendModel entity) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Model model = repositoryService.newModel();
        String modelName = entity.getModelName();
        String modelKey = entity.getModelKey();
        String description = entity.getDescription();
        String tenantId = oConvertUtils.getString(TenantContext.getTenant(), CommonConstant.TENANT_ID_DEFAULT_VALUE + "");

        model.setName(modelName);
        model.setKey(modelKey);
        model.setMetaInfo(getObjectNode(entity));
        model.setTenantId(tenantId);
        repositoryService.saveModel(model);

        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace",
                "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.replace("stencilset", stencilSetNode);

        ObjectNode propertiesNode = objectMapper.createObjectNode();
        propertiesNode.put("process_id", modelKey);                                 //流程唯一标识
        propertiesNode.put("process_author", sysUser.getUsername());                        //流程作者
        propertiesNode.put("name", modelName);
        editorNode.set("properties", propertiesNode);


        BpmnModel bpmnModel = new BpmnModel();


        org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
        bpmnModel.addProcess(process);
        process.setId(modelKey);
        process.setName(modelName);
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        startEvent.setName("开始");
        process.addFlowElement(startEvent);
        GraphicInfo g1 = new GraphicInfo();
        g1.setWidth(36);
        g1.setHeight(36);
        g1.setX(255);
        g1.setY(255);
        bpmnModel.addGraphicInfo("start", g1);
        BpmnXMLConverter converter = new BpmnXMLConverter();
        byte[] bytes = converter.convertToXML(bpmnModel);

        repositoryService.addModelEditorSource(model.getId(), bytes);

        entity.setModelId(model.getId());
        entity.setModelMetaInfo(model.getMetaInfo());
        entity.setModelCategory(model.getCategory());
        entity.setModelDeploymentId(model.getDeploymentId());
        entity.setModelTenantId(tenantId);
        entity.setModelVersion(model.getVersion());
        entity.setDescription(description);
        return super.save(entity);
    }


    private String getObjectNode(ExtendModel entity) {

        String modelId = entity.getModelId();
        String modelName = entity.getModelName();
        String modelKey = entity.getModelKey();
        String description = entity.getDescription();
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_ID, modelId);
        modelNode.put("modelKey", modelKey);
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, modelName);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);

        return modelNode.toString();
    }

    @Transactional(rollbackFor = {Exception.class})
    public void deploy(ExtendModel extendModel) throws XMLStreamException {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String modelId = extendModel.getModelId();
        ExtendModelForm extendModelForm = extendModelFormService.getOne(
                new QueryWrapper<ExtendModelForm>()
                        .lambda()
                        .eq(ExtendModelForm::getModelId, modelId));

        ExtendModel modelExtend = this.getOne(new QueryWrapper<ExtendModel>()
                .lambda()
                .eq(ExtendModel::getModelId, modelId));

        if (ObjectUtils.anyNull(extendModelForm, modelExtend)) {
            throw new JeecgBootException("模型与表单数据不能为空");
        }


        // 获取AppendForm表单
        List<ExtendKAppendForm> extendKAppendFormList = extendKAppendFormService.list(
                new QueryWrapper<ExtendKAppendForm>()
                        .lambda()
                        .eq(ExtendKAppendForm::getModelId, modelId));

        // 获取模型
        Model model = repositoryService.getModel(modelId);
        byte[] bytes = repositoryService.getModelEditorSource(model.getId());
        if (ObjectUtils.isEmpty(bytes)) {
            throw new JeecgBootException("模型数据为空，请先成功设计流程并保存");
        }

        InputStream inputStream = new ByteArrayInputStream(bytes);
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(reader);

        if (bpmnModel.getProcesses().size() == 0) {
            throw new JeecgBootException("模型不符要求，请至少设计一条主线流程");
        }


        // 校验流程图
        ActivityUtils.validateFlowModel(bpmnModel);

        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        String tenantId = oConvertUtils.getString(TenantContext.getTenant(), CommonConstant.TENANT_ID_DEFAULT_VALUE + "");
        // 部署发布模型流程
        String processName = model.getKey() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .addString(processName, new String(bpmnBytes, StandardCharsets.UTF_8))
                .tenantId(tenantId)
                .category(extendModel.getModelCategory())
                .name(model.getName())
                .key(model.getKey())
                .deploy();


        // 获取查询器
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> list = processDefinitionQuery.deploymentId(deployment.getId()).list();
        for (ProcessDefinition definition : list) {

            ExtendModelFormDeployment extendModelFormDeployment = new ExtendModelFormDeployment();
            extendModelFormDeployment.setPrintConfId(modelExtend.getPrintConfId());
            extendModelFormDeployment.setModelId(modelExtend.getModelId());
            extendModelFormDeployment.setModelKey(modelExtend.getModelKey());
            extendModelFormDeployment.setModelName(modelExtend.getModelName());
            extendModelFormDeployment.setBizModule(modelExtend.getBizModule());
            extendModelFormDeployment.setFormCategory(modelExtend.getFormCategory());
            extendModelFormDeployment.setDeploymentId(definition.getDeploymentId());
            extendModelFormDeployment.setVersion(definition.getVersion());
            extendModelFormDeployment.setProcessDefinitionId(definition.getId());

            if (modelExtend.getFormCategory().equals(1)) {
                if (StringUtils.isBlank(extendModelForm.getHtmlJson())) {
                    throw new JeecgBootException("请先设计流程表单");
                }
                extendModelFormDeployment.setHtmlJson(extendModelForm.getHtmlJson());
            }
            if (modelExtend.getFormCategory().equals(2)) {
                extendModelFormDeployment.setTableId(modelExtend.getTableId());
                extendModelFormDeployment.setTableName(modelExtend.getTableName());
                extendModelFormDeployment.setFlowStatusCol(modelExtend.getProcessStatusField());
            }
            extendModelFormDeploymentService.save(extendModelFormDeployment);


            //保存模型deploymentId关联

            Model acModel = repositoryService.getModel(modelId);
            acModel.setDeploymentId(definition.getDeploymentId());
            acModel.setKey(definition.getKey());
            acModel.setName(definition.getName());
            acModel.setMetaInfo(definition.getDescription());
            repositoryService.saveModel(acModel);
            // 保存AppendForm表单快照

            // 当前有效的人工节点
            List<ExtendKNode> userTaskNodeList = extendKNodeService.getUserTaskNodeListByModelId(modelId);

            // 当前有效的人工节点
            if (CollectionUtils.isNotEmpty(userTaskNodeList)) {

                List<String> nodeIdList = userTaskNodeList.stream().map(ExtendKNode::getNodeId).collect(Collectors.toList());

                List<ExtendKAppendForm> newActKAppendFormList = extendKAppendFormList.stream().filter(item -> nodeIdList.contains(item.getNodeId())).collect(Collectors.toList());

                // 添加新设计的流程表单
                if (CollectionUtils.isNotEmpty(newActKAppendFormList)) {
                    for (ExtendKAppendForm extendKAppendForm1 : newActKAppendFormList) {
                        ExtendKAppendFormDeployment extendKAppendFormDeployment = new ExtendKAppendFormDeployment();
                        BeanUtils.copyProperties(extendKAppendForm1, extendKAppendFormDeployment, "createBy", "createTime", "id", "modifyDate", "updateBy", "updateTime");
                        extendKAppendFormDeployment.setCreateBy(sysUser.getId());
                        extendKAppendFormDeployment.setCreateTime(Calendar.getInstance().getTime());
                        extendKAppendFormDeployment.setDeploymentId(definition.getDeploymentId());
                        extendKAppendFormDeployment.setVersion(definition.getVersion());
                        extendKAppendFormDeployment.setProcessDefinitionId(definition.getId());
                        extendKAppendFormDeploymentService.save(extendKAppendFormDeployment);
                    }
                }

            }

            //ExtendActModel actModel = new ExtendActModel();
            //actModel.setProcessDefinitionId(definition.getId());
            this.update(new UpdateWrapper<ExtendModel>()
                    .lambda()
                    .set(ExtendModel::getProcessDefinitionId, definition.getId())
                    .set(ExtendModel::getModelDeploymentId, definition.getDeploymentId())
                    .set(ExtendModel::getDeployStatus, 1)
                    .set(ExtendModel::getDeployTime, new Date())
                    .eq(ExtendModel::getModelId, model.getId())
            );

            this.handelModelNode(modelId, bpmnModel, definition);
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    public void saveFlow(ExtendModel extendModel) {
        if (ObjectUtil.isNull(extendModel)) {
            throw new JeecgBootException("流程设计保存失败，不允许空的设计内容");
        }

        String modelId = extendModel.getModelId();
        String jsonXml = extendModel.getModelXml();
        String svgXml = extendModel.getModelSvg();
        String modelName = extendModel.getModelName();
        String modelKey = extendModel.getModelKey();
        String description = extendModel.getDescription();

        String tenantId = oConvertUtils.getString(TenantContext.getTenant(), CommonConstant.TENANT_ID_DEFAULT_VALUE + "");
        Assert.hasText(modelId, "无法获取模型编码，请核对后处理。");
        Assert.hasText(jsonXml, "无法获取模型JSON配置，请核对后处理。");
        Assert.hasText(svgXml, "无法获取模型SVG-xml配置，请核对后处理。");

        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, modelName);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);

        Model model;

        if (StringUtils.isNotEmpty(modelId)) {
            model = repositoryService.getModel(modelId);
            if (model == null) {
                model = repositoryService.newModel();
            }
        } else {
            model = repositoryService.newModel();
        }
        model.setName(modelName);
        model.setKey(modelKey);
        model.setMetaInfo(modelNode.toString());
        model.setTenantId(tenantId);
        repositoryService.saveModel(model);
        repositoryService.addModelEditorSource(model.getId(), jsonXml.getBytes(StandardCharsets.UTF_8));
        final byte[] result = new Base64().decode(svgXml);
        repositoryService.addModelEditorSourceExtra(model.getId(), result);
        this.update(new UpdateWrapper<ExtendModel>()
                .lambda()
                .set(ExtendModel::getDeployStatus, 0)
                .set(ExtendModel::getModelSvg, extendModel.getModelSvg())
                .set(ExtendModel::getModelXml, extendModel.getModelXml())
                .set(ExtendModel::getModelId, model.getId())
                .eq(ExtendModel::getId, extendModel.getId())
        );

    }

    public String getFlowByModelId(String modelId) throws IOException {

        byte[] bpmnBytes = repositoryService.getModelEditorSource(modelId);
        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        //byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);
        String json_xml = new String(bpmnBytes, StandardCharsets.UTF_8);
        return json_xml;

/*        BpmnModel bpmnModel = new BpmnModel();

        org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
        process.setId("myProcess");
        bpmnModel.addProcess(process);
        //创建任务
        UserTask task = new UserTask();
        task.setId("myTask");
        process.addFlowElement(task);
        //设置任务的图形信息
        GraphicInfo g1 = new GraphicInfo();
        g1.setHeight(100);
        g1.setWidth(200);
        g1.setX(110);
        g1.setY(120);
        bpmnModel.addGraphicInfo("myTask", g1);

        BpmnXMLConverter converter = new BpmnXMLConverter();
        byte[] bytes = converter.convertToXML(bpmnModel);*/

        // return new String(bytes, StandardCharsets.UTF_8);
    }

    @Transactional(rollbackFor = {Exception.class})
    public void handelModelNode(String modelId, BpmnModel model, ProcessDefinition definition) {
        // 获取流程节点审批人
        Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof UserTask) {
                UserTask userTask = (UserTask) flowElement;

                ExtendKNode actKNode = new ExtendKNode();
                actKNode.setNodeId(userTask.getId());
                actKNode.setNodeName(userTask.getName());
                actKNode.setAssignee(userTask.getAssignee());
                actKNode.setCandidateUsers(Joiner.on(',').join(userTask.getCandidateUsers()));
                actKNode.setCandidateGroup(Joiner.on(',').join(userTask.getCandidateGroups()));
                actKNode.setIncoming(Joiner.on(',').join(userTask.getIncomingFlows()));
                actKNode.setOutgoing(Joiner.on(',').join(userTask.getOutgoingFlows()));
                actKNode.setModelId(modelId);
                actKNode.setPriority(userTask.getPriority());
                actKNode.setProcessDefinitionId(definition.getId());

                ExtendKNodeDesign actKNodeDesign = extendKNodeDesignService.getOne(new LambdaQueryWrapper<ExtendKNodeDesign>()
                        .eq(ExtendKNodeDesign::getModelId, actKNode.getModelId())
                        .eq(ExtendKNodeDesign::getNodeId, actKNode.getNodeId()));
                if (ObjectUtil.isNotNull(actKNodeDesign)) {
                    actKNode.setNodeType(actKNodeDesign.getNodeType());
                }
                extendKNodeService.save(actKNode);
            }
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    public boolean edit(ExtendModel entity) {

        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String modelId = entity.getModelId();
        String modelName = entity.getModelName();
        String modelKey = entity.getModelKey();
        String description = entity.getDescription();
        String tenantId = oConvertUtils.getString(TenantContext.getTenant(), CommonConstant.TENANT_ID_DEFAULT_VALUE + "");

        Model model;
        if (StringUtils.isNotEmpty(modelId)) {
            model = repositoryService.getModel(modelId);
            if (model == null) {
                model = repositoryService.newModel();
            }
        } else {
            model = repositoryService.newModel();
        }

        model.setName(modelName);
        model.setKey(modelKey);
        model.setMetaInfo(getObjectNode(entity));
        model.setTenantId(tenantId);
        repositoryService.saveModel(model);

        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace",
                "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.replace("stencilset", stencilSetNode);
        ObjectNode propertiesNode = objectMapper.createObjectNode();
        propertiesNode.put("process_id", modelKey);                                 //流程唯一标识
        propertiesNode.put("process_author", sysUser.getUsername());                        //流程作者
        propertiesNode.put("name", modelName);
        editorNode.set("properties", propertiesNode);


        BpmnModel bpmnModel = new BpmnModel();


        org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
        bpmnModel.addProcess(process);
        process.setId(modelKey);
        process.setName(modelName);
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        startEvent.setName("开始");
        process.addFlowElement(startEvent);
        GraphicInfo g1 = new GraphicInfo();
        g1.setWidth(36);
        g1.setHeight(36);
        g1.setX(255);
        g1.setY(255);
        bpmnModel.addGraphicInfo("start", g1);
        BpmnXMLConverter converter = new BpmnXMLConverter();
        byte[] bytes = converter.convertToXML(bpmnModel);


        repositoryService
                .addModelEditorSource(model.getId(),
                        StringUtils.isBlank(entity.getModelXml()) ? bytes : entity.getModelXml().getBytes(StandardCharsets.UTF_8));


        extendModelFormService
                .lambdaUpdate()
                .set(ExtendModelForm::getModelId, model.getId())
                .eq(ExtendModelForm::getModelId, modelId);

        entity.setModelId(model.getId());
        entity.setModelMetaInfo(model.getMetaInfo());
        entity.setModelCategory(model.getCategory());
        entity.setModelDeploymentId(model.getDeploymentId());
        entity.setModelTenantId(tenantId);
        entity.setModelVersion(model.getVersion());
        entity.setDescription(description);
        entity.setDeployStatus(0);
        return super.updateById(entity);
    }
}
