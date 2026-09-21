package com.project.milo.utils.poi;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EasyExcelUtils {

    /**
     * 构建单元格样式：细边框 + 水平垂直居中 + 宋体11号不加粗（无背景色）
     * 表头与正文共用该样式，保证导出文档表头和正文内容样式一致
     */
    public static WriteCellStyle buildCellStyle() {
        WriteCellStyle cellStyle = new WriteCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        WriteFont font = new WriteFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 12);
        font.setBold(false);  // 不加粗
        cellStyle.setWriteFont(font);
        return cellStyle;
    }

    /**
     * 构建样式策略：表头与正文内容使用相同样式
     */
    public static HorizontalCellStyleStrategy buildStyleStrategy() {
        WriteCellStyle headStyle = buildCellStyle();
        WriteCellStyle contentStyle = buildCellStyle();
        return new HorizontalCellStyleStrategy(headStyle, contentStyle);
    }

    /*小数量导出*/
    public static <T> void exportExcel(HttpServletResponse response, String fileName, String sheetName, Class<T> clazz, List<T> data) throws IOException {

        String encodingFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + encodingFileName + ".xlsx");
        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(buildCellStyle(), buildCellStyle());
        EasyExcel.write(response.getOutputStream(), clazz).registerWriteHandler(horizontalCellStyleStrategy).sheet(sheetName).doWrite(data);
    }

    /*小数量导出（写入指定输出流，供异步导出落盘等场景复用；样式与写法与 response 版完全一致）*/
    public static <T> void exportExcel(OutputStream outputStream, String fileName, String sheetName, Class<T> clazz, List<T> data) {
        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(buildCellStyle(), buildCellStyle());
        EasyExcel.write(outputStream, clazz).registerWriteHandler(horizontalCellStyleStrategy).sheet(sheetName).doWrite(data);
    }


    /*上百万条的大数量导出*/
    @FunctionalInterface
    public interface PageQueryFunction<T> {
        /**
         * 分页查询数据
         * @param pageNum 页码（从1开始）
         * @param pageSize 每页条数
         * @return 当前页数据列表
         */
        List<T> query(int pageNum, int pageSize);
    }

    /**
     * 分批流式导出Excel（生产推荐）
     * @param response 响应对象
     * @param fileName 导出文件名（不带后缀）
     * @param sheetName sheet名称
     * @param clazz 导出VO类
     * @param pageSize 分批大小（建议1000-5000条，避免OOM）
     * @param queryFunction 分页查询逻辑（lambda实现）
     */
    public static <T> void exportBatch(HttpServletResponse response,
                                       String fileName,
                                       String sheetName,
                                       Class<T> clazz,
                                       int pageSize,
                                       PageQueryFunction<T> queryFunction) throws IOException {
        // 1. 响应头配置（浏览器下载）
        String encodeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + encodeFileName + ".xlsx");

        // 2. 表头与正文内容样式一致
        HorizontalCellStyleStrategy styleStrategy = new HorizontalCellStyleStrategy(buildCellStyle(), buildCellStyle());

        // 3. 初始化Excel写入器（流式模式，全程不缓存全量数据）
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), clazz)
                .registerWriteHandler(styleStrategy)
                .build()) {

            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
            int pageNum = 1;
            List<T> dataList;

            // 4. 循环分页查询+写入，直到无数据
            while ((dataList = queryFunction.query(pageNum, pageSize)) != null && !dataList.isEmpty()) {
                // 写入当前批数据（首次自动写表头，后续仅追加数据）
                excelWriter.write(dataList, writeSheet);
                pageNum++;
            }
        }
        // 5. 自动关闭流，无需手动处理
    }


}
