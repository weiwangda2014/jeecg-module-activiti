package org.jeecg.modules.activiti.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
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
 * @Description: extend_k_node
 * @Author: jeecg-boot
 * @Date: 2023-10-22
 * @Version: V1.0
 */
@Data
@TableName("extend_k_node")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "extend_k_node对象", description = "extend_k_node")
public class ExtendKNode implements Serializable {
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
     * 节点ID
     */
    @Excel(name = "节点ID", width = 15)
    @ApiModelProperty(value = "节点ID")
    private String nodeId;
    /**
     * 节点名称
     */
    @Excel(name = "节点名称", width = 15)
    @ApiModelProperty(value = "节点名称")
    private String nodeName;
    /**
     * appendFormId
     */
    @Excel(name = "appendFormId", width = 15)
    @ApiModelProperty(value = "appendFormId")
    private String appendFormId;
    /**
     * 模型ID
     */
    @Excel(name = "模型ID", width = 15)
    @ApiModelProperty(value = "模型ID")
    private String modelId;
    /**
     * processDefinitionId
     */
    @Excel(name = "processDefinitionId", width = 15)
    @ApiModelProperty(value = "processDefinitionId")
    private String processDefinitionId;
    /**
     * priority
     */
    @Excel(name = "priority", width = 15)
    @ApiModelProperty(value = "priority")
    private String priority;
    /**
     * incoming
     */
    @Excel(name = "incoming", width = 15)
    @ApiModelProperty(value = "incoming")
    private String incoming;
    /**
     * outgoing
     */
    @Excel(name = "outgoing", width = 15)
    @ApiModelProperty(value = "outgoing")
    private String outgoing;
    /**
     * candidateUsers
     */
    @Excel(name = "candidateUsers", width = 15)
    @ApiModelProperty(value = "candidateUsers")
    private String candidateUsers;
    /**
     * candidateGroup
     */
    @Excel(name = "candidateGroup", width = 15)
    @ApiModelProperty(value = "candidateGroup")
    private String candidateGroup;
    /**
     * assignee
     */
    @Excel(name = "assignee", width = 15)
    @ApiModelProperty(value = "assignee")
    private String assignee;
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
     * 节点脚本
     */
    @TableField(exist = false)
    private String nodeScript;
}
