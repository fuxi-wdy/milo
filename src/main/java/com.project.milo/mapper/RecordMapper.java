package com.project.milo.mapper;

import com.project.milo.entity.Record;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface RecordMapper {


    /**
     * 查询维护记录
     * * @param record
     *
     * @return
     */
    List<Record> selectListRecord(Record record);


    /**
     * 查询导出
     * * @param record
     * @return
     */
    List<Record> selectListRecordExport(Record record);

    /**
     * 统计导出总行数（与列表查询同条件）
     * * @param record
     *
     * @return 总行数
     */
    long countRecord(Record record);

    /**
     * 按单位名称关键词搜索单位（全局、去重、用于输入下拉联想，不受当月限制）
     * @param name 单位名称关键词
     * @return 单位名称列表（每项含 name 键）
     */
    List<Map<String, Object>> searchUnitNames(String name);

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
     * 批量新增记录（导入用）
     * @param list 记录列表
     * @return
     */
    int batchInsert(List<Record> list);

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


    /**
     * *查询大于30天没有维护的单位
     * @param record
     * @return
     */
    List<Record> selectRecordByNameAndOp(Record record);
}
