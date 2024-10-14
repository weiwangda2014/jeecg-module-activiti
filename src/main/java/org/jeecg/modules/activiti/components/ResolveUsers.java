package org.jeecg.modules.activiti.components;

import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component
public class ResolveUsers {
    /**
     * 会签时，动态获取审批人
     *
     * @param execution
     * @return
     */
    public List<String> resolveUsersForTask(DelegateExecution execution, String value) {
        System.out.println(value);
        String[] user = value.split(",");
        String instanceId = execution.getProcessInstanceId();
        System.out.println("实例id:" + instanceId);
        List<String> users = Arrays.asList(user);

        return users;
    }
}