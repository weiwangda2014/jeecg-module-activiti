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
 * @Description: 流程信息抄送
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Data
@TableName("extend_k_process_duplicate")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "流程信息抄送", description = "流程信息抄送")
public class ExtendKProcessDuplicate implements Serializable {
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
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
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
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;
    /**
     * 逻辑删除
     */

    @TableLogic
    private Integer delFlag;
    /**
     * 部门编码
     */
    @ApiModelProperty(value = "部门编码")
    private String sysOrgCode;
    /**
     * 流程实例ID
     */
    @Excel(name = "流程实例ID", width = 15)
    @ApiModelProperty(value = "流程实例ID")
    private String processInstanceId;
    /**
     * 流程ID
     */
    @Excel(name = "流程ID", width = 15)
    @ApiModelProperty(value = "流程ID")
    private String modelId;
    /**
     * 抄送用户ID
     */
    @Excel(name = "抄送用户ID", width = 15)
    @ApiModelProperty(value = "抄送用户ID")
    private String userId;
    /**
     * 抄送用户
     */
    @Excel(name = "抄送用户", width = 15)
    @ApiModelProperty(value = "抄送用户")
    private String userName;
    /**
     * 任务ID
     */
    @Excel(name = "任务ID", width = 15)
    @ApiModelProperty(value = "任务ID")
    private String taskId;

    /**
     * 任务名称
     */
    @Excel(name = "任务名称", width = 15)
    @ApiModelProperty(value = "任务名称")
    private String taskName;
    /**
     * 业务标题
     */
    @Excel(name = "业务标题", width = 15)
    @ApiModelProperty(value = "业务标题")
    private String title;
    /**
     * 模型名称
     */
    @Excel(name = "模型名称", width = 15)
    @ApiModelProperty(value = "模型名称")
    private String modelName;
    /**
     * 模型key
     */
    @Excel(name = "模型key", width = 15)
    @ApiModelProperty(value = "模型key")
    private String modelKey;
    /**
     * 发起人
     */
    @Excel(name = "发起人", width = 15)
    @ApiModelProperty(value = "发起人")
    private String initiator;
    /**
     * 开始时间
     */
    @Excel(name = "开始时间", width = 15)
    @ApiModelProperty(value = "开始时间")
    private String startTime;
    /**
     * 结束时间
     */
    @Excel(name = "结束时间", width = 15)
    @ApiModelProperty(value = "结束时间")
    private String endTime;
    /**
     * 耗时
     */
    @Excel(name = "耗时", width = 15)
    @ApiModelProperty(value = "耗时")
    private String totalTime;

    @Excel(name = "表单类型（1-自定义 ,2-ONLINE表单）", width = 15, dicCode = "activiti_form_category")
    @Dict(dicCode = "activiti_form_category")
    @ApiModelProperty(value = "表单类型（1-自定义 ,2-ONLINE表单）")
    private Integer formCategory;
}
