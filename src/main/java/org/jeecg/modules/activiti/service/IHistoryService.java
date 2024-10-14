package org.jeecg.modules.activiti.service;

import cn.hutool.db.Page;
import org.jeecg.modules.activiti.entity.VO.HistoryTaskVo;
import org.jeecg.modules.activiti.entity.VO.TaskVo;

import java.util.List;
import java.util.Map;

public interface IHistoryService {

    Map<String, Object> doneList(TaskVo taskVo, Page page);

    List<HistoryTaskVo> historicFlow(String processInstanceId, String taskId);

    List<Map<String, Object>> getHistoryNode(String processInstanceId, String taskId);
}
