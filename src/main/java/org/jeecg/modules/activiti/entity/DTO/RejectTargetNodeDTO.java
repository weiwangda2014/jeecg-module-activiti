package org.jeecg.modules.activiti.entity.DTO;

import lombok.Data;

@Data
public class RejectTargetNodeDTO {

    /**
     * 当前任务ID
     */
    private String taskId;
    /**
     * 目标节点ID
     */
    private String nodeId;
    /**
     * 驳回理由
     */
    private String reason;
}
