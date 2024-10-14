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
 * @Description: extend_k_node_file
 * @Author: jeecg-boot
 * @Date:   2023-10-22
 * @Version: V1.0
 */
@Data
@TableName("extend_k_node_file")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="extend_k_node_file对象", description="extend_k_node_file")
public class ExtendKNodeFile implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
	/**nodeId*/
	@Excel(name = "nodeId", width = 15)
    @ApiModelProperty(value = "nodeId")
    private String nodeId;
	/**用户ID*/
	@Excel(name = "用户ID", width = 15)
    @ApiModelProperty(value = "用户ID")
    private String userId;
	/**用户名称*/
	@Excel(name = "用户名称", width = 15)
    @ApiModelProperty(value = "用户名称")
    private String userName;
	/**filePath*/
	@Excel(name = "filePath", width = 15)
    @ApiModelProperty(value = "filePath")
    private String filePath;
	/**modelId*/
	@Excel(name = "modelId", width = 15)
    @ApiModelProperty(value = "modelId")
    private String modelId;
	/**processInstanceId*/
	@Excel(name = "processInstanceId", width = 15)
    @ApiModelProperty(value = "processInstanceId")
    private String processInstanceId;
	/**任务ID*/
	@Excel(name = "任务ID", width = 15)
    @ApiModelProperty(value = "任务ID")
    private String taskId;
	/**执行ID*/
	@Excel(name = "执行ID", width = 15)
    @ApiModelProperty(value = "执行ID")
    private String executionId;
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
	/**部门编码*/
    @ApiModelProperty(value = "部门编码")
    private String sysOrgCode;
	/**逻辑删除*/
	@Excel(name = "逻辑删除", width = 15)
    @ApiModelProperty(value = "逻辑删除")
    @TableLogic
    private Integer delFlag;
}
