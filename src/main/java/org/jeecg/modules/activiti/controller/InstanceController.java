package org.jeecg.modules.activiti.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.modules.activiti.entity.DTO.StartInstanceDTO;
import org.jeecg.modules.activiti.service.IActivityInstanceService;
import org.jeecg.modules.online.config.exception.BusinessException;
import org.jeecg.modules.online.config.exception.a;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 实例
 */
@RestController
@RequestMapping("/activiti/instance")
@Api(tags = "流程实例管理")
public class InstanceController {

    @Resource
    private IActivityInstanceService activityInstanceService;



    /**
     * 启动表单流程示例
     * @param startInstanceDTO 参数
     * @return 消息
     */
    @PostMapping("/startInstance")
    @ApiOperation(value = "开始流程实例", notes = "开始流程实例")
    @Transactional
    public Result<Object> startInstance(@RequestBody StartInstanceDTO startInstanceDTO) {
        activityInstanceService.startForm(startInstanceDTO);
        return Result.OK("申请成功");
    }


    /**
     * 启动Online表单流程示例
     * @param startInstanceDTO
     * @return
     * @throws a
     * @throws BusinessException
     */
    @PostMapping("/startOnlInstance")
    @ApiOperation(value = "开始流程实例", notes = "开始流程实例")
    public Result<Object> startOnlInstance(@RequestBody StartInstanceDTO startInstanceDTO) throws a, BusinessException {
        activityInstanceService.startOnline(startInstanceDTO);
        return Result.OK("申请成功！");
    }

    /**
     * 获取流程实例跟踪图片展示
     * @param processInstanceId
     * @return
     * @throws IOException
     */
    @GetMapping("/getFlowImgByInstanceId")
    @ApiOperation(value = "获取流程实例跟踪", notes = "获取流程实例跟踪")
    public Result<Object> getFlowImgByInstanceId(@ApiParam(value = "processInstanceId", name = "流程实例ID") String processInstanceId) throws IOException {
        String flowImg = activityInstanceService.getFlowImgByInstanceId(processInstanceId);
        Result<Object> result = new Result<>();
        result.setSuccess(true);
        result.setCode(CommonConstant.SC_OK_200);
        result.setResult(flowImg);
        return result;
    }
}
