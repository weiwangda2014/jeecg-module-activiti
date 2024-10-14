package org.jeecg.modules.activiti.service;

import org.activiti.engine.runtime.ProcessInstance;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.activiti.entity.ExtendModelFormData;
import org.jeecg.modules.activiti.entity.DTO.StartInstanceDTO;
import org.jeecg.modules.activiti.entity.VO.ProcessNodeVo;
import org.jeecg.modules.online.config.exception.BusinessException;
import org.jeecg.modules.online.config.exception.a;

import java.io.IOException;
import java.util.Map;

/**
 * 流程实例service
 */
public interface IActivityInstanceService {

    /**
     * 查询首届点
     * @param processDefinitionId
     * @return
     */
    ProcessNodeVo getFirstNode(String processDefinitionId);

    /**
     * 根据流程实例Id,获取实时流程图片
     * @param processInstanceId
     */
    String getFlowImgByInstanceId(String processInstanceId) throws IOException;

    /**
     * 根据流程实例ID，获取实时流程xml
     * @param processInstanceId
     * @return
     */
    String getFlowXmlByInstanceId(String processInstanceId);

    /**
     * 处理发起流程抄送
     * @param actReModelFormData
     */
    void handleProcessDuplicate(ExtendModelFormData actReModelFormData, Map<String, Object> variables, String procInstId, LoginUser sysUser);

    /**
     * 发送待办消息
     * @param pi
     * @param sysUser
     */
    void handleBacklogMsg(ProcessInstance pi, LoginUser sysUser);

    void startForm(StartInstanceDTO startInstanceDTO);

    void startOnline(StartInstanceDTO startInstanceDTO) throws a, BusinessException;
}
