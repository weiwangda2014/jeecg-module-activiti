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
import org.jeecg.modules.activiti.entity.ExtendKAppendFormDeployment;
import org.jeecg.modules.activiti.service.IExtendKAppendFormDeploymentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @Description: extend_k_append_form_deployment
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Api(tags = "extend_k_append_form_deployment")
@RestController
@RequestMapping("/activiti/extendKAppendFormDeployment")
@Slf4j
public class ExtendKAppendFormDeploymentController extends JeecgController<ExtendKAppendFormDeployment, IExtendKAppendFormDeploymentService> {
    @Resource
    private IExtendKAppendFormDeploymentService extendKAppendFormDeploymentService;

    /**
     * 分页列表查询
     *
     * @param extendKAppendFormDeployment
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "extend_k_append_form_deployment-分页列表查询", notes = "extend_k_append_form_deployment-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<ExtendKAppendFormDeployment>> queryPageList(ExtendKAppendFormDeployment extendKAppendFormDeployment,
                                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                    HttpServletRequest req) {
        QueryWrapper<ExtendKAppendFormDeployment> queryWrapper = QueryGenerator.initQueryWrapper(extendKAppendFormDeployment, req.getParameterMap());
        Page<ExtendKAppendFormDeployment> page = new Page<>(pageNo, pageSize);
        IPage<ExtendKAppendFormDeployment> pageList = extendKAppendFormDeploymentService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param extendKAppendFormDeployment
     * @return
     */
    @AutoLog(value = "extend_k_append_form_deployment-添加")
    @ApiOperation(value = "extend_k_append_form_deployment-添加", notes = "extend_k_append_form_deployment-添加")
    @RequiresPermissions("activiti:extend_k_append_form_deployment:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody ExtendKAppendFormDeployment extendKAppendFormDeployment) {
        extendKAppendFormDeploymentService.save(extendKAppendFormDeployment);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param extendKAppendFormDeployment
     * @return
     */
    @AutoLog(value = "extend_k_append_form_deployment-编辑")
    @ApiOperation(value = "extend_k_append_form_deployment-编辑", notes = "extend_k_append_form_deployment-编辑")
    @RequiresPermissions("activiti:extend_k_append_form_deployment:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody ExtendKAppendFormDeployment extendKAppendFormDeployment) {
        extendKAppendFormDeploymentService.updateById(extendKAppendFormDeployment);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "extend_k_append_form_deployment-通过id删除")
    @ApiOperation(value = "extend_k_append_form_deployment-通过id删除", notes = "extend_k_append_form_deployment-通过id删除")
    @RequiresPermissions("activiti:extend_k_append_form_deployment:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        extendKAppendFormDeploymentService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "extend_k_append_form_deployment-批量删除")
    @ApiOperation(value = "extend_k_append_form_deployment-批量删除", notes = "extend_k_append_form_deployment-批量删除")
    @RequiresPermissions("activiti:extend_k_append_form_deployment:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.extendKAppendFormDeploymentService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "extend_k_append_form_deployment-通过id查询", notes = "extend_k_append_form_deployment-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<ExtendKAppendFormDeployment> queryById(@RequestParam(name = "id", required = true) String id) {
        ExtendKAppendFormDeployment extendKAppendFormDeployment = extendKAppendFormDeploymentService.getById(id);
        if (extendKAppendFormDeployment == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(extendKAppendFormDeployment);
    }


}
