package org.jeecg.modules.activiti.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 流程表单部署发布
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Data
@TableName("extend_model_form_deployment")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "流程表单部署发布", description = "流程表单部署发布")
public class ExtendModelFormDeployment implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
    /**
     * 创建者
     */
    @ApiModelProperty(value = "创建者")
    private String createBy;
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    /**
     * 更新者
     */
    @ApiModelProperty(value = "更新者")
    private String updateBy;
    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    /**
     * 部门编码
     */
    @ApiModelProperty(value = "部门编码")
    private String sysOrgCode;
    /**
     * 逻辑删除标记（0：显示；1：隐藏）
     */
    @Excel(name = "逻辑删除标记（0：显示；1：隐藏）", width = 15)
    @ApiModelProperty(value = "逻辑删除标记（0：显示；1：隐藏）")
    @TableLogic
    private Integer delFlag;
    /**
     * 模型ID
     */
    @Excel(name = "模型ID", width = 15)
    @ApiModelProperty(value = "模型ID")
    private String modelId;

    /**
     * 流程定义Name
     */
    @Excel(name = "流程定义Name", width = 15)
    @ApiModelProperty(value = "流程定义Name")
    private String modelName;

    @Excel(name = "打印配置ID", width = 15)
    @ApiModelProperty(value = "打印配置ID")
    private String printConfId;
    /**
     * 流程定义Key
     */
    @Excel(name = "流程定义Key", width = 15)
    @ApiModelProperty(value = "流程定义Key")
    private String modelKey;

    /**
     * 表单JSON
     */
    @Excel(name = "表单JSON", width = 15)
    @ApiModelProperty(value = "表单JSON")
    private String htmlJson;
    /**
     * 主表ID
     */
    @Excel(name = "主表ID", width = 15)
    @ApiModelProperty(value = "主表ID")
    private String masterTableId;
    /**
     * 字表ID
     */
    @Excel(name = "字表ID", width = 15)
    @ApiModelProperty(value = "字表ID")
    private String slaveTableId;
    /**
     * tableName
     */
    @Excel(name = "tableName", width = 15)
    @ApiModelProperty(value = "tableName")
    private String tableName;

    /**
     * tableName
     */
    @Excel(name = "tableId", width = 15)
    @ApiModelProperty(value = "tableId")
    private String tableId;
    /**
     * 表单类型（0、自定义表单 1、Onl表单）
     */
    @Dict(dicCode = "activiti_form_category")
    @Excel(name = "表单类型（0、自定义表单 1、Onl表单）", width = 15)
    @ApiModelProperty(value = "表单类型（1、自定义表单 2、Onl表单）")
    private Integer formCategory;
    /**
     * 流程状态列
     */
    @Excel(name = "流程状态列", width = 15)
    @ApiModelProperty(value = "流程状态列")
    private String flowStatusCol;
    /**
     * 流程定义ID
     */
    @Excel(name = "流程定义ID", width = 15)
    @ApiModelProperty(value = "流程定义ID")
    private String processDefinitionId;
    /**
     * 版本号
     */
    @Excel(name = "版本号", width = 15)
    @ApiModelProperty(value = "版本号")
    private Integer version;
    /**
     * 流程部署ID
     */
    @Excel(name = "流程部署ID", width = 15)
    @ApiModelProperty(value = "流程部署ID")
    private String deploymentId;


    /**
     * 所属业务板块
     */
    @Excel(name = "所属业务板块", width = 15)
    @ApiModelProperty(value = "所属业务板块")
    @Dict(dicCode = "activiti_biz_module")
    private Integer bizModule;
}
