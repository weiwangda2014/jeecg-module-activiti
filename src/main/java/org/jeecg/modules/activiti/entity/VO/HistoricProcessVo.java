package org.jeecg.modules.activiti.entity.VO;

import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;

@Data
public class HistoricProcessVo {

    private String modelKey;
    private String modelName;
    private String processInstanceId;
    private String totalTime;
    private String startTime;
    private String endTime;

    private String formId;
    private String formDataId;
    private String formData;
    private String tableId;
    private String tableName;
    private String createBy;
    private String instanceName;

    @Dict(dicCode = "bpmn_status")
    private Integer bpmnStatus;


    private String taskName;
    private String assignee;
    private String assigneeName;
    private String taskId;
    private Integer multi;
}
