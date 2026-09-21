package com.project.milo.entity;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Record {

    @ExcelProperty(value = "记录编号",index = 0)
    private String id;
    @ExcelProperty(value = "单位名称",index = 1)
    private String name;
    @ExcelProperty(value = "记录日期",index = 2)
    private String riqi;
    @ExcelProperty(value = "问题描述",index = 3)
    private String question;
    @ExcelProperty(value = "处理方式",index = 4)
    private String answer;
    @ExcelProperty(value = "记录人",index = 5)
    private String op;

    @ExcelIgnore
    private String startDate;

    @ExcelIgnore
    private String endDate;

    @ExcelIgnore
    private Integer days;

    @ExcelIgnore
    private  List<Record> ordList;

    @ExcelIgnore
    private Integer count;

    @ExcelIgnore
    private Map<String, Object> params;

    public Map<String, Object> getParams() {
        if (params == null)
        {
            params = new HashMap<>();
        }
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
