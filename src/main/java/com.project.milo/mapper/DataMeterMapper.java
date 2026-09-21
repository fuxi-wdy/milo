package com.project.milo.mapper;


import com.project.milo.entity.DataMeter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DataMeterMapper {



    /**
     * 查询列表
     * @return
     */
    List<DataMeter> selectListDataMeter(DataMeter dataMeter);


    /**
     * 统计导出总行数（与列表查询同条件）
     * @param dataMeter 查询条件
     * @return 总行数
     */
    long countDataMeter(DataMeter dataMeter);

    /**
     * 导出分批查询：按主键键集分页
     * @param cond 查询条件
     * @param lastId 上一批最大 scId（首查传 0）
     * @param pageSize 每批行数
     * @return 当前批数据
     */
    List<DataMeter> selectDataMeterExportPage(@Param("cond") DataMeter cond, @Param("lastId") Long lastId, @Param("pageSize") int pageSize);


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



}
