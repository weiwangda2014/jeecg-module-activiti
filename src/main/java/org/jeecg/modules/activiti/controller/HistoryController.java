package org.jeecg.modules.activiti.controller;


import cn.hutool.db.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.activiti.entity.VO.HistoryTaskVo;
import org.jeecg.modules.activiti.entity.VO.TaskVo;
import org.jeecg.modules.activiti.service.IHistoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "流程模型历史记录")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/activiti/history")
public class HistoryController {

    @Resource
    private IHistoryService iHistoryService;


    /**
     * 已办列表
     */
    @ApiOperation(value = "流程-已办列表", notes = "已办列表")
    @RequestMapping(value = "/doneList", method = RequestMethod.GET)
    public Result<Object> doneList(TaskVo taskVo,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        Page page = new Page(pageNo - 1, pageSize);
        Map<String, Object> result = iHistoryService.doneList(taskVo, page);
        return Result.OK(result);
    }


    @ApiOperation(value = "流程-流程流转历史", notes = "流程流转历史")
    @RequestMapping(value = "/historyFlow", method = RequestMethod.GET)
    public Result<Object> historicFlow(@ApiParam(value = "processInstanceId", name = "流程实例Id")
                                       @RequestParam String processInstanceId,
                                       @ApiParam(value = "taskId", name = "任务Id")
                                       @RequestParam(required = false, defaultValue = "") String taskId) {


        List<HistoryTaskVo> list = iHistoryService.historicFlow(processInstanceId, taskId);
        return Result.OK(list);
    }


    @GetMapping("/getHistoryNode")
    @ApiOperation(value = "获取流程实例已执行节点", notes = "获取流程实例已执行节点")
    public Result<List<Map<String, Object>>> getHistoryNode(@ApiParam("流程实例id") @RequestParam String processInstanceId,
                                                            @ApiParam("任务id") @RequestParam String taskId) {
        List<Map<String, Object>> resultList = iHistoryService.getHistoryNode(processInstanceId, taskId);
        return Result.OK(resultList);
    }

}
