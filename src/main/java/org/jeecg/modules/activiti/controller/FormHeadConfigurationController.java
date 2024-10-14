package org.jeecg.modules.activiti.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.online.cgform.entity.OnlCgformHead;
import org.jeecg.modules.online.cgform.mapper.OnlCgformHeadMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "表单配置")
@RestController
@RequestMapping("/form/formConfiguration")
public class FormHeadConfigurationController {

    @Resource
    private OnlCgformHeadMapper onlCgformHeadMapper;

    @AutoLog(value = "表单配置-列表查询")
    @ApiOperation(value = "表单配置-列表查询", notes = "表单配置-列表查询")
    @GetMapping(value = "/tableList")
    public Result<?> tableList(@RequestParam("tableTypes") String tableTypes, HttpServletRequest req) {
        String[] tps = tableTypes.split(",");
        List<OnlCgformHead> list = onlCgformHeadMapper.selectList(new QueryWrapper<OnlCgformHead>()
                .lambda()
                .in(OnlCgformHead::getTableType, tps)
                .eq(OnlCgformHead::getCopyType, 0)
        );
        //Map<String, String> maps = list.stream().collect(Collectors.toMap(OnlCgformHead::getId, OnlCgformHead::getTableName));
        return Result.OK(list);
    }
}
