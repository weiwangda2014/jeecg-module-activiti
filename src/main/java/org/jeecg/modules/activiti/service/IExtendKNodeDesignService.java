package org.jeecg.modules.activiti.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.activiti.entity.ExtendKNodeDesign;

import java.util.Map;

/**
 *
 */
public interface IExtendKNodeDesignService extends IService<ExtendKNodeDesign> {
    void postProcessFormDataByScript(String modelId, String taskDefinitionKey, Map<String, Object> variables);
}
