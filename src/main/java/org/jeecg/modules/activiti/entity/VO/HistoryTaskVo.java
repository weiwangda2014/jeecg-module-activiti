package org.jeecg.modules.activiti.entity.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.activiti.engine.history.HistoricTaskInstance;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.activiti.constants.ActivitiConstant;
import org.jeecg.modules.activiti.entity.ExtendKAppendFormData;
import org.jeecg.modules.activiti.entity.ExtendKAppendFormDeployment;
import org.jeecg.modules.activiti.entity.ExtendKNodeFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author pmc
 */
@Data
public class HistoryTaskVo {

    private String id;

    private String name;

    private String key;

    private String description;

    private String executionId;

    private String assignee;

    private String owner;

    private String procDefId;

    private String procInstId;

    private String applyer;

    private String category;

    private Integer priority;

    private String deleteReason;

    private String comment;

    private Long duration;

    private Long workTime;

    private String outgoing;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dueTime;

    private String processName;

    private String routeName;

    private String businessKey;

    private String tableId;
    private String tableName;

    private List<AssigneeVo> assignees;
    private String title;
    private Integer status;
    private Integer result;
    private String flagTxt;
    private Map dataMap;
    //附加表单
    private ExtendKAppendFormData actKAppendFormData;
    // 附加表单数据
    private ExtendKAppendFormDeployment actKAppendForm;
    // 上传的附件
    private List<ExtendKNodeFile> actKNodeFiles;

    public HistoryTaskVo(HistoricTaskInstance task) {
        this.id = task.getId();
        this.name = task.getName();
        this.key = task.getTaskDefinitionKey();
        this.description = task.getDescription();
        this.executionId = task.getExecutionId();
        this.assignee = task.getAssignee();
        this.owner = task.getOwner();
        this.procDefId = task.getProcessDefinitionId();
        this.procInstId = task.getProcessInstanceId();
        this.priority = task.getPriority();
        this.category = task.getCategory();
        this.deleteReason = getMyDeleteReason(task.getDeleteReason());
        this.duration = task.getDurationInMillis();
        this.workTime = task.getWorkTimeInMillis();
        this.createTime = task.getCreateTime();
        this.startTime = task.getStartTime();
        this.endTime = task.getEndTime();
        this.dueTime = task.getDueDate();
    }

    public String getMyDeleteReason(String deleteReason) {

        if (StringUtils.isBlank(deleteReason)) {
            return "";
        }
        if (ActivitiConstant.PASSED_FLAG.equals(deleteReason)) {
            deleteReason = "审批通过";
        } else if (ActivitiConstant.BACKED_FLAG.equals(deleteReason)) {
            deleteReason = "审批驳回";
        } else if (deleteReason.contains(ActivitiConstant.DELETE_PRE)) {
            String reason = "";
            if (ActivitiConstant.DELETE_PRE.equals(deleteReason)) {
                reason = "未填写";
            } else if (deleteReason.length() > 8) {
                reason = deleteReason.substring(8);
            }
            deleteReason = "删除撤回-原因" + reason;
        } else if (deleteReason.contains(ActivitiConstant.CANCEL_PRE)) {
            String reason = "";
            if (ActivitiConstant.CANCEL_PRE.equals(deleteReason)) {
                reason = "未填写";
            } else if (deleteReason.length() > 9) {
                reason = deleteReason.substring(9);
            }
            deleteReason = "发起人撤回-原因" + reason;
        } else {
            deleteReason = "已删除-原因" + deleteReason;
        }
        return deleteReason;
    }
}
