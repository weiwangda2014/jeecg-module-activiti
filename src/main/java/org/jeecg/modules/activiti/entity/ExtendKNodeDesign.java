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
 * @Description: extend_k_node_design
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Data
@TableName("extend_k_node_design")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "extend_k_node_design对象", description = "extend_k_node_design")
public class ExtendKNodeDesign implements Serializable {
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
    @Excel(name = "逻辑删除", width = 15)
    @ApiModelProperty(value = "逻辑删除")
    @TableLogic
    private Integer delFlag;
    /**
     * 部门编码
     */
    @ApiModelProperty(value = "部门编码")
    private String sysOrgCode;
    /**
     * modelId
     */
    @Excel(name = "modelId", width = 15)
    @ApiModelProperty(value = "modelId")
    private String modelId;
    /**
     * nodeId
     */
    @Excel(name = "nodeId", width = 15)
    @ApiModelProperty(value = "nodeId")
    private String nodeId;
    /**
     * nodeType
     */
    @Excel(name = "nodeType", width = 15)
    @ApiModelProperty(value = "nodeType")
    private Integer nodeType;
    /**
     * 是否可以查看附加表单
     */
    @Excel(name = "是否可以查看附加表单", width = 15)
    @ApiModelProperty(value = "是否可以查看附加表单")
    private Integer isShowAppendForm;
    /**
     * nodeScript
     */
    @Excel(name = "nodeScript", width = 15)
    @ApiModelProperty(value = "nodeScript")
    private String nodeScript;
}
