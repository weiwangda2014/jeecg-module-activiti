package org.jeecg.modules.activiti.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.List;
import java.util.Map;

/**
 * 流程表单数据历史表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("extend_hi_model_form_data")
public class ExtendHiModelFormData extends JeecgEntity {

    /**
     * 是否删除
     */
    @TableLogic
    @ApiModelProperty(value = "逻辑删除标记（0：显示；1：隐藏）")
    private Integer delFlag;
    /**
     * 所属部门
     */
    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
    /**
     * 流程模型ID
     */
    private String modelId;

    /**
     * 表单ID
     */
    private String formId;

    /**
     * 表单模型
     */
    private String formData;

    /**
     * Onl表单ID
     */
    private String tableId;

    /**
     * 数据ID
     */
    private String dataId;

    /**
     * Onl表单名称
     */
    private String tableName;

    /**
     * 流程状态（0：未提交、1、处理中、2、已完成、3、已退回、4、未通过、5、已撤销、6、已作废、7、已挂起）
     */
    @Dict(dicCode = "bpmn_status")
    private Integer bpmnStatus;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 提交类型（1、流程提交 2、工单提交 ）
     */
    private Integer submitType;



    @TableField(exist = false)
    private String modelKey;

    @TableField(exist = false)
    private String modelName;

    @TableField(exist = false)
    private List<Map<String, Object>> appendFormList;
}
