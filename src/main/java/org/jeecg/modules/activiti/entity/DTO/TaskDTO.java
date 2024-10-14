package org.jeecg.modules.activiti.entity.DTO;

import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.activiti.entity.ExtendKAppendFormDeployment;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class TaskDTO implements Serializable {

    private String modelId;
    private String modelName;
    private String modelKey;
    private String tableId;
    private String tableName;
    private String formDataId;
    private String dataId;
    private String formId;
    private String createBy;
    private String instanceName;
    private String taskId;
    private String nodeName;
    private Date createTime;
    private String assignee;
    private String candidate;
    private String processInstanceId;
    private String executionId;
    private String processDefinitionId;
    private String parentTaskId;
    private Integer nodeType;
    private String nodeId;
    @Dict(dicCode = "activiti_biz_module")
    private Integer bizModule;
    private Date formDataCreateTime;
    private String formData;
    private Integer status;

    /**
     * 任务类型：1:普通任务;0:组任务
     */
    @Dict(dicCode = "activiti_task_type")
    private Integer taskType;

    /**
     * 角色组（任务组）
     */
    private List<String> roleList;

    /**
     * 任务发起人查询条件（前台传参）
     */
    private String taskInitiators;

    /**
     * 任务发起人查询条件（后台查询）
     */
    private List<String> taskInitiatorList;

    /**
     * 表单
     */
    private ExtendKAppendFormDeployment extendKAppendFormDeployment;

}
