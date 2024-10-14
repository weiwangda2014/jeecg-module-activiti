package org.jeecg.modules.activiti.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 流程表单
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Data
@TableName("extend_model_form")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "extend_model_form对象", description = "流程表单")
public class ExtendModelForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人")
    private String updateBy;
    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    /**
     * 逻辑删除
     */
    @Excel(name = "逻辑删除", width = 15)
    @ApiModelProperty(value = "逻辑删除")
    @TableLogic
    private Integer delFlag;
    /**
     * 所属部门
     */
    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
    /**
     * 模型编码
     */
    @Excel(name = "模型编码", width = 15)
    @ApiModelProperty(value = "模型编码")
    private String modelId;
    /**
     * 表单JSON
     */
    @Excel(name = "表单JSON", width = 15)
    @ApiModelProperty(value = "表单JSON")
    private String htmlJson;
    /**
     * 主表编码
     */
    @Excel(name = "主表编码", width = 15)
    @ApiModelProperty(value = "主表编码")
    private String masterTableId;
    /**
     * 子表编码
     */
    @Excel(name = "子表编码", width = 15)
    @ApiModelProperty(value = "子表编码")
    private String slaveTableId;

    /**
     * 表名称
     */
    @Excel(name = "表名称", width = 15)
    @ApiModelProperty(value = "表名称")
    private String tableId;
    /**
     * 表名称
     */
    @Excel(name = "表名称", width = 15)
    @ApiModelProperty(value = "表名称")
    private String tableName;
    /**
     * 表单类型（0-自定义 ,1-ONLINE表单）
     */
    @Excel(name = "表单类型（0-自定义 ,1-ONLINE表单）", width = 15, dicCode = "activiti_form_category")
    @Dict(dicCode = "activiti_form_category")
    @ApiModelProperty(value = "表单类型（1-自定义 ,2-ONLINE表单）")
    private Integer formCategory;
    /**
     * 流程状态列
     */
    @Excel(name = "流程状态列", width = 15)
    @ApiModelProperty(value = "流程状态列")
    private String flowStatusCol;
}
