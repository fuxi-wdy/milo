package com.project.milo.service.impl;

import com.project.milo.constant.CityEnum;
import com.project.milo.entity.DataMeter;
import com.project.milo.mapper.DataMeterMapper;
import com.project.milo.service.IDataMeterService;
import com.project.milo.service.IRecordService;
import com.project.milo.utils.StringUtils;
import com.project.milo.utils.poi.EasyExcelUtils;
import com.project.milo.utils.poi.EXGJ;
import com.project.milo.utils.poi.ProgressExcelUtil;
import com.project.milo.websocket.ProgressNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataMeterServiceImpl implements IDataMeterService {

    private static final Logger logger = LoggerFactory.getLogger(DataMeterServiceImpl.class);

    @Autowired
    DataMeterMapper dataMeterMapper;

    @Autowired
    IRecordService recordService;

    @Autowired
    private ProgressNotifier progressNotifier;


    /**
     * 查询列表
     * @return
     */
    @Override
    public List<DataMeter> selectListDataMeter(DataMeter dataMeter) {
        List<DataMeter> list = dataMeterMapper.selectListDataMeter(dataMeter);
        for (DataMeter dataMet : list) {
            if (dataMet.getType().equals("1")){
                dataMet.setType(CityEnum.MIANYANG.getName());
            }else if (dataMet.getType().equals("2")){
                dataMet.setType(CityEnum.DEYANG.getName());
            }else if (dataMet.getType().equals("3")){
                dataMet.setType(CityEnum.GUANGYUAN.getName());
            }else if (dataMet.getType().equals("4")){
                dataMet.setType(CityEnum.LESHAN.getName());
            }else if (dataMet.getType().equals("5")){
                dataMet.setType(CityEnum.ZIGONG.getName());
            }else if (dataMet.getType().equals("6")){
                dataMet.setType(CityEnum.YIBIN.getName());
            }
        }
        return list;
    }

    /**
     * 添加
     * @param dataMeter
     * @return
     */
    @Override
    public int insertDataMeter(DataMeter dataMeter) {
        return dataMeterMapper.insertDataMeter(dataMeter);
    }

    /**
     * 修改查询详情
     * @param id
     * @return
     */
    @Override
    public DataMeter selectDataMeterById(Long id) {
        DataMeter dataMeter = dataMeterMapper.selectDataMeterById(id);
        if (dataMeter.getType().equals("1")){
            dataMeter.setType(CityEnum.MIANYANG.getName());
        }else if (dataMeter.getType().equals("2")){
            dataMeter.setType(CityEnum.DEYANG.getName());
        }else if (dataMeter.getType().equals("3")){
            dataMeter.setType(CityEnum.GUANGYUAN.getName());
        }else if (dataMeter.getType().equals("4")){
            dataMeter.setType(CityEnum.LESHAN.getName());
        }else if (dataMeter.getType().equals("5")){
            dataMeter.setType(CityEnum.ZIGONG.getName());
        }else if (dataMeter.getType().equals("6")){
            dataMeter.setType(CityEnum.YIBIN.getName());
        }
        return dataMeter;
    }

    /**
     * 修改
     * @param
     * @return
     */
    @Override
    public int updateDataMeter(DataMeter dataMeter) {
        if (dataMeter.getType().equals(CityEnum.MIANYANG.getName())){
            dataMeter.setType(CityEnum.MIANYANG.getCode().toString());
        }else if (dataMeter.getType().equals(CityEnum.DEYANG.getName())){
            dataMeter.setType(CityEnum.DEYANG.getCode().toString());
        }else if (dataMeter.getType().equals(CityEnum.GUANGYUAN.getName())){
            dataMeter.setType(CityEnum.GUANGYUAN.getCode().toString());
        }else if (dataMeter.getType().equals(CityEnum.LESHAN.getName())){
            dataMeter.setType(CityEnum.LESHAN.getCode().toString());
        }else if (dataMeter.getType().equals(CityEnum.ZIGONG.getName())){
            dataMeter.setType(CityEnum.ZIGONG.getCode().toString());
        }else if (dataMeter.getType().equals(CityEnum.YIBIN.getName())){
            dataMeter.setType(CityEnum.YIBIN.getCode().toString());
        }
        return dataMeterMapper.updateDataMeter(dataMeter);
    }

    /**
     * 删除
     * @param dataMeter
     * @return
     */
    @Override
    public int deleteDataMeter(DataMeter dataMeter) {
        return dataMeterMapper.deleteDataMeter(dataMeter);
    }

    // ===================== 异步导出（带 WebSocket 进度，由通用工具类 ProgressExcelUtil 实现） =====================

    /**
     * 异步导出列表数据为 Excel（带 WebSocket 实时进度）：
     * 统一交给 {@link ProgressExcelUtil#exportWithProgress} 处理——
     * 统计总数 → 推送进度 → 直接调用封装好的导出工具类 EasyExcelUtils.exportExcel 生成 Excel
     * → 原子重命名 → 推送成功并附带下载地址。
     */
    @Async("ioExecutor")
    @Override
    public void exportDataMeterAsync(String taskId, DataMeter dataMeter) {
        ProgressExcelUtil.exportWithProgress(progressNotifier, taskId,
                "dataMeter_export_", "/api/dataMeter/download/",
                () -> dataMeterMapper.countDataMeter(dataMeter),
                () -> selectListDataMeter(dataMeter),
                (os, list) -> EasyExcelUtils.exportExcel(os, "资料数据列表", "资料数据列表", DataMeter.class, list));
        logger.info("已提交异步导出任务 taskId={}", taskId);
    }

}
