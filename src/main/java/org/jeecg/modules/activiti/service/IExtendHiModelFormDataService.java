package org.jeecg.modules.activiti.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.activiti.entity.ExtendHiModelFormData;
import org.jeecg.modules.activiti.entity.ExtendModelFormData;

import java.util.List;
import java.util.Map;

public interface IExtendHiModelFormDataService extends IService<ExtendHiModelFormData> {

    /**
     * 将流程表单数据保存到历史
     *
     * @param actReModelFormData
     */
    void saveHiFormData(ExtendModelFormData actReModelFormData, String processInstanceId);

    /**
     * 查询附加表单数据
     *
     * @param taskId
     * @param processInstanceId
     * @return
     */
    List<Map<String, Object>> handleNodeAppendForm(String taskId, String processInstanceId);
}
