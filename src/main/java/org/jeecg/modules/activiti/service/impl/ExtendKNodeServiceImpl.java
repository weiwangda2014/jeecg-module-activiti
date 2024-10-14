package org.jeecg.modules.activiti.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.collections4.CollectionUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.activiti.entity.ExtendKNode;
import org.jeecg.modules.activiti.entity.ExtendKNodeDesign;
import org.jeecg.modules.activiti.mapper.ExtendKNodeMapper;
import org.jeecg.modules.activiti.service.IExtendKNodeDesignService;
import org.jeecg.modules.activiti.service.IExtendKNodeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

@Service
public class ExtendKNodeServiceImpl extends ServiceImpl<ExtendKNodeMapper, ExtendKNode> implements IExtendKNodeService {

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private IExtendKNodeDesignService extendKNodeDesignService;

    @Override
    public List<ExtendKNode> getUserTaskNodeListByModelId(String modelId) {
        List<ExtendKNode> actKNodeList = Lists.newArrayList();
        // 获取模型
        Model modelData = this.repositoryService.getModel(modelId);
        byte[] bytes = this.repositoryService.getModelEditorSource(modelData.getId());
        if (bytes == null) {
            throw new JeecgBootException("模型数据为空，请先成功设计流程并保存");
        }
        InputStream in = new ByteArrayInputStream(bytes);
        XMLInputFactory factory = XMLInputFactory.newFactory();
        XMLStreamReader reader;
        try {
            reader = factory.createXMLStreamReader(in);
        } catch (XMLStreamException e) {
            throw new JeecgBootException("读取流程图失败，modelId [ " + modelId + " ]");
        }
        BpmnModel model = new BpmnXMLConverter().convertToBpmnModel(reader);
        // 获取流程节点审批人
        Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
        if (CollectionUtils.isNotEmpty(flowElements)) {
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
                    //actKNode.setProcessDefinitionId(StringUtils.EMPTY);
                    // 设置任务节点类型
                    ExtendKNodeDesign actKNodeDesign = this.extendKNodeDesignService.getOne(new LambdaQueryWrapper<ExtendKNodeDesign>()
                            .eq(ExtendKNodeDesign::getModelId, actKNode.getModelId())
                            .eq(ExtendKNodeDesign::getNodeId, actKNode.getNodeId())
                    );
                    if (ObjectUtil.isNotNull(actKNodeDesign)) {
                        actKNode.setNodeType(actKNodeDesign.getNodeType());
                        actKNode.setIsShowAppendForm(actKNodeDesign.getIsShowAppendForm());
                        actKNode.setNodeScript(actKNodeDesign.getNodeScript());
                    } else {
                        actKNode.setNodeType(0);
                        actKNode.setIsShowAppendForm(0);
                        actKNode.setNodeScript("");
                    }
                    actKNodeList.add(actKNode);
                }
            }
        }
        return actKNodeList;
    }
}
