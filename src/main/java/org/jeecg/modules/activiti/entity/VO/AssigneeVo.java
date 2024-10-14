package org.jeecg.modules.activiti.entity.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author pmc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssigneeVo {

    private String username;

    private Boolean isExecutor;
}