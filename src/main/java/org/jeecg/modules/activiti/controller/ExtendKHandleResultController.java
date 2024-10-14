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
import org.jeecg.modules.activiti.entity.ExtendKHandleResult;
import org.jeecg.modules.activiti.service.IExtendKHandleResultService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @Description: extend_k_handle_result
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Api(tags = "extend_k_handle_result")
@RestController
@RequestMapping("/activiti/extendKHandleResult")
@Slf4j
public class ExtendKHandleResultController extends JeecgController<ExtendKHandleResult, IExtendKHandleResultService> {
    @Resource
    private IExtendKHandleResultService extendKHandleResultService;

    /**
     * 分页列表查询
     *
     * @param extendKHandleResult
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "extend_k_handle_result-分页列表查询", notes = "extend_k_handle_result-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<ExtendKHandleResult>> queryPageList(ExtendKHandleResult extendKHandleResult,
                                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                            HttpServletRequest req) {
        QueryWrapper<ExtendKHandleResult> queryWrapper = QueryGenerator.initQueryWrapper(extendKHandleResult, req.getParameterMap());
        Page<ExtendKHandleResult> page = new Page<>(pageNo, pageSize);
        IPage<ExtendKHandleResult> pageList = extendKHandleResultService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param extendKHandleResult
     * @return
     */
    @AutoLog(value = "extend_k_handle_result-添加")
    @ApiOperation(value = "extend_k_handle_result-添加", notes = "extend_k_handle_result-添加")
    @RequiresPermissions("activiti:extend_k_handle_result:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody ExtendKHandleResult extendKHandleResult) {
        extendKHandleResultService.save(extendKHandleResult);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param extendKHandleResult
     * @return
     */
    @AutoLog(value = "extend_k_handle_result-编辑")
    @ApiOperation(value = "extend_k_handle_result-编辑", notes = "extend_k_handle_result-编辑")
    @RequiresPermissions("activiti:extend_k_handle_result:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody ExtendKHandleResult extendKHandleResult) {
        extendKHandleResultService.updateById(extendKHandleResult);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "extend_k_handle_result-通过id删除")
    @ApiOperation(value = "extend_k_handle_result-通过id删除", notes = "extend_k_handle_result-通过id删除")
    @RequiresPermissions("activiti:extend_k_handle_result:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        extendKHandleResultService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "extend_k_handle_result-批量删除")
    @ApiOperation(value = "extend_k_handle_result-批量删除", notes = "extend_k_handle_result-批量删除")
    @RequiresPermissions("activiti:extend_k_handle_result:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.extendKHandleResultService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "extend_k_handle_result-通过id查询", notes = "extend_k_handle_result-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<ExtendKHandleResult> queryById(@RequestParam(name = "id") String id) {
        ExtendKHandleResult extendKHandleResult = extendKHandleResultService.getById(id);
        if (extendKHandleResult == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(extendKHandleResult);
    }

}