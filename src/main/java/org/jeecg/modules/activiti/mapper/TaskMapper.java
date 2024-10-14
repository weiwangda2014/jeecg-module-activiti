package org.jeecg.modules.activiti.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.activiti.entity.DTO.TaskDTO;
import org.jeecg.modules.activiti.entity.VO.TaskVo;

public interface TaskMapper {
    IPage<TaskVo> getMyTaskList(Page<TaskVo> page,@Param("taskVo") TaskDTO task);
}
