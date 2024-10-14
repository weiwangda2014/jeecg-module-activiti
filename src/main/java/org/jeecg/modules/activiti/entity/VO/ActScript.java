package org.jeecg.modules.activiti.entity.VO;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@EqualsAndHashCode(callSuper = false)
public class ActScript implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 操作
     */
    private String option;
    /**
     * 范围
     */
    private String scope;

    /**
     * 是否禁用
     */
    private boolean disable = false;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 字段名称
     */
    private ArrayList<String> columnNames;
    /**
     * 字段值
     */
    private ArrayList<Value> values;
    /**
     * 条件
     */
    private ArrayList<Condition> conditions;

    @Data
    @EqualsAndHashCode(callSuper = false)
    public class Condition implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        /**
         * where 字段
         */
        private String whereColumnName;
        /**
         * 匹配方式
         */
        private String match;
        /**
         * 匹配值
         */
        private String whereValueColumn;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public class Value implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        /**
         * 值字段 字段
         */
        private String valueColumnName;
        /**
         * 是否是表达式
         */
        private Boolean isExp;

        /**
         * 是否是主键
         */
        private Boolean isPk;
        /**
         * pk值来源  value: 本字段 sys：系统
         */
        private String pkSrc;

    }

}
