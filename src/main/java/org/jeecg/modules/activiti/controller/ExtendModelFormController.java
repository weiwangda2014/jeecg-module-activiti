package org.jeecg.modules.activiti.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.activiti.entity.ExtendModelForm;
import org.jeecg.modules.activiti.service.IExtendModelFormService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @Description: 流程表单
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Api(tags = "流程表单")
@RestController
@RequestMapping("/activiti/extendModelForm")
@Slf4j
public class ExtendModelFormController extends JeecgController<ExtendModelForm, IExtendModelFormService> {

    @Resource
    private IExtendModelFormService extendModelFormService;

    /**
     * 分页列表查询
     *
     * @param extendModelForm
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "流程表单-分页列表查询")
    @ApiOperation(value = "流程表单-分页列表查询", notes = "流程表单-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<ExtendModelForm>> queryPageList(ExtendModelForm extendModelForm,
                                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                        HttpServletRequest req) {
        QueryWrapper<ExtendModelForm> queryWrapper = QueryGenerator.initQueryWrapper(extendModelForm, req.getParameterMap());
        Page<ExtendModelForm> page = new Page<>(pageNo, pageSize);
        IPage<ExtendModelForm> pageList = extendModelFormService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param extendModelForm
     * @return
     */
    @AutoLog(value = "流程表单-添加")
    @ApiOperation(value = "流程表单-添加", notes = "流程表单-添加")
    //@RequiresPermissions("activiti:extend_model_form:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody ExtendModelForm extendModelForm) {
        extendModelFormService.saveOrUpdate(extendModelForm);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param extendModelForm
     * @return
     */
    @AutoLog(value = "流程表单-编辑")
    @ApiOperation(value = "流程表单-编辑", notes = "流程表单-编辑")
    //@RequiresPermissions("activiti:extend_model_form:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody ExtendModelForm extendModelForm) {
        extendModelFormService.saveOrUpdate(extendModelForm);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "流程表单-通过id删除")
    @ApiOperation(value = "流程表单-通过id删除", notes = "流程表单-通过id删除")
    //@RequiresPermissions("activiti:extend_model_form:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        extendModelFormService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "流程表单-批量删除")
    @ApiOperation(value = "流程表单-批量删除", notes = "流程表单-批量删除")
    //@RequiresPermissions("activiti:extend_model_form:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.extendModelFormService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "流程表单-通过id查询")
    @ApiOperation(value = "流程表单-通过id查询", notes = "流程表单-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<ExtendModelForm> queryById(@RequestParam(name = "id") String id) {
        ExtendModelForm extendModelForm = extendModelFormService.getById(id);
        if (extendModelForm == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(extendModelForm);
    }

    /**
     * 流程管理 --&gt; 设计表单
     *
     * @param modelId
     * @return
     */
    @GetMapping("/getFormByModelId")
    @ApiOperation(value = "根据模型ID查询设计表单", notes = "根据模型ID查询设计表单")
    public Result<Object> getFormByModelId(@ApiParam(name = "modelId", value = "模型定义ID", required = true) @RequestParam(name = "modelId", required = true) String modelId) {
        ExtendModelForm actReModelForm = extendModelFormService.getOne(new QueryWrapper<ExtendModelForm>()
                .lambda()
                .eq(ExtendModelForm::getModelId, modelId));
        return Result.OK(actReModelForm);
    }
}
