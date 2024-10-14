package org.jeecg.modules.activiti.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.activiti.entity.ExtendModelFormData;
import org.jeecg.modules.activiti.service.IExtendModelFormDataService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Description: 流程表单数据
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Api(tags = "流程表单数据")
@RestController
@RequestMapping("/activiti/formData")
@Slf4j
public class ExtendModelFormDataController extends JeecgController<ExtendModelFormData, IExtendModelFormDataService> {

    @Resource
    private IExtendModelFormDataService extendModelFormDataService;


    /**
     * 分页列表查询
     *
     * @param extendModelFormData
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "流程表单数据-分页列表查询", notes = "流程表单数据-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<ExtendModelFormData>> queryPageList(ExtendModelFormData extendModelFormData,
                                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                            HttpServletRequest req) {
        QueryWrapper<ExtendModelFormData> queryWrapper = QueryGenerator.initQueryWrapper(extendModelFormData, req.getParameterMap());
        Page<ExtendModelFormData> page = new Page<>(pageNo, pageSize);
        IPage<ExtendModelFormData> pageList = extendModelFormDataService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param extendModelFormData
     * @return
     */
    @AutoLog(value = "流程表单数据-添加")
    @ApiOperation(value = "流程表单数据-添加", notes = "流程表单数据-添加")
    // @RequiresPermissions("activiti:extend_model_form_data:add")
    @PostMapping(value = "/add")
    public Result<ExtendModelFormData> add(@RequestBody ExtendModelFormData extendModelFormData) {
        extendModelFormDataService.save(extendModelFormData);
        return Result.OK(extendModelFormData);
    }

    /**
     * 编辑
     *
     * @param extendModelFormData
     * @return
     */
    @AutoLog(value = "流程表单数据-编辑")
    @ApiOperation(value = "流程表单数据-编辑", notes = "流程表单数据-编辑")
    // @RequiresPermissions("activiti:extend_model_form_data:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<ExtendModelFormData> edit(@RequestBody ExtendModelFormData extendModelFormData) {
        extendModelFormDataService.updateById(extendModelFormData);
        return Result.OK(extendModelFormData);
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "流程表单数据-通过id删除")
    @ApiOperation(value = "流程表单数据-通过id删除", notes = "流程表单数据-通过id删除")
    //  @RequiresPermissions("activiti:extend_model_form_data:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        extendModelFormDataService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "流程表单数据-批量删除")
    @ApiOperation(value = "流程表单数据-批量删除", notes = "流程表单数据-批量删除")
    // @RequiresPermissions("activiti:extend_model_form_data:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.extendModelFormDataService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "流程表单数据-通过id查询")
    @ApiOperation(value = "流程表单数据-通过id查询", notes = "流程表单数据-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<ExtendModelFormData> queryById(@RequestParam(name = "id") String id) {
        ExtendModelFormData extendModelFormData = extendModelFormDataService.getById(id);
        if (extendModelFormData == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(extendModelFormData);
    }

    @PostMapping("/saveOrUpdate")
    @ApiOperation(value = "流程表单数据保存或修改", notes = "流程表单数据保存/修改")
    public Result<Object> saveOrUpdate(@RequestBody ExtendModelFormData formData) {
        extendModelFormDataService.saveOrUpdate(formData);
        return Result.OK(formData);
    }

}
