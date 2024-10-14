package org.jeecg.modules.activiti.entity.DTO;

import lombok.Data;

@Data
public class CreateTaskDTO {

    private String processInstanceId;
    private String active;
}
