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
import org.jeecg.modules.activiti.entity.ExtendKNode;
import org.jeecg.modules.activiti.service.IExtendKNodeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @Description: extend_k_node
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Api(tags = "extend_k_node")
@RestController
@RequestMapping("/activiti/extendKNode")
@Slf4j
public class ExtendKNodeController extends JeecgController<ExtendKNode, IExtendKNodeService> {
    @Resource
    private IExtendKNodeService extendKNodeService;

    /**
     * 分页列表查询
     *
     * @param extendKNode
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "extend_k_node-分页列表查询", notes = "extend_k_node-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<ExtendKNode>> queryPageList(ExtendKNode extendKNode,
                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                    HttpServletRequest req) {
        QueryWrapper<ExtendKNode> queryWrapper = QueryGenerator.initQueryWrapper(extendKNode, req.getParameterMap());
        Page<ExtendKNode> page = new Page<>(pageNo, pageSize);
        IPage<ExtendKNode> pageList = extendKNodeService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param extendKNode
     * @return
     */
    @AutoLog(value = "extend_k_node-添加")
    @ApiOperation(value = "extend_k_node-添加", notes = "extend_k_node-添加")
    @RequiresPermissions("activiti:extend_k_node:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody ExtendKNode extendKNode) {
        extendKNodeService.save(extendKNode);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param extendKNode
     * @return
     */
    @AutoLog(value = "extend_k_node-编辑")
    @ApiOperation(value = "extend_k_node-编辑", notes = "extend_k_node-编辑")
    @RequiresPermissions("activiti:extend_k_node:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody ExtendKNode extendKNode) {
        extendKNodeService.updateById(extendKNode);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "extend_k_node-通过id删除")
    @ApiOperation(value = "extend_k_node-通过id删除", notes = "extend_k_node-通过id删除")
    @RequiresPermissions("activiti:extend_k_node:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        extendKNodeService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "extend_k_node-批量删除")
    @ApiOperation(value = "extend_k_node-批量删除", notes = "extend_k_node-批量删除")
    @RequiresPermissions("activiti:extend_k_node:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.extendKNodeService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "extend_k_node-通过id查询", notes = "extend_k_node-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<ExtendKNode> queryById(@RequestParam(name = "id") String id) {
        ExtendKNode extendKNode = extendKNodeService.getById(id);
        if (extendKNode == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(extendKNode);
    }


}
