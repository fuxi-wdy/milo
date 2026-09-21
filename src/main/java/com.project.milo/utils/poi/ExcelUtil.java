package com.project.milo.utils.poi;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.project.milo.common.AjaxResult;
import com.project.milo.constant.Type;
import com.project.milo.utils.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用Excel导入导出工具类（基于EasyExcel）
 * 需在项目中引入：com.alibaba:easyexcel:4.0.3
 *
 * @author YourName
 * @date 2026-08-19
 */
public class ExcelUtil<T> {

    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    public static final String FORMULA_REGEX_STR = "=|-|\\+|@";

    public static final String[] FORMULA_STR = { "=", "-", "+", "@" };

    /**
     * 用于dictType属性数据存储，避免重复查缓存
     */
    public Map<String, String> sysDictMap = new HashMap<String, String>();

    /**
     * Excel sheet最大行数，默认65536
     */
    public static final int sheetSize = 65536;

    /**
     * 工作表名称
     */
    private String sheetName;

    /**
     * 导出类型（EXPORT:导出数据；IMPORT：导入模板）
     */
    private Type type;

    /**
     * 工作薄对象
     */
    private Workbook wb;

    /**
     * 工作表对象
     */
    private Sheet sheet;

    /**
     * 样式列表
     */
    private Map<String, CellStyle> styles;

    /**
     * 导入导出数据列表
     */
    private List<T> list;

    /**
     * 注解列表
     */
    private List<Object[]> fields;

    /**
     * 当前行号
     */
    private int rownum;

    /**
     * 标题
     */
    private String title;

    /**
     * 最大高度
     */
    private short maxHeight;

    /**
     * 合并后最后行数
     */
    private int subMergedLastRowNum = 0;

    /**
     * 合并后开始行数
     */
    private int subMergedFirstRowNum = 1;

    /**
     * 对象的子列表方法
     */
    private Method subMethod;

    /**
     * 对象的子列表属性
     */
    private List<Field> subFields;

    /**
     * 统计列表
     */
    private Map<Integer, Double> statistics = new HashMap<Integer, Double>();

    /**
     * 数字格式
     */
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("######0.00");

    /**
     * 实体对象
     */
    public Class<T> clazz;

    /**
     * 需要排除列属性
     */
    public String[] excludeFields;

    public ExcelUtil(Class<T> clazz)
    {
        this.clazz = clazz;
    }

    /**
     * 隐藏Excel中列属性
     *
     * @param fields 列属性名 示例[单个"name"/多个"id","name"]
     * @throws Exception
     */
    public void hideColumn(String... fields)
    {
        this.excludeFields = fields;
    }







    /**
     * 导出Excel（普通列表）
     *
     * @param response   HttpServletResponse
     * @param data       数据列表
     * @param clazz      导出VO的Class（需标注@ExcelProperty）
     * @param sheetName  Sheet名称
     * @param fileName   导出文件名（不含后缀）
     */
    public static <T> void export(HttpServletResponse response, List<T> data,
                                  Class<T> clazz, String sheetName, String fileName) throws IOException {
        setResponseHeader(response, fileName + ".xlsx");
        // 样式策略（可选，设置表头加粗居中，内容左对齐）
        WriteHandler styleStrategy = getDefaultStyleStrategy();
        EasyExcel.write(response.getOutputStream(), clazz)
                .sheet(sheetName)
                .registerWriteHandler(styleStrategy)
                .doWrite(data);
    }

    public AjaxResult export1(HttpServletResponse response, List<T> data, String fileName) {

        try {
            setResponseHeader(response, fileName + ".xlsx");
            // 样式策略（可选，设置表头加粗居中，内容左对齐）
            WriteHandler styleStrategy = getDefaultStyleStrategy();
            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet(sheetName)
                    .registerWriteHandler(styleStrategy)
                    .doWrite(data);
        } catch (IOException e) {
            log.error("导出Excel异常{}", e.getMessage());
            return AjaxResult.error("导出Excel异常：" + e.getMessage());
        }finally {
        }
        return AjaxResult.success();
    }










    /**
     * 导出大数据（分批从外部获取数据，避免OOM）
     *
     * @param response   HttpServletResponse
     * @param clazz      导出VO的Class
     * @param sheetName  Sheet名称
     * @param fileName   文件名
     * @param pageFetcher 分页获取数据的函数，参数为页码，返回该页数据列表（可为空）
     * @param pageSize   每页大小
     */
    public static <T> void exportBigData(HttpServletResponse response,
                                         Class<T> clazz,
                                         String sheetName,
                                         String fileName,
                                         Function<Integer, List<T>> pageFetcher,
                                         int pageSize) throws IOException {
        setResponseHeader(response, fileName + ".xlsx");
        try (var excelWriter = EasyExcel.write(response.getOutputStream(), clazz)
                .registerWriteHandler(getDefaultStyleStrategy())
                .build()) {
            var writeSheet = EasyExcel.writerSheet(sheetName).build();
            int pageNum = 1;
            List<T> pageData;
            do {
                pageData = pageFetcher.apply(pageNum);
                if (pageData != null && !pageData.isEmpty()) {
                    excelWriter.write(pageData, writeSheet);
                    pageNum++;
                }
            } while (pageData != null && !pageData.isEmpty());
        }
    }

    /**
     * 导入Excel（带JSR-303校验，返回成功数据和错误信息）
     *
     * @param file     上传的文件
     * @param clazz    导入VO的Class（需@ExcelProperty + 校验注解）
     * @param validator Spring的校验器（注入LocalValidatorFactoryBean）
     * @param handler  处理每一条有效数据的回调（如保存到数据库）
     * @param <T>      导入VO类型
     * @return 导入结果：包含成功条数、错误列表等
     */
    public static <T> ImportResult<T> importExcel(MultipartFile file,
                                                  Class<T> clazz,
                                                  Validator validator,
                                                  ImportHandler<T> handler) throws IOException {
        ImportListener<T> listener = new ImportListener<>(validator, handler);
        EasyExcel.read(file.getInputStream(), clazz, listener)
                .sheet()
                .doRead();
        return listener.getResult();
    }

    // ==================== 内部辅助方法 ====================

    /** 设置响应头（处理中文乱码） */
    private static void setResponseHeader(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encoded = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encoded);
    }

    /** 默认样式：表头蓝底白字加粗居中，内容左对齐 */
    private static WriteHandler getDefaultStyleStrategy() {
        WriteCellStyle headStyle = new WriteCellStyle();
        headStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 表头字体加粗等可在headStyle中设置，此处省略

        WriteCellStyle contentStyle = new WriteCellStyle();
        contentStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        return new HorizontalCellStyleStrategy(headStyle, contentStyle);
    }



    // ==================== 内部监听器 ====================

    private static class ImportListener<T> extends AnalysisEventListener<T> {
        private final Validator validator;
        private final ImportHandler<T> handler;
        private final List<T> successList = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();
        private int totalRows = 0;

        public ImportListener(Validator validator, ImportHandler<T> handler) {
            this.validator = validator;
            this.handler = handler;
        }

        @Override
        public void invoke(T data, AnalysisContext context) {
            totalRows++;
            int rowIndex = context.readRowHolder().getRowIndex() + 1; // 行号从1开始
            // 1. JSR-303校验
            Set<ConstraintViolation<T>> violations = validator.validate(data);
            if (!violations.isEmpty()) {
                String msg = violations.stream()
                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                        .collect(Collectors.joining("; "));
                errorMessages.add(String.format("第%d行: %s", rowIndex, msg));
                return;
            }
            // 2. 自定义校验（如果handler有额外校验）
            String customError = handler.validate(data);
            if (customError != null) {
                errorMessages.add(String.format("第%d行: %s", rowIndex, customError));
                return;
            }
            // 3. 有效数据，加入成功列表
            successList.add(data);
            // 如果需批量入库，可在handler中处理，或者外部遍历successList
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // 全部读取完成后，回调处理成功数据（例如批量入库）
            if (!successList.isEmpty()) {
                handler.handle(successList);
            }
        }

        public ImportResult<T> getResult() {
            return new ImportResult<>(totalRows, successList, errorMessages);
        }
    }

    // ==================== 导入结果封装 ====================

    public static class ImportResult<T> {
        private final int totalRows;
        private final List<T> successData;
        private final List<String> errors;

        public ImportResult(int totalRows, List<T> successData, List<String> errors) {
            this.totalRows = totalRows;
            this.successData = successData;
            this.errors = errors;
        }

        public int getTotalRows() { return totalRows; }
        public List<T> getSuccessData() { return successData; }
        public List<String> getErrors() { return errors; }
        public boolean hasErrors() { return !errors.isEmpty(); }
        public int getSuccessCount() { return successData.size(); }
    }

    // ==================== 导入处理器接口（用户实现） ====================

    @FunctionalInterface
    public interface ImportHandler<T> {
        /**
         * 处理有效数据（如批量插入数据库）
         */
        void handle(List<T> validData);

        /**
         * 可选：对单行数据进行额外校验，返回错误信息，无则返回null
         */
        default String validate(T data) {
            return null;
        }
    }
}