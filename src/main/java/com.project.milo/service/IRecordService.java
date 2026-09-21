package com.project.milo.service;

import com.project.milo.entity.Record;

import java.util.List;
import java.util.Map;

public interface  IRecordService {

    /**
     * 按单位名称关键词搜索单位（全局、去重、用于输入下拉联想，不受当月限制）
     * @param name 单位名称关键词
     * @return 单位名称列表（每项含 name 键）
     */
    List<Map<String, Object>> searchUnitNames(String name);


    /**
     * 查询维护记录
     * * @param record
     *
     * @return
     */
    List<Record> selectListRecord(Record record);


    /**
     * 删除记录
     * * @param record
     * @return
     */
    int deleteRecord(Record record);


    /**
     * 添加记录
     * * @param record
     * @return
     */
    int insertRecord(Record record);

    /**
     * 修改记录
     * * @param record
     * @return
     */
    int updateRecord(Record record);


    /**
     * 根据id查询记录
     * * @param record
     * @return
     */
    Record selectRecordById(Record record);


    List<Record> selectRecordByNameAndOp(Record record);


    /**
     * 异步导出维护记录列表为 Excel（带 WebSocket 实时进度）
     * 接口立即返回，任务在 ioExecutor 线程池后台执行，进度通过 /ws/progress 推送，
     * 完成后通过进度消息下发下载地址。
     * @param taskId 任务ID（由调用方生成，用于进度消息匹配）
     * @param record 查询条件（name / op / params.startDate / params.endDate）
     */
    void exportRecordAsync(String taskId, Record record);

    /**
     * 异步导入维护记录为 Excel（带 WebSocket 实时进度，逐批入库）
     * 接口立即返回，任务在 ioExecutor 线程池后台执行，进度通过 /ws/progress 推送，
     * 完成后通过进度消息下发结果（成功/失败条数）。
     * @param taskId 任务ID（由调用方生成，用于进度消息匹配）
     * @param file   已转存到本地的 Excel 文件（Controller 在请求线程内 transferTo 生成，
     *               异步方法处理完毕后负责删除）
     */
    void importRecordAsync(String taskId, java.io.File file);

}
