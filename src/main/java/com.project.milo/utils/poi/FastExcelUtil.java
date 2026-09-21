package com.project.milo.utils.poi;

import cn.idev.excel.FastExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.event.AnalysisEventListener;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import cn.idev.excel.support.ExcelTypeEnum;
import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.metadata.style.WriteFont;
import cn.idev.excel.write.style.HorizontalCellStyleStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * FastExcel 导入导出工具类
 *
 * @author yourname
 * @date 2026-08-28
 */
@Slf4j
public class FastExcelUtil {

    /**
     * 默认批次处理大小
     */
    private static final int DEFAULT_BATCH_SIZE = 1000;

    /**
     * JSON 转换器
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 获取 Map 转换器实例（单例）
     */
    private static final MapConverter MAP_CONVERTER = new MapConverter();

    /**
     * 样式策略（单例，避免重复创建）
     */
    private static final HorizontalCellStyleStrategy STYLE_STRATEGY = buildStyleStrategy();

    /**
     * 构建表头样式：加粗 + 宋体11号 + 浅灰色背景 + 细边框
     */
    private static WriteCellStyle buildHeadCellStyle() {
        WriteCellStyle headStyle = new WriteCellStyle();

        // 字体设置
        WriteFont headFont = new WriteFont();
        headFont.setBold(true);              // 加粗
        headFont.setFontName("宋体");
        headFont.setFontHeightInPoints((short) 12);
        headStyle.setWriteFont(headFont);

        // 背景色：浅灰色
        headStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);

        // 边框：细边框
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);

        // 水平垂直居中
        headStyle.setHorizontalAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);

        return headStyle;
    }

    /**
     * 构建内容样式：宋体11号不加粗 + 细边框
     */
    private static WriteCellStyle buildContentCellStyle() {
        WriteCellStyle contentStyle = new WriteCellStyle();

        // 字体设置（不加粗）
        WriteFont contentFont = new WriteFont();
        contentFont.setBold(false);
        contentFont.setFontName("宋体");
        contentFont.setFontHeightInPoints((short) 12);
        contentStyle.setWriteFont(contentFont);

        // 边框：细边框（与表头保持一致）
        contentStyle.setBorderBottom(BorderStyle.THIN);
        contentStyle.setBorderTop(BorderStyle.THIN);
        contentStyle.setBorderLeft(BorderStyle.THIN);
        contentStyle.setBorderRight(BorderStyle.THIN);

        // 水平垂直居中
        contentStyle.setHorizontalAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        contentStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);

        return contentStyle;
    }

    /**
     * 构建样式策略：表头与正文内容使用相同样式（表头加粗，内容不加粗）
     */
    private static HorizontalCellStyleStrategy buildStyleStrategy() {
        WriteCellStyle headStyle = buildHeadCellStyle();
        WriteCellStyle contentStyle = buildContentCellStyle();
        return new HorizontalCellStyleStrategy(headStyle, contentStyle);
    }

    /**
     * 导出 Excel（同步导出，一次性写入所有数据）
     * 自动注册 Map 转换器，自动应用统一样式
     *
     * @param response   HttpServletResponse
     * @param fileName   文件名（不含后缀）
     * @param sheetName  工作表名称
     * @param clazz      数据模型类
     * @param dataList   数据列表
     * @param <T>        数据类型
     */
    public static <T> void export(HttpServletResponse response,
                                  String fileName,
                                  String sheetName,
                                  Class<T> clazz,
                                  List<T> dataList) throws IOException {
        try {
            // 设置响应头
            setResponseHeader(response, fileName);

            // 执行导出：注册 Map 转换器 + 样式策略
            FastExcel.write(response.getOutputStream(), clazz)
                    .excelType(ExcelTypeEnum.XLSX)
                    .registerConverter(MAP_CONVERTER)
                    .registerWriteHandler(STYLE_STRATEGY)
                    .sheet(sheetName)
                    .doWrite(dataList);

            log.info("导出成功：{}，共 {} 条数据", fileName, dataList.size());
        } catch (Exception e) {
            log.error("导出失败：{}", e.getMessage(), e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }


    /**
     * 导出 Excel（写入指定输出流，供异步导出落盘等场景复用；样式与写法与 response 版完全一致）
     * 自动注册 Map 转换器，自动应用统一样式
     *
     * @param outputStream 输出流
     * @param fileName     文件名（不含后缀）
     * @param sheetName    工作表名称
     * @param clazz        数据模型类
     * @param dataList     数据列表
     * @param <T>          数据类型
     */
    public static <T> void export(OutputStream outputStream,
                                  String fileName,
                                  String sheetName,
                                  Class<T> clazz,
                                  List<T> dataList) {
        FastExcel.write(outputStream, clazz)
                .excelType(ExcelTypeEnum.XLSX)
                .registerConverter(MAP_CONVERTER)
                .registerWriteHandler(STYLE_STRATEGY)
                .sheet(sheetName)
                .doWrite(dataList);
        log.info("导出成功：{}，共 {} 条数据", fileName, dataList.size());
    }
    /**
     * 导出 Excel（排除指定字段）
     *
     * @param response           HttpServletResponse
     * @param fileName           文件名（不含后缀）
     * @param sheetName          工作表名称
     * @param clazz              数据模型类
     * @param dataList           数据列表
     * @param excludeFieldNames  需要排除的字段名
     * @param <T>                数据类型
     */
    public static <T> void exportWithExclude(HttpServletResponse response,
                                             String fileName,
                                             String sheetName,
                                             Class<T> clazz,
                                             List<T> dataList,
                                             String... excludeFieldNames) throws IOException {
        try {
            setResponseHeader(response, fileName);

            FastExcel.write(response.getOutputStream(), clazz)
                    .excelType(ExcelTypeEnum.XLSX)
                    .registerConverter(MAP_CONVERTER)
                    .registerWriteHandler(STYLE_STRATEGY)
                    .excludeColumnFieldNames(List.of(excludeFieldNames))
                    .sheet(sheetName)
                    .doWrite(dataList);

            log.info("导出成功：{}，共 {} 条数据，排除字段：{}", fileName, dataList.size(),
                    String.join(", ", excludeFieldNames));
        } catch (Exception e) {
            log.error("导出失败：{}", e.getMessage(), e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 导出 Excel（只导出指定字段）
     *
     * @param response           HttpServletResponse
     * @param fileName           文件名（不含后缀）
     * @param sheetName          工作表名称
     * @param clazz              数据模型类
     * @param dataList           数据列表
     * @param includeFieldNames  需要导出的字段名
     * @param <T>                数据类型
     */
    public static <T> void exportWithInclude(HttpServletResponse response,
                                             String fileName,
                                             String sheetName,
                                             Class<T> clazz,
                                             List<T> dataList,
                                             String... includeFieldNames) throws IOException {
        try {
            setResponseHeader(response, fileName);

            FastExcel.write(response.getOutputStream(), clazz)
                    .excelType(ExcelTypeEnum.XLSX)
                    .registerConverter(MAP_CONVERTER)
                    .registerWriteHandler(STYLE_STRATEGY)
                    .includeColumnFieldNames(Arrays.asList(includeFieldNames))
                    .sheet(sheetName)
                    .doWrite(dataList);

            log.info("导出成功：{}，共 {} 条数据，导出字段：{}", fileName, dataList.size(),
                    String.join(", ", includeFieldNames));
        } catch (Exception e) {
            log.error("导出失败：{}", e.getMessage(), e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 导出 Excel（分页查询导出，适用于大数据量）
     * 自动注册 Map 转换器，自动应用统一样式
     *
     * @param response     HttpServletResponse
     * @param fileName     文件名（不含后缀）
     * @param sheetName    工作表名称
     * @param clazz        数据模型类
     * @param pageFetcher  分页查询函数，参数为页码（从0开始），返回该页数据列表
     * @param totalPages   总页数
     * @param <T>          数据类型
     */
    public static <T> void exportByPage(HttpServletResponse response,
                                        String fileName,
                                        String sheetName,
                                        Class<T> clazz,
                                        Function<Integer, List<T>> pageFetcher,
                                        int totalPages) throws IOException {
        try {
            setResponseHeader(response, fileName);

            // 使用 try-with-resources 自动关闭
            try (OutputStream outputStream = response.getOutputStream()) {
                var excelWriter = FastExcel.write(outputStream, clazz)
                        .excelType(ExcelTypeEnum.XLSX)
                        .registerConverter(MAP_CONVERTER)
                        .registerWriteHandler(STYLE_STRATEGY)
                        .build();

                var writeSheet = FastExcel.writerSheet(sheetName).build();

                // 分页写入
                for (int page = 0; page < totalPages; page++) {
                    List<T> pageData = pageFetcher.apply(page);
                    if (pageData != null && !pageData.isEmpty()) {
                        excelWriter.write(pageData, writeSheet);
                    }
                    // 每页写入后释放内存
                    pageData = null;
                }

                excelWriter.finish();
                log.info("分页导出成功：{}，共 {} 页", fileName, totalPages);
            }
        } catch (Exception e) {
            log.error("分页导出失败：{}", e.getMessage(), e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 导出 Excel（动态表头，支持 Map 数据）
     *
     * @param response   HttpServletResponse
     * @param fileName   文件名（不含后缀）
     * @param sheetName  工作表名称
     * @param headers    表头列表
     * @param dataList   Map 数据列表，每个 Map 代表一行
     */
    public static void exportWithMap(HttpServletResponse response,
                                     String fileName,
                                     String sheetName,
                                     List<String> headers,
                                     List<Map<String, Object>> dataList) throws IOException {
        try {
            setResponseHeader(response, fileName);

            // 构建表头
            List<List<String>> head = new ArrayList<>();
            for (String header : headers) {
                head.add(List.of(header));
            }

            // 转换数据为 List<List<Object>>
            List<List<Object>> data = new ArrayList<>();
            for (Map<String, Object> row : dataList) {
                List<Object> rowData = new ArrayList<>();
                for (String header : headers) {
                    rowData.add(row.get(header));
                }
                data.add(rowData);
            }

            // 导出（不使用实体类，直接导出数据）
            FastExcel.write(response.getOutputStream())
                    .head(head)
                    .excelType(ExcelTypeEnum.XLSX)
                    .registerWriteHandler(STYLE_STRATEGY)
                    .sheet(sheetName)
                    .doWrite(data);

            log.info("动态导出成功：{}，共 {} 条数据", fileName, data.size());
        } catch (Exception e) {
            log.error("导出失败：{}", e.getMessage(), e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 导出 Excel（使用实体类 + 自定义表头）
     * 自动注册 Map 转换器，自动应用统一样式
     *
     * @param response   HttpServletResponse
     * @param fileName   文件名（不含后缀）
     * @param sheetName  工作表名称
     * @param clazz      数据模型类
     * @param head       自定义表头
     * @param dataList   数据列表
     * @param <T>        数据类型
     */
    public static <T> void exportWithCustomHead(HttpServletResponse response,
                                                String fileName,
                                                String sheetName,
                                                Class<T> clazz,
                                                List<List<String>> head,
                                                List<T> dataList) throws IOException {
        try {
            setResponseHeader(response, fileName);

            // 执行导出，使用自定义表头
            FastExcel.write(response.getOutputStream(), clazz)
                    .head(head)
                    .excelType(ExcelTypeEnum.XLSX)
                    .registerConverter(MAP_CONVERTER)
                    .registerWriteHandler(STYLE_STRATEGY)
                    .sheet(sheetName)
                    .doWrite(dataList);

            log.info("自定义表头导出成功：{}，共 {} 条数据", fileName, dataList.size());
        } catch (Exception e) {
            log.error("导出失败：{}", e.getMessage(), e);
            throw new IOException("导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 导入 Excel（同步导入，一次性读取所有数据到内存）
     * 注意：仅适用于小文件（< 1000行）
     *
     * @param file  上传的文件
     * @param clazz 数据模型类
     * @param <T>   数据类型
     * @return 数据列表
     */
    public static <T> List<T> importSync(MultipartFile file, Class<T> clazz) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return FastExcel.read(inputStream)
                    .head(clazz)
                    .excelType(ExcelTypeEnum.XLSX)
                    .registerConverter(MAP_CONVERTER)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            log.error("同步导入失败：{}", e.getMessage(), e);
            throw new IOException("导入失败：" + e.getMessage(), e);
        }
    }

    /**
     * 导入 Excel（异步监听器模式，逐行处理，适合大文件）
     *
     * @param file           上传的文件
     * @param clazz          数据模型类
     * @param batchProcessor 批次处理器，参数为数据列表，返回是否继续读取（true=继续，false=停止）
     * @param <T>            数据类型
     * @return 总处理条数
     */
    public static <T> int importAsync(MultipartFile file,
                                      Class<T> clazz,
                                      BatchProcessor<T> batchProcessor) throws IOException {
        return importAsync(file, clazz, batchProcessor, DEFAULT_BATCH_SIZE);
    }

    /**
     * 导入 Excel（异步监听器模式，逐行处理，适合大文件）
     *
     * @param file           上传的文件
     * @param clazz          数据模型类
     * @param batchProcessor 批次处理器
     * @param batchSize      批次大小
     * @param <T>            数据类型
     * @return 总处理条数
     */
    public static <T> int importAsync(MultipartFile file,
                                      Class<T> clazz,
                                      BatchProcessor<T> batchProcessor,
                                      int batchSize) throws IOException {
        // 计数器
        final int[] totalCount = {0};
        final boolean[] shouldContinue = {true};

        try (InputStream inputStream = file.getInputStream()) {
            FastExcel.read(inputStream, clazz, new AnalysisEventListener<T>() {
                        private final List<T> cacheList = new ArrayList<>(batchSize);

                        @Override
                        public void invoke(T data, AnalysisContext context) {
                            if (!shouldContinue[0]) {
                                return;
                            }

                            cacheList.add(data);

                            if (cacheList.size() >= batchSize) {
                                boolean result = batchProcessor.process(cacheList);
                                totalCount[0] += cacheList.size();
                                cacheList.clear();

                                if (!result) {
                                    shouldContinue[0] = false;
                                }
                            }
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                            if (shouldContinue[0] && !cacheList.isEmpty()) {
                                batchProcessor.process(cacheList);
                                totalCount[0] += cacheList.size();
                                cacheList.clear();
                            }
                            log.info("异步导入完成，共处理 {} 条数据", totalCount[0]);
                        }
                    })
                    .registerConverter(MAP_CONVERTER)
                    .sheet().doRead();

            return totalCount[0];
        } catch (Exception e) {
            log.error("异步导入失败：{}", e.getMessage(), e);
            throw new IOException("导入失败：" + e.getMessage(), e);
        }
    }

    /**
     * 导入 Excel（流式处理，每行单独处理）
     *
     * @param file        上传的文件
     * @param clazz       数据模型类
     * @param rowConsumer 行处理器，每解析一行调用一次
     * @param <T>         数据类型
     * @return 总处理条数
     */
    public static <T> int importStream(MultipartFile file,
                                       Class<T> clazz,
                                       Consumer<T> rowConsumer) throws IOException {
        final int[] count = {0};

        try (InputStream inputStream = file.getInputStream()) {
            FastExcel.read(inputStream, clazz, new AnalysisEventListener<T>() {
                        @Override
                        public void invoke(T data, AnalysisContext context) {
                            rowConsumer.accept(data);
                            count[0]++;
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                            log.info("流式导入完成，共处理 {} 条数据", count[0]);
                        }
                    })
                    .registerConverter(MAP_CONVERTER)
                    .sheet().doRead();

            return count[0];
        } catch (Exception e) {
            log.error("流式导入失败：{}", e.getMessage(), e);
            throw new IOException("导入失败：" + e.getMessage(), e);
        }
    }

    /**
     * 设置导出响应头
     */
    private static void setResponseHeader(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 处理中文文件名
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition",
                "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
    }

    /**
     * 批次处理器接口
     */
    @FunctionalInterface
    public interface BatchProcessor<T> {
        /**
         * 处理一批数据
         * @param batch 数据批次
         * @return true=继续处理下一批，false=停止读取
         */
        boolean process(List<T> batch);
    }

    /**
     * Map 类型转换器（内部类）
     * 将 Map 与 JSON 字符串互转
     */
    private static class MapConverter implements Converter<Map<String, Object>> {

        @Override
        public Class<?> supportJavaTypeKey() {
            return Map.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        /**
         * 从 Excel 读取数据转换为 Map
         */
        @Override
        public Map<String, Object> convertToJavaData(ReadCellData<?> cellData,
                                                     ExcelContentProperty contentProperty,
                                                     GlobalConfiguration globalConfiguration) {
            String jsonStr = cellData.getStringValue();
            if (jsonStr == null || jsonStr.isEmpty()) {
                return new HashMap<>();
            }
            try {
                return OBJECT_MAPPER.readValue(jsonStr, Map.class);
            } catch (JsonProcessingException e) {
                log.error("解析 JSON 失败：{}", jsonStr, e);
                return new HashMap<>();
            }
        }

        /**
         * 将 Map 转换为 Excel 数据
         */
        @Override
        public WriteCellData<?> convertToExcelData(Map<String, Object> value,
                                                   ExcelContentProperty contentProperty,
                                                   GlobalConfiguration globalConfiguration) {
            if (value == null || value.isEmpty()) {
                return new WriteCellData<>("");
            }
            try {
                String jsonStr = OBJECT_MAPPER.writeValueAsString(value);
                return new WriteCellData<>(jsonStr);
            } catch (JsonProcessingException e) {
                log.error("序列化 Map 失败：{}", value, e);
                return new WriteCellData<>(value.toString());
            }
        }
    }
}
