package com.project.milo.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DataMeter {


    //序号
    //@Excel(name = "序号",width = 5)
    @ExcelProperty("序号")
    private Long scId;

    //单位名称
    @ExcelProperty("单位名称")
    private String name;

    //类型
    @ExcelProperty("类型")
    private String type;

    //服务器资料
    @ExcelProperty("服务器资料")
    private String fuWuQi;

    //签名地址信息
    @ExcelProperty("签名地址信息")
    private String sign;

    //部署日期
    @ExcelProperty("部署日期")
    private String bsDate;

    //上线日期
    @ExcelProperty("上线日期")
    private String sxDate;

    //负责人
    @ExcelProperty("负责人")
    private String concat;

    //版本号
    @ExcelProperty("版本号")
    private String version;

    //备注
    @ExcelIgnore
    @ExcelProperty("备注")
    private String remark;

    @ExcelIgnore
    @ExcelProperty("boss")
    private String boss;





}
