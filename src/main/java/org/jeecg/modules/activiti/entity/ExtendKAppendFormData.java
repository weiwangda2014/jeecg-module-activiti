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
 * @Description: extend_k_append_form_data
 * @Author: jeecg-boot
 * @Date:   2023-10-22
 * @Version: V1.0
 */
@Data
@TableName("extend_k_append_form_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="extend_k_append_form_data对象", description="extend_k_append_form_data")
public class ExtendKAppendFormData implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
	/**修改人*/
    @ApiModelProperty(value = "修改人")
    private String updateBy;
	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;
	/**逻辑删除*/
	@Excel(name = "逻辑删除", width = 15)
    @ApiModelProperty(value = "逻辑删除")
    @TableLogic
    private Integer delFlag;
	/**部门编码*/
    @ApiModelProperty(value = "部门编码")
    private String sysOrgCode;
	/**流程模型ID*/
	@Excel(name = "流程模型ID", width = 15)
    @ApiModelProperty(value = "流程模型ID")
    private String modelId;
	/**流程实例ID*/
	@Excel(name = "流程实例ID", width = 15)
    @ApiModelProperty(value = "流程实例ID")
    private String processInstanceId;
	/**执行ID*/
	@Excel(name = "执行ID", width = 15)
    @ApiModelProperty(value = "执行ID")
    private String executionId;
	/**任务ID*/
	@Excel(name = "任务ID", width = 15)
    @ApiModelProperty(value = "任务ID")
    private String taskId;
	/**附加表单ID*/
	@Excel(name = "附加表单ID", width = 15)
    @ApiModelProperty(value = "附加表单ID")
    private String formId;
	/**节点ID*/
	@Excel(name = "节点ID", width = 15)
    @ApiModelProperty(value = "节点ID")
    private String nodeId;
	/**提交人ID*/
	@Excel(name = "提交人ID", width = 15)
    @ApiModelProperty(value = "提交人ID")
    private String userId;
	/**提交人*/
	@Excel(name = "提交人", width = 15)
    @ApiModelProperty(value = "提交人")
    private String userName;
	/**表单数据*/
	@Excel(name = "表单数据", width = 15)
    @ApiModelProperty(value = "表单数据")
    private String formData;
}
