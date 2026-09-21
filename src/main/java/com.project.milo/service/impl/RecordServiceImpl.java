package com.project.milo.service.impl;

import com.project.milo.constant.RadioEnum;
import com.project.milo.entity.Record;
import com.project.milo.mapper.RecordMapper;
import com.project.milo.service.IRecordService;
import com.project.milo.utils.DateUtils;
import com.project.milo.utils.poi.FastExcelUtil;
import com.project.milo.utils.poi.ProgressExcelUtil;
import com.project.milo.websocket.ProgressMessage;
import com.project.milo.websocket.ProgressNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecordServiceImpl implements IRecordService {

    @Autowired
    private RecordMapper recordMapper;

    @Autowired
    private ProgressNotifier progressNotifier;

    /** 导出文件存放目录（与数据资料模块共用配置） */
    @Value("${milo.export-dir:${user.dir}/export-files}")
    private String exportDir;

    /**
     * 按单位名称关键词搜索单位（全局、去重、用于输入下拉联想，不受当月限制）
     */
    @Override
    public List<Map<String, Object>> searchUnitNames(String name) {
        return recordMapper.searchUnitNames(name);
    }

    /**
     * 查询维护记录*
     *
     * @param record
     * @return
     */
    @Override
    public List<Record> selectListRecord(Record record) {
        return recordMapper.selectListRecord(record);
    }

    /**
     * 删除记录
     *
     * @param record
     * @return
     */
    @Override
    public int deleteRecord(Record record) {
        return recordMapper.deleteRecord(record);
    }

    /**
     * 添加记录
     *
     * @param record
     * @return
     */
    @Override
    public int insertRecord(Record record) {
        if (record.getOp().equals("wr"))
            record.setOp(RadioEnum.WR.getName());
        else if (record.getOp().equals("hyh"))
            record.setOp(RadioEnum.HYH.getName());
        else if (record.getOp().equals("wdy"))
            record.setOp(RadioEnum.WDY.getName());
        else if (record.getOp().equals("cwl"))
            record.setOp(RadioEnum.CWL.getName());
        else if (record.getOp().equals("zzj"))
            record.setOp(RadioEnum.ZZJ.getName());
        return recordMapper.insertRecord(record);
    }

    /**
     * 修改记录
     *
     * @param record
     * @return
     */
    @Override
    public int updateRecord(Record record) {
        if (record.getOp().equals("wr"))
            record.setOp(RadioEnum.WR.getName());
        else if (record.getOp().equals("hyh"))
            record.setOp(RadioEnum.HYH.getName());
        else if (record.getOp().equals("wdy"))
            record.setOp(RadioEnum.WDY.getName());
        else if (record.getOp().equals("cwl"))
            record.setOp(RadioEnum.CWL.getName());
        else if (record.getOp().equals("zzj"))
            record.setOp(RadioEnum.ZZJ.getName());
        return recordMapper.updateRecord(record);
    }

    /**
     * 根据id查询记录
     *
     * @param record
     * @return
     */
    @Override
    public Record selectRecordById(Record record) {
        Record result = recordMapper.selectRecordById(record);
        if (RadioEnum.WR.getName().equals(result.getOp())) {
            result.setOp("wr");
        } else if (RadioEnum.HYH.getName().equals(result.getOp())) {
            result.setOp("hyh");
        } else if (RadioEnum.WDY.getName().equals(result.getOp())) {
            result.setOp("wdy");
        } else if (RadioEnum.CWL.getName().equals(result.getOp())) {
            result.setOp("cwl");
        } else if (RadioEnum.ZZJ.getName().equals(result.getOp())) {
            result.setOp("zzj");
        }
        return result;
    }

    /**
     * 查询大于30天没有维护的单位*
     * @param record
     * @return
     */
    @Override
    public List<Record> selectRecordByNameAndOp(Record record) {
        List<Record> result =  recordMapper.selectRecordByNameAndOp(record);
        Map<String, List<Record>> resultStream =  result.stream().filter(r -> r.getOp() != null).collect(Collectors.groupingBy(Record::getOp));
        List<Record> alllist = new ArrayList<>();
        for (Map.Entry<String, List<Record>> entry : resultStream.entrySet()){
            Record re = new Record();
            re.setName(entry.getKey());
            re.setOrdList(entry.getValue());
            re.setCount(entry.getValue().size());
            alllist.add(re);
        }
        return alllist;

    }

    // ===================== 异步导出（带 WebSocket 进度，由通用工具类 ProgressExcelUtil 实现） =====================

    /**
     * 异步导出维护记录列表为 Excel（带 WebSocket 实时进度）：
     * 统一交给 {@link ProgressExcelUtil#exportWithProgress} 处理——
     * 统计总数 → 推送进度 → 直接调用封装好的导出工具类 FastExcelUtil.export 生成 Excel
     * → 原子重命名 → 推送成功并附带下载地址。
     */
    @Async("ioExecutor")
    @Override
    public void exportRecordAsync(String taskId, Record record) {
        ProgressExcelUtil.exportWithProgress(progressNotifier, taskId,
                "record_export_", "/api/record/download/",
                () -> recordMapper.countRecord(record),
                () -> recordMapper.selectListRecordExport(record),
                (os, list) -> FastExcelUtil.export(os, "维护记录", "维护记录", Record.class, list));
    }

    /**
     * 异步导入维护记录 Excel（带 WebSocket 实时进度）：
     * 统一交给 {@link ProgressExcelUtil#importWithProgress} 处理——
     * 第一遍扫描总行数 → 推送进度 → 逐批调用 recordMapper.batchInsert 入库 → 推送成功/失败结果。
     * 入参为 Controller 在请求线程内 transferTo 落盘的临时文件，处理完毕后在此删除。
     */
    @Async("ioExecutor")
    @Override
    public void importRecordAsync(String taskId, java.io.File file) {
        try {
            ProgressExcelUtil.importWithProgress(progressNotifier, taskId, file, Record.class,
                    batch -> recordMapper.batchInsert(batch), 500);
        } catch (Exception e) {
            // importWithProgress 在解析失败（第一遍计数）时直接抛出，此处补推失败消息
            progressNotifier.send(ProgressMessage.failed(taskId, "IMPORT", "导入失败: " + e.getMessage()));
        } finally {
            if (file != null) {
                try {
                    java.nio.file.Files.deleteIfExists(file.toPath());
                } catch (Exception ignored) {
                    // 忽略临时文件清理失败
                }
            }
        }
    }

}
