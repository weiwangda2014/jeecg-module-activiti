package org.jeecg.modules.activiti.service;

import org.jeecg.modules.activiti.entity.ExtendKNode;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: extend_k_node
 * @Author: jeecg-boot
 * @Date:   2023-10-22
 * @Version: V1.0
 */
public interface IExtendKNodeService extends IService<ExtendKNode> {

    List<ExtendKNode> getUserTaskNodeListByModelId(String modelId);
}
