package org.jeecg.modules.activiti.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.jeecg.modules.online.cgform.service.IOnlCgformHeadService;
import org.jeecg.modules.online.cgform.service.IOnlineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "流程模型在线表格")
@RestController
@RequestMapping({"/activiti/online/cgform/api"})
public class ActivitiCommonController {

    @Resource
    private IOnlCgformHeadService onlCgformHeadService;

    @Resource
    private IOnlineService onlineService;

    @GetMapping({"/getFormItem/{code}"})
    public Result<?> getFormItem(@PathVariable("code") String var1, HttpServletRequest var2) {
        OnlCgformHead var3;

        try {
            var3 = this.onlCgformHeadService.getTable(var1);
        } catch (org.jeecg.modules.online.config.exception.a var8) {
            return Result.error("表不存在");
        }

        Result var4 = new Result();
        LoginUser var5 = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String var6 = var2.getParameter("selectFields");
        if (oConvertUtils.isNotEmpty(var6)) {
            List var7 = Arrays.asList(var6.split(","));
        }

        JSONObject var9 = this.onlineService.queryOnlineFormItem(var3, var5.getUsername());
        var4.setResult(org.jeecg.modules.online.cgform.d.b.b(var9));
        var4.setOnlTable(var3.getTableName());
        return var4;
    }

    @GetMapping({"/form/table_name/{tableName}/{dataId}"})
    public Result<?> table_name(@PathVariable("tableName") String var1, @PathVariable("dataId") String var2) {
        try {
            LambdaQueryWrapper<OnlCgformHead> var3 = new LambdaQueryWrapper();
            var3.eq(OnlCgformHead::getTableName, var1);
            OnlCgformHead var4 = this.onlCgformHeadService.getOne(var3);
            if (var4 == null) {
                throw new Exception("OnlCgform tableName: " + var1 + " 不存在！");
            } else {
                SqlInjectionUtil.filterContent(var2, "'");
                Result var5 = this.b(var4.getId(), var2);
                var5.setOnlTable(var1);
                return var5;
            }
        } catch (Exception var6) {
            log.error("Online表单查询异常，" + var6.getMessage(), var6);
            return Result.error("查询失败，" + var6.getMessage());
        }
    }

    @GetMapping({"/form/{code}/{id}"})
    public Result<?> b(@PathVariable("code") String var1, @PathVariable("id") String var2) {
        try {
            SqlInjectionUtil.filterContent(var2, "'");
            Map var3 = this.onlCgformHeadService.queryManyFormData(var1, var2);
            return Result.ok(org.jeecg.modules.online.cgform.d.b.a(var3));
        } catch (Exception var4) {
            log.error("Online表单查询异常：" + var4.getMessage(), var4);
            return Result.error("查询失败，" + var4.getMessage());
        }
    }
}
