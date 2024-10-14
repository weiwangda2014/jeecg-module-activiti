package org.jeecg.modules.activiti.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.task.Task;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.activiti.entity.DTO.ApprovalRecordDTO;
import org.jeecg.modules.activiti.entity.DTO.CreateTaskDTO;
import org.jeecg.modules.activiti.entity.DTO.RejectTargetNodeDTO;
import org.jeecg.modules.activiti.entity.DTO.TaskDTO;
import org.jeecg.modules.activiti.entity.VO.HistoricProcessVo;
import org.jeecg.modules.activiti.entity.VO.TaskVo;
import java.util.Map;

/**
 * @program: ruoyi-vue-plus
 * @description: 任务接口
 * @author: gssong
 * @created: 2021/10/17 14:57
 */
public interface ITaskService {


    IPage<TaskVo> getMyAllTaskList(Page<TaskVo> page, TaskDTO task);

    IPage<TaskVo> getMyTaskList(Page<TaskVo> page, TaskDTO task);

    void revocation(String procInstId);

    void cancellation(String processInstanceId);

    void rejectTargetNode(RejectTargetNodeDTO rejectTargetNodeDTO);

    void backProcess(String taskId);

    IPage<Map<String, Object>> getGroupTaskList(TaskDTO taskDTO, cn.hutool.db.Page page);

    void activateProcess(String processInstanceId);

    void suspendProcess(String processInstanceId);

    IPage<Map<String, Object>> getApprovalRecord(ApprovalRecordDTO approvalRecordDTO, cn.hutool.db.Page page);

    IPage<Map<String, Object>> getMyCreateTask(CreateTaskDTO createTaskDTO, cn.hutool.db.Page page);

    void handleDuplicate(String usernames, String procInstId, String modelId, Task task, LoginUser sysUser);

    Map<String, Object> getNextTask(String taskId);

    void setAssignee(String taskId, String userId);

    FlowElement getNextUserFlowElement(Task task);

    void claimTask(String taskId);

    void completeTask(JSONObject params);

    IPage<HistoricProcessVo> getAllTask(TaskDTO task, cn.hutool.db.Page page);

    void urging(String procInstId);
}
