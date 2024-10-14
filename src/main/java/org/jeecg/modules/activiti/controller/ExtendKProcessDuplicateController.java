package org.jeecg.modules.activiti.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.activiti.entity.ExtendKProcessDuplicate;
import org.jeecg.modules.activiti.service.IExtendKProcessDuplicateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Description: extend_k_process_duplicate
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Api(tags = "抄送")
@RestController
@RequestMapping("/activiti/duplicate")
@Slf4j
public class ExtendKProcessDuplicateController extends JeecgController<ExtendKProcessDuplicate, IExtendKProcessDuplicateService> {


    @Resource
    private IExtendKProcessDuplicateService extendKProcessDuplicateService;


    /**
     * 获取我的抄送列表
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取我的抄送列表", notes = "获取我的抄送列表")
    public Result<IPage<ExtendKProcessDuplicate>> queryPageList(ExtendKProcessDuplicate extendKProcessDuplicate,
                                                                @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                HttpServletRequest request) {

        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        LambdaQueryWrapper<ExtendKProcessDuplicate> queryWrapper = QueryGenerator
                .initQueryWrapper(extendKProcessDuplicate, request.getParameterMap())
                .lambda()
                .eq(ExtendKProcessDuplicate::getUserName, loginUser.getUsername())
                .orderByDesc(ExtendKProcessDuplicate::getCreateTime);
        Page<ExtendKProcessDuplicate> page = new Page<>(pageNo, pageSize);
        IPage<ExtendKProcessDuplicate> pageList = extendKProcessDuplicateService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

}
