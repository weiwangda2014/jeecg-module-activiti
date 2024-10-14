package org.jeecg.modules.activiti.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.activiti.entity.ExtendModelFormDeployment;
import org.jeecg.modules.activiti.service.IExtendModelFormDeploymentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @Description: 流程表单部署发布
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Api(tags = "流程表单部署发布")
@RestController
@RequestMapping("/activiti/extendModelFormDeployment")
@Slf4j
public class ExtendModelFormDeploymentController extends JeecgController<ExtendModelFormDeployment, IExtendModelFormDeploymentService> {

    @Resource
    private IExtendModelFormDeploymentService extendModelFormDeploymentService;

    /**
     * 分页列表查询
     *
     * @param extendModelFormDeployment
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "流程表单部署发布-分页列表查询")
    @ApiOperation(value = "流程表单部署发布-分页列表查询", notes = "流程表单部署发布-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<ExtendModelFormDeployment>> queryPageList(ExtendModelFormDeployment extendModelFormDeployment,
                                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                  HttpServletRequest req) {
        QueryWrapper<ExtendModelFormDeployment> queryWrapper = QueryGenerator.initQueryWrapper(extendModelFormDeployment, req.getParameterMap());
        Page<ExtendModelFormDeployment> page = new Page<>(pageNo, pageSize);
        IPage<ExtendModelFormDeployment> pageList = extendModelFormDeploymentService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param extendModelFormDeployment
     * @return
     */
    @AutoLog(value = "流程表单部署发布-添加")
    @ApiOperation(value = "流程表单部署发布-添加", notes = "流程表单部署发布-添加")
    @RequiresPermissions("activiti:extend_model_form_deployment:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody ExtendModelFormDeployment extendModelFormDeployment) {
        extendModelFormDeploymentService.save(extendModelFormDeployment);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param extendModelFormDeployment
     * @return
     */
    @AutoLog(value = "流程表单部署发布-编辑")
    @ApiOperation(value = "流程表单部署发布-编辑", notes = "流程表单部署发布-编辑")
    @RequiresPermissions("activiti:extend_model_form_deployment:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody ExtendModelFormDeployment extendModelFormDeployment) {
        extendModelFormDeploymentService.updateById(extendModelFormDeployment);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "流程表单部署发布-通过id删除")
    @ApiOperation(value = "流程表单部署发布-通过id删除", notes = "流程表单部署发布-通过id删除")
    @RequiresPermissions("activiti:extend_model_form_deployment:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        extendModelFormDeploymentService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "流程表单部署发布-批量删除")
    @ApiOperation(value = "流程表单部署发布-批量删除", notes = "流程表单部署发布-批量删除")
    @RequiresPermissions("activiti:extend_model_form_deployment:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.extendModelFormDeploymentService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "流程表单部署发布-通过id查询")
    @ApiOperation(value = "流程表单部署发布-通过id查询", notes = "流程表单部署发布-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<ExtendModelFormDeployment> queryById(@RequestParam(name = "id") String id) {
        ExtendModelFormDeployment extendModelFormDeployment = extendModelFormDeploymentService.getById(id);
        if (extendModelFormDeployment == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(extendModelFormDeployment);
    }

}
