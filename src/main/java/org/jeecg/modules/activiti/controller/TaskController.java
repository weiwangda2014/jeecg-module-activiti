package org.jeecg.modules.activiti.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.activiti.entity.DTO.ApprovalRecordDTO;
import org.jeecg.modules.activiti.entity.DTO.CreateTaskDTO;
import org.jeecg.modules.activiti.entity.DTO.RejectTargetNodeDTO;
import org.jeecg.modules.activiti.entity.DTO.TaskDTO;
import org.jeecg.modules.activiti.entity.VO.HistoricProcessVo;
import org.jeecg.modules.activiti.entity.VO.TaskVo;
import org.jeecg.modules.activiti.service.ITaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;


/**
 *
 */
@Api(tags = "流程模型任务")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/activiti/task")
public class TaskController {

    @Resource
    private ITaskService iTaskService;


    @GetMapping("/allTask")
    @ApiOperation(value = "查询所有流程", notes = "查询所有流程")
    public Result<Object> getAllTask(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                     TaskDTO task) {

        cn.hutool.db.Page page = new cn.hutool.db.Page(pageNo - 1, pageSize);
        IPage<HistoricProcessVo> result = iTaskService.getAllTask(task, page);

        return Result.OK(result);
    }


    @ApiOperation(value = "流程模型信息-查询所有流程任务", notes = "流程模型信息-查询所有流程任务")
    @GetMapping(value = "/list")
    public Result<IPage<TaskVo>> queryPageList(TaskDTO task,
                                               @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<TaskVo> page = new Page<>(pageNo, pageSize);
        IPage<TaskVo> taskList = iTaskService.getMyAllTaskList(page, task);
        return Result.OK(taskList);
    }


    @GetMapping("/getApprovalRecord")
    @ApiOperation(value = "查询流程进度", notes = "查询流程进度")
    public Result<Object> getApprovalRecord(ApprovalRecordDTO approvalRecordDTO,
                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        cn.hutool.db.Page page = new cn.hutool.db.Page(pageNo, pageSize);
        IPage<Map<String, Object>> result = iTaskService.getApprovalRecord(approvalRecordDTO, page);
        return Result.OK(result);
    }

    @GetMapping("/getMyCreateTask")
    @ApiOperation(value = "查询我发起的流程", notes = "查询我发起的流程")
    public Result<Object> getMyCreateTask(
            CreateTaskDTO createTaskDTO,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        cn.hutool.db.Page page = new cn.hutool.db.Page(pageNo - 1, pageSize);
        IPage<Map<String, Object>> resultList = iTaskService.getMyCreateTask(createTaskDTO, page);

        return Result.OK(resultList);
    }

    @GetMapping("/suspendProcess")
    @ApiOperation(value = "挂起流程实例", notes = "挂起流程实例")
    public Result<String> suspendProcess(@ApiParam(value = "流程实例Id", name = "流程实例id")
                                         @RequestParam("processInstanceId") String processInstanceId) {
        iTaskService.suspendProcess(processInstanceId);
        return Result.OK("挂起流程实例成功");
    }

    @GetMapping("/urging")
    @ApiOperation(value = "催办流程实例", notes = "催办流程实例")
    public Result<String> urging(@ApiParam("流程实例id") @RequestParam String processInstanceId){
        iTaskService.urging(processInstanceId);
        return Result.OK("催办成功");
    }
    /**
     * 激活流程实例
     *
     * @param processInstanceId 流程实例ID
     * @return 消息
     */
    @GetMapping("/activateProcess")
    @ApiOperation(value = "激活流程实例", notes = "激活流程实例")
    public Result<String> activateProcess(@ApiParam(value = "流程实例Id", name = "流程实例id")
                                          @RequestParam("processInstanceId") String processInstanceId) {
        iTaskService.activateProcess(processInstanceId);
        return Result.OK("激活流程实例成功");
    }

    /**
     * 查询组任务列表
     *
     * @param taskDTO  参数
     * @param pageNo   页码
     * @param pageSize 页数
     * @return 消息
     */
    @GetMapping("/getGroupTaskList")
    @ApiOperation(value = "查询组任务列表", notes = "查询组任务列表")
    public Result<Object> getGroupTaskList(TaskDTO taskDTO,
                                           @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        cn.hutool.db.Page page = new cn.hutool.db.Page(pageNo, pageSize);
        IPage<Map<String, Object>> result = iTaskService.getGroupTaskList(taskDTO, page);
        return Result.OK(result);
    }

    /**
     * 拾取任务
     *
     * @param taskId 节点ID
     * @return 消息
     */
    @PostMapping("/claimTask")
    @ApiOperation(value = "拾取任务", notes = "拾取任务")
    public Result<String> claimTask(@ApiParam("任务id") @RequestParam String taskId) {
        iTaskService.claimTask(taskId);
        return Result.OK("拾取任务成功");
    }


    /**
     * 审批通过
     *
     * @param params 参数
     * @return 消息
     */
    @PostMapping("/completeTask")
    @ApiOperation(value = "任务节点审批通过", notes = "任务节点审批通过")
    public Result<String> completeTask(@RequestBody JSONObject params) {
        iTaskService.completeTask(params);
        return Result.OK("审批通过");
    }


    /**
     * 委托任务
     *
     * @param taskId 节点ID
     * @param userId 用户ID
     * @return 消息
     */
    @GetMapping("/entrustTask")
    @ApiOperation(value = "委托个人任务", notes = "委托个人任务")
    public Result<String> entrustTask(@ApiParam(value = "taskId", name = "任务id")
                                      @RequestParam("taskId") String taskId,
                                      @ApiParam(value = "userId", name = "委托人id")
                                      @RequestParam("userId") String userId) {
        iTaskService.setAssignee(taskId, userId);
        return Result.OK("委托任务成功");
    }

    /**
     * 获取下一个用户任务节点
     *
     * @param taskId 任务节点
     * @return 消息
     */
    @GetMapping("/getNextTaskInfo")
    @ApiOperation(value = "获取下一个用户任务节点", notes = "获取下一个用户任务节点")
    public Result<Object> getNextTaskInfo(@ApiParam(value = "taskId", name = "任务id")
                                          @RequestParam("taskId") String taskId) {
        Map<String, Object> nextNodeMap = iTaskService.getNextTask(taskId);
        return Result.OK(nextNodeMap);
    }

    /**
     * 退回到上一个节点
     *
     * @param taskId 任务节点
     */
    @GetMapping("/backProcess")
    @ApiOperation(value = "退回到上个任务节点", notes = "退回到上个任务节点")
    public Result<String> backProcess(@ApiParam(value = "taskId", name = "当前任务ID")
                                      @RequestParam("taskId") String taskId) {
        iTaskService.backProcess(taskId);
        return Result.ok("退回到上个任务节点成功");
    }


    /**
     * 退回到任意节点
     *
     * @param rejectTargetNodeDTO 参数
     * @return 消息
     */
    @GetMapping("/rejectTargetNode")
    @ApiOperation(value = "退回到任意任务节点", notes = "退回到任意任务节点")
    public Result<Object> rejectTargetNode(RejectTargetNodeDTO rejectTargetNodeDTO) {
        iTaskService.rejectTargetNode(rejectTargetNodeDTO);
        return Result.ok("退回节点成功");
    }

    /**
     * 作废流程实例
     *
     * @param processInstanceId 流程实例ID
     * @return 消息
     */
    @GetMapping("/cancellation")
    @ApiOperation(value = "作废流程实例", notes = "作废流程实例")
    public Result<String> cancellation(@ApiParam("流程实例id")
                                       @RequestParam("processInstanceId") String processInstanceId) {
        iTaskService.cancellation(processInstanceId);
        return Result.OK("作废成功");
    }

    /**
     * 撤销流程实例
     *
     * @param processInstanceId 流程实例ID
     * @return 消息
     */
    @GetMapping("/revocation")
    @ApiOperation(value = "撤销流程实例", notes = "撤销流程实例")
    public Result<String> revocation(@ApiParam(value = "processInstanceId", name = "流程实例id")
                                     @RequestParam String processInstanceId) {
        iTaskService.revocation(processInstanceId);
        return Result.OK("撤销成功");
    }
}




