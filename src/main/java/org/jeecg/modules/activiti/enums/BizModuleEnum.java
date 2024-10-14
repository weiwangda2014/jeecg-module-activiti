package org.jeecg.modules.activiti.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jeecg.common.system.annotation.EnumDict;
import org.jeecg.common.system.vo.DictModel;

import java.util.ArrayList;
import java.util.List;

//@EnumDict("activiti_biz_module")
public enum BizModuleEnum {

/*    ADMINISTRATION(1, "行政"),
    LOGISTICS(2, "后勤"),
    SALE(3, "销售");

    @JsonValue
    @EnumValue
    Integer value;
    String text;

    BizModuleEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    *//**
     * 获取字典数据
     *
     * @return
     *//*
    public static List<DictModel> getDictList() {
        List<DictModel> list = new ArrayList<>();
        DictModel dictModel;
        for (BizModuleEnum e : BizModuleEnum.values()) {
            dictModel = new DictModel();
            dictModel.setValue(e.value + "");
            dictModel.setText(e.text);
            list.add(dictModel);
        }
        return list;
    }*/
}
