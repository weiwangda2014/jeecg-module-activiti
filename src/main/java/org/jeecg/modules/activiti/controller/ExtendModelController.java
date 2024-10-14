package org.jeecg.modules.activiti.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.activiti.entity.ExtendModel;
import org.jeecg.modules.activiti.service.IExtendModelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @Description: 流程模型信息
 * @Author: jeecg-boot
 * @Date: 2023-10-21
 * @Version: V1.0
 */
@Api(tags = "流程模型信息")
@RestController
@RequestMapping("/activiti/extendActModel")
@Slf4j
public class ExtendModelController extends JeecgController<ExtendModel, IExtendModelService> {

    @Resource
    private IExtendModelService extendActModelService;

    /**
     * 分页列表查询
     *
     * @param extendModel
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "流程模型信息-分页列表查询", notes = "流程模型信息-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<ExtendModel>> queryPageList(ExtendModel extendModel,
                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                    HttpServletRequest req) {
        QueryWrapper<ExtendModel> queryWrapper = QueryGenerator.initQueryWrapper(extendModel, req.getParameterMap());
        Page<ExtendModel> page = new Page<>(pageNo, pageSize);
        IPage<ExtendModel> pageList = extendActModelService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param extendModel
     * @return
     */
    @AutoLog(value = "流程模型信息-添加")
    @ApiOperation(value = "流程模型信息-添加", notes = "流程模型信息-添加")
    @RequiresPermissions("activiti:model:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody ExtendModel extendModel) {
        extendActModelService.add(extendModel);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param extendModel
     * @return
     */
    @AutoLog(value = "流程模型信息-编辑")
    @ApiOperation(value = "流程模型信息-编辑", notes = "流程模型信息-编辑")
    @RequiresPermissions("activiti:model:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody ExtendModel extendModel) {
        extendActModelService.edit(extendModel);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "流程模型信息-通过id删除")
    @ApiOperation(value = "流程模型信息-通过id删除", notes = "流程模型信息-通过id删除")
    @RequiresPermissions("activiti:model:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        extendActModelService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "流程模型信息-批量删除")
    @ApiOperation(value = "流程模型信息-批量删除", notes = "流程模型信息-批量删除")
    @RequiresPermissions("activiti:model:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.extendActModelService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "流程模型信息-通过id查询", notes = "流程模型信息-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<ExtendModel> queryById(@RequestParam(name = "id", required = true) String id) {
        ExtendModel extendModel = extendActModelService.getById(id);
        if (extendModel == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(extendModel);
    }

    /**
     * 保存流程设计
     *
     * @return
     */
    @PostMapping("/saveFlow")
    @ApiOperation(value = "流程设计保存", notes = "流程设计保存")
    public Result<String> saveFlow(@RequestBody ExtendModel extendModel) {
        extendActModelService.saveFlow(extendModel);
        return Result.OK("流程设计保存成功");
    }

    /**
     * 根据模型编号获取流程设计
     *
     * @return
     */
    @GetMapping("/getFlowByModelId")
    @ApiOperation(value = "根据流程定义ID查询流程", notes = "根据流程定义ID查询流程")
    public Result<Object> getFlowByModelId(
            @ApiParam(name = "modelId", value = "模型定义ID", required = true) @RequestParam(name = "modelId") String modelId) throws IOException {
        String json_xml = extendActModelService.getFlowByModelId(modelId);
        Result<Object> result = new Result<>();
        result.setSuccess(true);
        result.setCode(CommonConstant.SC_OK_200);
        result.setMessage("流程设计XML");
        result.setResult(json_xml);
        return result;
    }

    /**
     * 发布流程
     *
     * @param extendModel
     * @return
     */
    @AutoLog(value = "流程模型信息-流程定义部署")
    @ApiOperation(value = "流程模型信息-流程定义部署", notes = "流程模型信息-流程定义部署")
    @RequiresPermissions("activiti:model:deploy")
    @PostMapping(value = "/deploy")
    public Result<String> deploy(@RequestBody ExtendModel extendModel) throws XMLStreamException, UnsupportedEncodingException {
        extendActModelService.deploy(extendModel);
        return Result.OK("部署发布成功！");
    }
}
