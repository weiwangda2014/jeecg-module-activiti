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
 * @Description: 流程表单数据
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Data
@TableName("extend_model_form_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "extend_model_form_data对象", description = "流程表单数据")
public class ExtendModelFormData implements Serializable {
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
     * 修改人
     */
    @ApiModelProperty(value = "修改人")
    private String updateBy;
    /**
     * 修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;
    /**
     * 逻辑删除
     */
    @ApiModelProperty(value = "逻辑删除")
    @TableLogic
    private Integer delFlag;
    /**
     * 部门编码
     */
    @ApiModelProperty(value = "部门编码")
    private String sysOrgCode;

    /**
     * 表单ID
     */
    @Excel(name = "表单ID", width = 15)
    @ApiModelProperty(value = "表单ID")
    private String formId;
    /**
     * 流程定义ID
     */
    @Excel(name = "流程定义ID", width = 15)
    @ApiModelProperty(value = "流程定义ID")
    private String modelId;


    /**
     * 流程定义Name
     */
    @Excel(name = "流程定义Name", width = 15)
    @ApiModelProperty(value = "流程定义Name")
    private String modelName;


    /**
     * 流程定义Key
     */
    @Excel(name = "流程定义Key", width = 15)
    @ApiModelProperty(value = "流程定义Key")
    private String modelKey;

    /**
     * 所属业务板块
     */
    @Excel(name = "所属业务板块", width = 15)
    @ApiModelProperty(value = "所属业务板块")
    @Dict(dicCode = "activiti_biz_module")
    private Integer bizModule;
    /**
     * 表单JSON
     */
    @Excel(name = "表单JSON", width = 15)
    @ApiModelProperty(value = "表单JSON")
    private String htmlJson;

    private String printConfId;
    /**
     * 流程实例ID
     */
    @Excel(name = "流程实例ID", width = 15)
    @ApiModelProperty(value = "流程实例ID")
    private String processInstanceId;
    /**
     * tableId
     */
    @Excel(name = "tableId", width = 15)
    @ApiModelProperty(value = "tableId")
    private String tableId;
    /**
     * tableName
     */
    @Excel(name = "tableName", width = 15)
    @ApiModelProperty(value = "tableName")
    private String tableName;

    /**
     * 表单数据
     */
    @Excel(name = "表单数据", width = 15)
    @ApiModelProperty(value = "表单数据")
    private String formData;
    /**
     * dataId
     */
    @Excel(name = "dataId", width = 15)
    @ApiModelProperty(value = "dataId")
    private String dataId;
    /**
     * 用户ID
     */
    @Excel(name = "用户ID", width = 15)
    @ApiModelProperty(value = "用户ID")
    private String userId;
    /**
     * 用户名称
     */
    @Excel(name = "用户名称", width = 15)
    @ApiModelProperty(value = "用户名称")
    private String userName;
    /**
     * 流程状态（0：未提交、1、处理中、2、已完成、3、已退回）
     */
    @Dict(dicCode = "bpmn_status")
    @Excel(name = "流程状态（0：未提交、1、处理中、2、已完成、3、已退回）", width = 15)
    @ApiModelProperty(value = "流程状态（0：未提交、1、处理中、2、已完成、3、已退回）")
    private Integer bpmnStatus;

    /**
     * 流程定义ID
     */
    @Excel(name = "流程定义ID", width = 15)
    @ApiModelProperty(value = "流程定义ID")
    private String processDefinitionId;
    /**
     * 提交类型（2、工单提交 1、流程提交）
     */
    @Excel(name = "提交类型（2、工单提交 1、流程提交）", width = 15)
    @ApiModelProperty(value = "提交类型（2、工单提交 1、流程提交）")
    private Integer submitType;


    /**
     * 表单类型（0-自定义 ,1-ONLINE表单）
     */
    @Excel(name = "表单类型（0-自定义 ,1-ONLINE表单）", width = 15, dicCode = "activiti_form_category")
    @Dict(dicCode = "activiti_form_category")
    @ApiModelProperty(value = "表单类型（1-自定义 ,2-ONLINE表单）")
    private Integer formCategory;
}
