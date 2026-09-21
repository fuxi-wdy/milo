package com.project.milo.service;

import com.project.milo.entity.DataMeter;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface IDataMeterService {

    /**
     * 查询列表
     * @return
     */
    List<DataMeter> selectListDataMeter(DataMeter dataMeter);

    /**
     * 添加
     * * @param dataMeter
     * @return
     */
    int insertDataMeter(DataMeter dataMeter);


    /**
     * 修改查询详情
     * @param id
     * @return
     */
    DataMeter selectDataMeterById(Long id);


    /**
     * 修改
     * @param
     * @return
     */
    int updateDataMeter(DataMeter dataMeter);


    /**
     * 删除
     * @param dataMeter
     * @return
     */
    int deleteDataMeter(DataMeter dataMeter);


    /**
     * 异步导出列表数据为 Excel（带 WebSocket 实时进度）
     * 接口立即返回，任务在 ioExecutor 线程池后台执行，进度通过 /ws/progress 推送，
     * 完成后通过进度消息下发下载地址。
     * @param taskId 任务ID（由调用方生成，用于进度消息匹配）
     * @param dataMeter 查询条件（name / type / concat）
     */
    void exportDataMeterAsync(String taskId, DataMeter dataMeter);



}
