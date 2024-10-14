package org.jeecg.modules.activiti.service.impl;

import cn.hutool.db.Db;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.googlecode.aviator.AviatorEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.activiti.entity.ExtendKNodeDesign;
import org.jeecg.modules.activiti.entity.VO.ActScript;
import org.jeecg.modules.activiti.enums.SqlMathEnum;
import org.jeecg.modules.activiti.mapper.ExtendKNodeDesignMapper;
import org.jeecg.modules.activiti.service.IExtendKNodeDesignService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 *
 */
@Slf4j
@Service
public class ExtendKNodeDesignServiceImpl extends ServiceImpl<ExtendKNodeDesignMapper, ExtendKNodeDesign> implements IExtendKNodeDesignService {

    private static final String sqlBlankStr = "     ";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postProcessFormDataByScript(String modelId, String taskId, Map<String, Object> variables) {
        ExtendKNodeDesign actKNodeDesign = this.getOne(new QueryWrapper<ExtendKNodeDesign>()
                .lambda()
                .eq(ExtendKNodeDesign::getModelId, modelId)
                .eq(ExtendKNodeDesign::getNodeId, taskId));
        if (actKNodeDesign != null) {
            String nodeScript = actKNodeDesign.getNodeScript();
            if (StringUtils.isNotBlank(nodeScript)) {
                List<String> sqlList = parsScrip(nodeScript, variables);
                for (String sql : sqlList) {
                    try {
                        Db.use().execute(sql);
                    } catch (SQLException ex) {
                        throw new JeecgBootException(ex.getMessage());
                    }
                }
            }
        }
        log.info("modelId：{}，taskId：{} 没有定义节点脚本", modelId, taskId);
    }

    private List<String> parsScrip(String nodeScript, Map<String, Object> variables) throws JeecgBootException {
        List<ActScript> actScripts = getActScripts(nodeScript);
        List<String> sqlList = new ArrayList<>(actScripts.size());

        for (ActScript script : actScripts) {
            if (script.isDisable()) {
                continue;
            }
            String option = script.getOption();
            String tableName = script.getTableName();
            ArrayList<String> columnNames = script.getColumnNames();
            ArrayList<ActScript.Condition> conditions = script.getConditions();
            Integer columnsSize = columnNames.size();
            ArrayList<ActScript.Value> values = script.getValues();
            validateParams(option, tableName, columnNames, conditions, columnsSize, values);

            StringBuilder sql = new StringBuilder();
            if (StringUtils.equalsIgnoreCase(option, "update")) {
                buildUpdateSql(option, tableName, columnNames, conditions, columnsSize, values, sql, variables);
            } else if (StringUtils.equalsIgnoreCase(option, "insert")) {
                //todo 创建 插入操作 脚本
                buildInsertSql(variables, option, tableName, columnNames, columnsSize, values, sql);
            }
            log.info("scriptSql: {}", sql);
            sqlList.add(sql.toString());
        }
        return sqlList;

    }

    private void buildInsertSql(Map<String, Object> variables, String option, String tableName, ArrayList<String> columeNames, Integer columSize, ArrayList<ActScript.Value> values, StringBuilder sql) throws JeecgBootException {
        sql.append(option).append(sqlBlankStr);//insert
        sql.append("into").append(sqlBlankStr); // into
        sql.append(tableName).append(sqlBlankStr);  //table
        sql.append("(").append(sqlBlankStr);
        for (int i = 0; i < columSize; i++) {
            sql.append(columeNames.get(i));
            if (i + 1 < columSize) {
                sql.append(",");
            }
        }
        sql.append(")").append(sqlBlankStr);
        sql.append("values").append(sqlBlankStr);
        sql.append("(").append(sqlBlankStr);

        for (int i = 0; i < columSize; i++) {
            //todo 此处是变量
            ActScript.Value value = values.get(i);
            Boolean isExp = value.getIsExp();
            String valueColumnName = value.getValueColumnName();
            if (isExp) {
                sql.append(getColumnVal(AviatorEvaluator.execute(valueColumnName, variables))).append(sqlBlankStr);
            } else {
                Object insertColumnVal = variables.get(valueColumnName);
                if (insertColumnVal == null) {
                    throw new JeecgBootException("脚本错误，操作类型：insert ，设置的valueColumnName在主表中不存在,valueColumnName: " + valueColumnName);
                }
                if (insertColumnVal instanceof String) {
                    sql.append("'").append(insertColumnVal).append("'");
                } else {
                    sql.append(insertColumnVal);
                }
            }
            if (i + 1 < columSize) {
                sql.append(",").append(sqlBlankStr);
            }
        }
        sql.append(");").append(sqlBlankStr);
    }

    private static void buildUpdateSql(String option, String tableName, ArrayList<String> columnNames, ArrayList<ActScript.Condition> conditions, Integer columSize, ArrayList<ActScript.Value> values, StringBuilder sql, Map<String, Object> variables) throws JeecgBootException {
        sql.append(option).append(sqlBlankStr);
        sql.append(tableName).append(sqlBlankStr);
        sql.append("set").append(sqlBlankStr);

        for (int i = 0; i < columSize; i++) {
            sql.append(columnNames.get(i)).append("=");
            //todo 此处是变量
            ActScript.Value value = values.get(i);
            Boolean isExp = value.getIsExp();
            String valueColumnName = value.getValueColumnName();
            if (isExp) {
                sql.append(getColumnVal(AviatorEvaluator.execute(valueColumnName, variables))).append(sqlBlankStr);
            } else {
                Object setColumnVal = variables.get(valueColumnName);
                if (setColumnVal == null) {
                    throw new JeecgBootException("脚本错误，操作类型：update ，设置的valueColumnName在主表中不存在,valueColumnName: " + valueColumnName);
                }

                sql.append(getColumnVal(setColumnVal));
            }
            if (i + 1 < columSize) {
                sql.append(",").append(sqlBlankStr);
            }
        }

        sql.append("where").append(sqlBlankStr);
        StringBuilder whereCond = new StringBuilder();
        for (ActScript.Condition condition : conditions) {
            if (StringUtils.isBlank(whereCond)) {
                whereCond.append(condition.getWhereColumnName());
            } else {
                whereCond.append("and").append(sqlBlankStr).append(condition.getWhereColumnName());
            }
            //todo 此处是变量
            String whereValueColumn = condition.getWhereValueColumn();
            Object whereColumnVal = variables.get(whereValueColumn);
            if (whereColumnVal == null) {
                throw new JeecgBootException("脚本错误，设置的valueColumnName在主表中不存在,whereValueColumn: " + whereValueColumn);
            }
            whereCond.append(SqlMathEnum.getSymbol(condition.getMatch().toUpperCase()));
            if (whereColumnVal instanceof List) {
                whereCond.append("(");
                StringBuilder subWhereCond = new StringBuilder();
                for (Object subColumnVal : (List) whereColumnVal) {
                    if (subWhereCond.length() != 0) {
                        subWhereCond.append(",");
                    }
                    subWhereCond.append(getColumnVal(subColumnVal));
                }
                whereCond.append(subWhereCond);
                whereCond.append(")");
            } else {
                whereCond.append(getColumnVal(whereColumnVal));
            }
        }
        sql.append(whereCond);
    }

    private static String getColumnVal(Object column) {
        StringBuilder sql = new StringBuilder();
        if (column instanceof String) {
            sql.append("'").append(column).append("'").append(sqlBlankStr);
        } else {
            sql.append(column).append(sqlBlankStr);
        }
        return sql.toString();
    }

    private static void validateParams(String option, String tableName, ArrayList<String> columnNames, ArrayList<ActScript.Condition> conditions, Integer columSize, ArrayList<ActScript.Value> values) throws JeecgBootException {
        if (StringUtils.isBlank(option)) {
            throw new JeecgBootException("脚本错误，option 不能为空或者 null");
        }
        if (StringUtils.isBlank(tableName)) {
            throw new JeecgBootException("脚本错误，tableName 不能为空或者 null");
        }
        if (CollectionUtils.isEmpty(columnNames)) {
            throw new JeecgBootException("脚本错误，columnNames 不能为空或者 null");
        }

        ArrayList<String> illegalColumnNames = new ArrayList<>();
        illegalColumnNames.add("");
        illegalColumnNames.add(" ");
        illegalColumnNames.add(null);
        if (CollectionUtils.containsAny(columnNames, illegalColumnNames)) {
            throw new JeecgBootException("脚本错误，columnNames列表不能包含空或者null");
        }

        if (columSize.compareTo(values.size()) != 0) {
            throw new JeecgBootException("脚本错误，columnNames列表长度必须和 values 列表长度相等");
        }
        if (CollectionUtils.containsAny(columnNames, illegalColumnNames)) {
            throw new JeecgBootException("脚本错误，columnNames列表不能包含空或者null");
        }

        if (StringUtils.equals("update", option) && CollectionUtils.isEmpty(conditions)) {
            throw new JeecgBootException("脚本错误，操作类型为update时，conditions列表不能包含空或者null");
        }
    }


    private static List<ActScript> getActScripts(String nodeScript) throws JeecgBootException {
        List<ActScript> actScripts = JSON.parseObject(nodeScript, new TypeReference<List<ActScript>>() {
        });
        log.info("actScripts:{}", actScripts);
        return actScripts;
    }

    public static void main(String[] args) {

        //JSON格式

/*        [
        {
            "columnNames":[
            "name1",
                    "name2"
        ],
            "scope":"task",
                "values":[
            {
                "valueColumnName":"zhangsan",
                    "isExp":false
            },
            {
                "valueColumnName":"lisi+wangwu",
                    "isExp":true
            }
        ],
            "conditions":[
            {
                "whereValueColumn":"age",
                    "match":"eq",
                    "whereColumnName":"age"
            },
            {
                "wherevalueColumn":"age1",
                    "match":"eq",
                    "whereColumnName":"age1"
            }
        ],
            "option":"update",
                "tableName":"tableName1"
        }
]*/
    }
}
