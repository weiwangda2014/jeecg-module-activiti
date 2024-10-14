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
import org.jeecg.modules.activiti.entity.ExtendKNodeDesign;
import org.jeecg.modules.activiti.service.IExtendKNodeDesignService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @Description: extend_k_node_design
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Api(tags = "extend_k_node_design")
@RestController
@RequestMapping("/activiti/extendKNodeDesign")
@Slf4j
public class ExtendKNodeDesignController extends JeecgController<ExtendKNodeDesign, IExtendKNodeDesignService> {
    @Resource
    private IExtendKNodeDesignService extendKNodeDesignService;

    /**
     * 分页列表查询
     *
     * @param extendKNodeDesign
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "extend_k_node_design-分页列表查询", notes = "extend_k_node_design-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<ExtendKNodeDesign>> queryPageList(ExtendKNodeDesign extendKNodeDesign,
                                                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                          HttpServletRequest req) {
        QueryWrapper<ExtendKNodeDesign> queryWrapper = QueryGenerator.initQueryWrapper(extendKNodeDesign, req.getParameterMap());
        Page<ExtendKNodeDesign> page = new Page<>(pageNo, pageSize);
        IPage<ExtendKNodeDesign> pageList = extendKNodeDesignService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param extendKNodeDesign
     * @return
     */
    @AutoLog(value = "extend_k_node_design-添加")
    @ApiOperation(value = "extend_k_node_design-添加", notes = "extend_k_node_design-添加")
    @RequiresPermissions("activiti:extend_k_node_design:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody ExtendKNodeDesign extendKNodeDesign) {
        extendKNodeDesignService.save(extendKNodeDesign);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param extendKNodeDesign
     * @return
     */
    @AutoLog(value = "extend_k_node_design-编辑")
    @ApiOperation(value = "extend_k_node_design-编辑", notes = "extend_k_node_design-编辑")
    @RequiresPermissions("activiti:extend_k_node_design:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody ExtendKNodeDesign extendKNodeDesign) {
        extendKNodeDesignService.updateById(extendKNodeDesign);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "extend_k_node_design-通过id删除")
    @ApiOperation(value = "extend_k_node_design-通过id删除", notes = "extend_k_node_design-通过id删除")
    @RequiresPermissions("activiti:extend_k_node_design:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        extendKNodeDesignService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "extend_k_node_design-批量删除")
    @ApiOperation(value = "extend_k_node_design-批量删除", notes = "extend_k_node_design-批量删除")
    @RequiresPermissions("activiti:extend_k_node_design:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.extendKNodeDesignService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "extend_k_node_design-通过id查询", notes = "extend_k_node_design-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<ExtendKNodeDesign> queryById(@RequestParam(name = "id") String id) {
        ExtendKNodeDesign extendKNodeDesign = extendKNodeDesignService.getById(id);
        if (extendKNodeDesign == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(extendKNodeDesign);
    }

}
