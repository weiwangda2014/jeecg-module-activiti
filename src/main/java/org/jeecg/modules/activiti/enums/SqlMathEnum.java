package org.jeecg.modules.activiti.enums;

public enum SqlMathEnum {
    EQ("EQ", "="),
    GT("GT", ">"),
    LT("LT", "<"),
    GTOREQ("GTOREQ", ">="),
    LTOREQ("LTOREQ", "<="),
    LIKE("LIKE", " like"),
    NOTEQ("NOTEQ", "!="),
    IN("IN", " in"),
    NOTIN("NOTIN", " not in");
    private String name;
    private String symbol;

    // 构造方法
    SqlMathEnum(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    // 普通方法
    public static String getSymbol(String name) {
        for (SqlMathEnum c : SqlMathEnum.values()) {
            if (c.getName().equals(name)) {
                return c.symbol;
            }
        }
        return null;
    }

    public static String getName(String symbol) {
        for (SqlMathEnum c : SqlMathEnum.values()) {
            if (c.getSymbol().equals(symbol)) {
                return c.name;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
