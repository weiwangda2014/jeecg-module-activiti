package org.jeecg.modules.activiti.utils;

import org.activiti.bpmn.model.*;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.activiti.constants.ActivitiConstant;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * <p>
 * Activiti工作流工具类
 * </p>
 *
 * @author FRH
 * @version 1.0
 */
public class ActivityUtils {


    /**
     * 流程校验，因流程判断比较复杂，取巧借用事务回滚机制，如果校验失败，则回滚
     *
     * @param bpmnModel 模型
     */
    public static void validateFlowModel(BpmnModel bpmnModel) {

        if (bpmnModel != null) {

            // 流程办理人
            Map<String, List<String>> handlerPersonMap = new HashMap<>();

            Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();
            List<StartEvent> startEvents = new ArrayList<>();
            List<UserTask> userTasks = new ArrayList<>();
            List<EndEvent> endEvents = new ArrayList<>();
            List<SequenceFlow> sequenceFlows = new ArrayList<>();
            StringBuilder nodeError = new StringBuilder();
            boolean isFoundNodeNameNotSet = false;
            for (FlowElement e : flowElements) {
                if (e instanceof StartEvent) {
                    StartEvent startEvent = (StartEvent) e;
                    startEvents.add(startEvent);
                } else if (e instanceof SequenceFlow) {
                    SequenceFlow sequenceFlow = (SequenceFlow) e;
                    sequenceFlows.add(sequenceFlow);
                } else if (e instanceof UserTask) {
                    UserTask userTask = (UserTask) e;
                    userTasks.add(userTask);
                    if (StringUtils.isBlank(userTask.getName())) {
                        isFoundNodeNameNotSet = true;
                    }

                    List<String> assignees = new ArrayList<>();
                    if (userTask.getAssignee() != null) {
                        assignees.add(userTask.getAssignee());
                    }
                    assignees.addAll(userTask.getCandidateUsers());
                    assignees.addAll(userTask.getCandidateGroups());
                    if (assignees.size() != 0) {
                        handlerPersonMap.put(userTask.getId(), assignees);
                    }

                } else if (e instanceof EndEvent) {
                    EndEvent endEvent = (EndEvent) e;
                    endEvents.add(endEvent);
                }
            }

            if (CollectionUtils.isEmpty(startEvents)) {
                nodeError.append(ActivitiConstant.PRCOESS_STARTEVENT_NOT_FOUND).append(ActivitiConstant.SEMICOLON).append(ActivitiConstant.HTML_NEWLINE);
            }

            if (CollectionUtils.isEmpty(userTasks)) {
                nodeError.append(ActivitiConstant.PRCOESS_USERTASK_NOT_FOUND).append(ActivitiConstant.SEMICOLON).append(ActivitiConstant.HTML_NEWLINE);
            }

            if (CollectionUtils.isEmpty(endEvents)) {
                nodeError.append(ActivitiConstant.PRCOESS_ENDEVENT_NOT_FOUND).append(ActivitiConstant.SEMICOLON).append(ActivitiConstant.HTML_NEWLINE);
            }

            if (StringUtils.isNotBlank(nodeError.toString())) {
                throw new JeecgBootException(nodeError.toString());
            }

            if (isFoundNodeNameNotSet) {
                throw new JeecgBootException(ActivitiConstant.PRCOESS_NODE_NAME_NOT_SET);
            }


            if (!CollectionUtils.isEmpty(sequenceFlows)) {
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    if (StringUtils.isBlank(sequenceFlow.getSourceRef()) && StringUtils.isBlank(sequenceFlow.getTargetRef())) {
                        nodeError.append(ActivitiConstant.PRCOESS_SEQUENCEFLOW_RELATE_NODE_NOT_FOUND).append(ActivitiConstant.SEMICOLON).append(ActivitiConstant.HTML_NEWLINE);
                        break;
                    }
                }
            }


            StringBuilder handlerPersonNotFound = new StringBuilder();

            for (StartEvent startEvent : startEvents) {
                List<SequenceFlow> outGoingFlows = startEvent.getOutgoingFlows();
                String nodeName = StringUtils.isBlank(startEvent.getName()) ? ActivitiConstant.ACT_TYPE_START_CN : startEvent.getName();
                if (CollectionUtils.isEmpty(outGoingFlows)) {
                    nodeError.append("【").append(nodeName).append("】").append(ActivitiConstant.PRCOESS_OUTGOING_FLOWS_NOT_FOUND).append(ActivitiConstant.SEMICOLON).append(ActivitiConstant.HTML_NEWLINE);
                }
                //节点之间连线校验
                validateSequenceFlows(nodeError, nodeName, outGoingFlows, ActivitiConstant.SEQUENCE_FLOW_TYPE_TARGET);

                // 判断开始任务办理人
/*                List<String> handlerPersonList = handlerPersonMap.get(startEvent.getId());
                if (CollectionUtils.isEmpty(handlerPersonList)) {
                    handlerPersonNotFound.append("[").append(startEvent.getName()).append("]").append(ActivitiConstant.PRCOESS_HANDLER_PERSON_NOT_FOUND).append(ActivitiConstant.SEMICOLON).append(ActivitiConstant.HTML_NEWLINE);
                }*/

            }

            for (UserTask userTask : userTasks) {
                List<SequenceFlow> incomingFlows = userTask.getIncomingFlows();
                if (CollectionUtils.isEmpty(incomingFlows)) {
                    nodeError.append("【").append(StringUtils.isBlank(userTask.getName()) ? ActivitiConstant.PRCOESS_WIHTOUT_NAME : userTask.getName()).append("】").append(ActivitiConstant.PRCOESS_INCOMING_FLOWS_NOT_FOUND).append(ActivitiConstant.SEMICOLON).append(ActivitiConstant.HTML_NEWLINE);
                }
                //节点之间连线校验
                validateSequenceFlows(nodeError, StringUtils.isBlank(userTask.getName()) ? ActivitiConstant.PRCOESS_WIHTOUT_NAME : userTask.getName(), incomingFlows, ActivitiConstant.SEQUENCE_FLOW_TYPE_SOURCE);

                List<SequenceFlow> outGoingFlows = userTask.getOutgoingFlows();
                if (CollectionUtils.isEmpty(outGoingFlows)) {
                    nodeError.append("【").append(StringUtils.isBlank(userTask.getName()) ? ActivitiConstant.PRCOESS_WIHTOUT_NAME : userTask.getName()).append("】").append(ActivitiConstant.PRCOESS_OUTGOING_FLOWS_NOT_FOUND).append(ActivitiConstant.SEMICOLON).append(ActivitiConstant.HTML_NEWLINE);
                }
                //节点之间连线校验
                validateSequenceFlows(nodeError, StringUtils.isBlank(userTask.getName()) ? ActivitiConstant.PRCOESS_WIHTOUT_NAME : userTask.getName(), outGoingFlows, ActivitiConstant.SEQUENCE_FLOW_TYPE_TARGET);

                List<String> handlerPersonList = handlerPersonMap.get(userTask.getId());
                if (CollectionUtils.isEmpty(handlerPersonList)) {
                    handlerPersonNotFound.append("【").append(userTask.getName()).append("】").append(ActivitiConstant.PRCOESS_HANDLER_PERSON_NOT_FOUND).append(ActivitiConstant.SEMICOLON).append(ActivitiConstant.HTML_NEWLINE);
                }

            }

            for (EndEvent endEvent : endEvents) {
                List<SequenceFlow> incomingFlows = endEvent.getIncomingFlows();
                String nodeName = StringUtils.isBlank(endEvent.getName()) ? ActivitiConstant.ACT_TYPE_END_CN : endEvent.getName();
                if (CollectionUtils.isEmpty(incomingFlows)) {
                    nodeError.append("【").append(nodeName).append("】").append(ActivitiConstant.PRCOESS_INCOMING_FLOWS_NOT_FOUND).append(ActivitiConstant.SEMICOLON).append(ActivitiConstant.HTML_NEWLINE);
                }
                //节点之间连线校验
                validateSequenceFlows(nodeError, nodeName, incomingFlows, ActivitiConstant.SEQUENCE_FLOW_TYPE_SOURCE);
            }

            if (StringUtils.isNotBlank(handlerPersonNotFound.toString())) {
                nodeError.append(handlerPersonNotFound);
            }

            if (StringUtils.isNotBlank(nodeError.toString())) {
                throw new JeecgBootException(nodeError.toString());
            }

        } else {
            throw new JeecgBootException("流程为空");
        }
    }

    /**
     * 节点之间连接线校验
     *
     * @param nodeError     节点错误信息
     * @param nodeName      节点名称
     * @param sequenceFlows 流程
     */
    private static void validateSequenceFlows(StringBuilder nodeError, String nodeName, List<SequenceFlow> sequenceFlows, String sequenceFlowType) {
        if (CollectionUtils.isEmpty(sequenceFlows)) {
            return;
        }
        String tip = null;
        String seqRef = null;
        int conditionCount = sequenceFlows.size();
        for (SequenceFlow sequenceFlow : sequenceFlows) {
            switch (sequenceFlowType) {
                case "SOURCE":
                    seqRef = sequenceFlow.getSourceRef();
                    tip = ActivitiConstant.PRCOESS_INCOMING_FLOWS_NOT_FOUND;
                    break;
                case "TARGET":
                    seqRef = sequenceFlow.getTargetRef();
                    tip = ActivitiConstant.PRCOESS_OUTGOING_FLOWS_NOT_FOUND;
                    break;
                default:
                    break;
            }

            if (StringUtils.isBlank(seqRef)) {
                nodeError.append("【").append(nodeName).append("】").append(tip).append(ActivitiConstant.SEMICOLON).append(ActivitiConstant.HTML_NEWLINE);
                break;
            }

            //存在多个分支时，则进行出口条件校验
            if (sequenceFlows.size() > 1) {
                String conditionExpression = sequenceFlow.getConditionExpression();
                if (StringUtils.isBlank(conditionExpression)) {
                    conditionCount = conditionCount - 1;
                }

            }

        }

        if (conditionCount == 0) {
            nodeError.append("【").append(nodeName).append("】").append(ActivitiConstant.PRCOESS_EXPORT_RULE_NOT_FOUND).append(ActivitiConstant.SEMICOLON).append(ActivitiConstant.HTML_NEWLINE);
        }
    }
}
