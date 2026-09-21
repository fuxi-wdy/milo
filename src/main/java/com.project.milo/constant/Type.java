package com.project.milo.constant;




/**
 * 字段类型（0：导出导入；1：仅导出；2：仅导入）
 */
public enum Type {


    ALL(0), EXPORT(1), IMPORT(2);
    private final int value;

    Type(int value)
    {
        this.value = value;
    }

    public int value()
    {
        return this.value;
    }
}
