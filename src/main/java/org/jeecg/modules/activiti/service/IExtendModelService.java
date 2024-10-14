package org.jeecg.modules.activiti.service;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.repository.ProcessDefinition;
import org.jeecg.modules.activiti.entity.ExtendModel;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @Description: 流程模型信息
 * @Author: jeecg-boot
 * @Date: 2023-10-21
 * @Version: V1.0
 */
public interface IExtendModelService extends IService<ExtendModel> {

    void deploy(ExtendModel extendModel) throws XMLStreamException, UnsupportedEncodingException;

    void saveFlow(ExtendModel extendModel);

    String getFlowByModelId(String modelId) throws IOException;

    void handelModelNode(String modelId, BpmnModel model, ProcessDefinition definition);

    boolean add(ExtendModel extendModel);

    boolean edit(ExtendModel extendModel);
}
