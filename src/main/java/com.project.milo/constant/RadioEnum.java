package com.project.milo.constant;

public enum RadioEnum {

    WR(1, "吴荣"),
    HYH(2, "何沅航"),
    WDY(3, "王冬洋"),
    CWL(4, "陈武龙"),
    ZZJ(5, "朱志军");

    private  Integer code;
    private  String name;



    RadioEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    // 提供 getter
    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
