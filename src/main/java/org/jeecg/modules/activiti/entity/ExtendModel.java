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
 * @Description: 流程模型
 * @Author: jeecg-boot
 * @Date: 2023-10-21
 * @Version: V1.0
 */
@Data
@TableName("extend_model")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "extend_act_model对象", description = "流程模型")
public class ExtendModel implements Serializable {
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
     * 所属部门
     */
    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
    /**
     * 逻辑删除
     */
    @Excel(name = "逻辑删除", width = 15)
    @ApiModelProperty(value = "逻辑删除")
    @TableLogic
    private Integer delFlag;
    /**
     * 模型名称
     */
    @Excel(name = "模型名称", width = 15)
    @ApiModelProperty(value = "模型名称")
    private String modelName;
    /**
     * 模型键
     */
    @Excel(name = "模型键", width = 15)
    @ApiModelProperty(value = "模型键")
    private String modelKey;
    /**
     * 模型类别
     */
    @Excel(name = "模型类别", width = 15)
    @ApiModelProperty(value = "模型类别")
    private String modelCategory;
    /**
     * 模型版本
     */
    @Excel(name = "模型版本", width = 15)
    @ApiModelProperty(value = "模型版本")
    private Integer modelVersion;

    /**
     * 模型XML
     */
    @Excel(name = "模型XML", width = 15)
    @ApiModelProperty(value = "模型XML")
    private String modelXml;

    /**
     * 模型Svg
     */
    @Excel(name = "模型Svg", width = 15)
    @ApiModelProperty(value = "模型Svg")
    private String modelSvg;

    /**
     * 模型信息
     */
    @Excel(name = "模型信息", width = 15)
    private String modelMetaInfo;

    /**
     * 模型部署编码
     */
    @Excel(name = "模型部署编码", width = 15)
    @ApiModelProperty(value = "模型部署编码")
    private String modelDeploymentId;
    /**
     * 模型租户
     */
    @Excel(name = "模型租户", width = 15)
    @ApiModelProperty(value = "模型租户")
    private String modelTenantId;
    /**
     * 模型编码
     */
    @Excel(name = "模型编码", width = 15)
    @ApiModelProperty(value = "模型编码")
    private String modelId;
    /**
     * 所属业务板块
     */
    @Excel(name = "所属业务板块", width = 15)
    @ApiModelProperty(value = "所属业务板块")
    @Dict(dicCode = "activiti_biz_module")
    private Integer bizModule;
    /**
     * 图标
     */
    @Excel(name = "图标", width = 15)
    @ApiModelProperty(value = "图标")
    private String icon;
    /**
     * 显示表单（0-否,1-是）
     */
    @Excel(name = "显示表单（0-否,1-是）", width = 15, dicCode = "activiti_form_status")
    @Dict(dicCode = "activiti_form_status")
    @ApiModelProperty(value = "显示表单（0-否,1-是）")
    private Integer formStatus;
    /**
     * 表单类型（0-自定义 ,1-ONLINE表单）
     */
    @Excel(name = "表单类型（1-自定义 ,2-ONLINE表单）", width = 15, dicCode = "activiti_form_category")
    @Dict(dicCode = "activiti_form_category")
    @ApiModelProperty(value = "表单类型（1-自定义 ,2-ONLINE表单）")
    private Integer formCategory;
    /**
     * 表ID
     */
    @Excel(name = "表ID", width = 15)
    @ApiModelProperty(value = "表ID")
    private String tableId;
    /**
     * 表名称
     */
    @Excel(name = "表名称", width = 15)
    @ApiModelProperty(value = "表名称")
    private String tableName;
    /**
     * 流程状态字段
     */
    @Excel(name = "流程状态字段", width = 15)
    @ApiModelProperty(value = "流程状态字段")
    private String processStatusField;
    /**
     * 流程描述
     */
    @Excel(name = "流程描述", width = 15)
    @ApiModelProperty(value = "流程描述")
    private String description;
    /**
     * 流程定义ID
     */
    @Excel(name = "流程定义ID", width = 15)
    @ApiModelProperty(value = "流程定义ID")
    private String processDefinitionId;
    /**
     * 流程是否发布（0-未发布 ，1-已发布）
     */
    @Excel(name = "流程是否发布（0-未发布 ，1-已发布）", width = 15, dicCode = "activiti_deploy_status")
    @Dict(dicCode = "activiti_deploy_status")
    @ApiModelProperty(value = "流程是否发布（0-未发布 ，1-已发布）")
    private Integer deployStatus;
    /**
     * 发布时间
     */
    @Excel(name = "发布时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "发布时间")
    private Date deployTime;
    /**
     * 打印配置ID
     */
    @Excel(name = "打印配置ID", width = 15)
    @ApiModelProperty(value = "打印配置ID")
    private String printConfId;
    /**
     * 标题表达式
     * 参考：XXXX【${busname}】-XXXX【${name}】；其中${}表达式取流程变量的值
     */
    @Excel(name = "标题表达式", width = 15)
    @ApiModelProperty(value = "标题表达式")
    private String titleExpression;
    /**
     * 简介表达式
     */
    @Excel(name = "简介表达式", width = 15)
    @ApiModelProperty(value = "简介表达式")
    private String descriptionExpression;
}
