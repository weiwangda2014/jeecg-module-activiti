package org.jeecg.modules.activiti.entity.DTO;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class StartInstanceDTO {

    private String modelId;
    private String formDataId;
    private String tableId;
    private String tableName;
    private JSONObject data;
}
