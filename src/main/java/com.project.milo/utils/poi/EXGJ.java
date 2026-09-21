package com.project.milo.utils.poi;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook; // 支持大文件导出，避免 OOM
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用 Excel 导出工具（基于反射 + SXSSFWorkbook）
 */
public class EXGJ {

    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    /**
     * 将数据列表导出为 Excel，并直接通过 HttpServletResponse 返回给客户端
     * @param list       数据列表（实体对象集合）
     * @param clazz      实体类 Class
     * @param columnMap  列映射：key = 实体字段名，value = Excel 列标题（按顺序插入）
     * @param fileName   下载的文件名（不含后缀，自动加 .xlsx）
     * @param response   HttpServletResponse
     */
    public static <T> void exportExcel(List<T> list, Class<T> clazz,
                                       java.util.LinkedHashMap<String, String> columnMap,
                                       String fileName,
                                       HttpServletResponse response) {
        if (list == null || list.isEmpty()) {
            try {
                response.sendError(HttpServletResponse.SC_NO_CONTENT, "无数据可导出");
            } catch (Exception ignored) {}
            return;
        }

        // 创建工作簿（SXSSFWorkbook 支持大数据，内存中只保留100行）
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("Sheet1");

            // 1. 创建表头行
            Row headerRow = sheet.createRow(0);
            List<String> fieldNames = new ArrayList<>(columnMap.keySet());
            int colIndex = 0;
            for (String fieldName : fieldNames) {
                Cell cell = headerRow.createCell(colIndex);
                cell.setCellValue(columnMap.get(fieldName));
                // 设置列宽（字符宽度 * 256）
                sheet.setColumnWidth(colIndex, 20 * 256);
                colIndex++;
            }

            // 2. 填充数据行
            int rowIndex = 1;
            for (T item : list) {
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < fieldNames.size(); i++) {
                    String fieldName = fieldNames.get(i);
                    Object value = getFieldValue(item, fieldName);
                    Cell cell = row.createCell(i);
                    setCellValue(cell, value);
                }
            }

            // 3. 设置响应头并输出
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String encodedFileName = URLEncoder.encode(fileName + ".xlsx", "UTF-8")
                    .replace("+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + encodedFileName);
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
                os.flush();
            }
            // 清除临时文件（SXSSFWorkbook 会生成临时文件，需清理）
            workbook.dispose();

        } catch (Exception e) {
            log.error("Excel 导出失败", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "导出失败");
            } catch (Exception ignored) {}
        }
    }

    // ---------- 反射工具方法 ----------
    private static Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            log.warn("获取字段值失败: {}", fieldName);
            return null;
        }
    }

    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        // 根据类型填充
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof java.util.Date) {
            // 日期格式化
            CellStyle dateStyle = cell.getSheet().getWorkbook().createCellStyle();
            CreationHelper createHelper = cell.getSheet().getWorkbook().getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
            cell.setCellValue((java.util.Date) value);
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue(value.toString());
        }
    }
}
