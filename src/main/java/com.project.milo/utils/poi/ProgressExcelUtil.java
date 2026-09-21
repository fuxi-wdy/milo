package com.project.milo.utils.poi;

import cn.idev.excel.FastExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import com.project.milo.websocket.ProgressMessage;
import com.project.milo.websocket.ProgressNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * 带 WebSocket 实时进度的 Excel 导入导出工具类（供各模块复用）
 *
 * <p>后端异步导出只需一行（方法需运行在 @Async 线程中，文件自动保存到系统临时目录）：</p>
 * <pre>
 * ProgressExcelUtil.exportWithProgress(progressNotifier, taskId,
 *         "record_export_", "/api/record/download/",
 *         () -&gt; recordMapper.countRecord(record),
 *         () -&gt; recordMapper.selectListRecord(record),
 *         (os, list) -&gt; FastExcelUtil.export(os, "维护记录", "维护记录", Record.class, list));
 * </pre>
 *
 * <p>后端异步导入（逐批入库 + 实时进度）：</p>
 * <pre>
 * ProgressExcelUtil.importWithProgress(progressNotifier, taskId, file, Record.class,
 *         batch -&gt; recordMapper.batchInsert(batch), 500);
 * </pre>
 *
 * <p>Controller 下载复用（下载完成后服务器文件自动删除）：</p>
 * <pre>
 * ProgressExcelUtil.writeDownload(response, fileName, "record_export_", "维护记录导出");
 * </pre>
 */
public class ProgressExcelUtil {

    private static final Logger log = LoggerFactory.getLogger(ProgressExcelUtil.class);

    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private ProgressExcelUtil() {
    }

    /** Excel 写入器：把数据列表写入指定输出流（调用方在此对接各自的封装导出工具类） */
    @FunctionalInterface
    public interface ExcelWriter<T> {
        void write(OutputStream outputStream, List<T> data) throws Exception;
    }

    /** 导入结果 */
    public static class ImportResult {
        public final long total;
        public final long success;
        public final long failed;

        public ImportResult(long total, long success, long failed) {
            this.total = total;
            this.success = success;
            this.failed = failed;
        }
    }

    // ===================== 带进度条导出 =====================

    /**
     * 导出文件存放目录：直接使用系统临时目录（无需任何配置）。
     * 导出文件仅用于本次下载，下载完成后即被删除，不会在服务器长期保存。
     */
    public static Path getExportDir() {
        Path dir = Path.of(System.getProperty("java.io.tmpdir"), "milo-export").toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {
            // 目录创建失败时由后续写入报错
        }
        return dir;
    }

    /**
     * 带 WebSocket 实时进度的异步导出：
     * 统计总数 → 推送 start → 调用调用方提供的 ExcelWriter 写入 .part 临时文件 → 原子重命名为正式文件
     * → 推送 running → 推送 success（附带下载地址）；任何异常推送 failed，finally 清理 .part。
     *
     * <p>文件保存在系统临时目录（见 {@link #getExportDir()}），下载完成后由下载接口删除，无需配置导出目录。</p>
     *
     * @param notifier          进度推送器
     * @param taskId            导出任务ID（前端生成，用于进度消息匹配）
     * @param filePrefix        生成文件名前缀，如 "record_export_"（最终 record_export_yyyyMMddHHmmss.xlsx）
     * @param downloadUrlPrefix 下载地址前缀，如 "/api/record/download/"
     * @param countSupplier     总数统计（与列表查询同条件）
     * @param dataSupplier      全量数据查询（条件与列表一致）
     * @param writer            Excel 写入器（直接调用模块自己的封装工具类）
     */
    public static <T> void exportWithProgress(ProgressNotifier notifier,
                                              String taskId,
                                              String filePrefix,
                                              String downloadUrlPrefix,
                                              LongSupplier countSupplier,
                                              Supplier<List<T>> dataSupplier,
                                              ExcelWriter<T> writer) {
        Path partPath = null;
        try {
            long total = countSupplier.getAsLong();
            String fileName = filePrefix + LocalDateTime.now().format(FILE_TIME_FORMAT) + ".xlsx";
            Path dir = getExportDir();
            partPath = dir.resolve(fileName + ".part");
            Path target = dir.resolve(fileName);

            notifier.send(ProgressMessage.start(taskId, "EXPORT", total, "开始导出，共 " + total + " 条数据"));

            // 查询全量数据（条件与列表一致）
            List<T> list = dataSupplier.get();

            // 调用各模块自己的封装导出工具类写入临时文件（样式与同步导出完全一致）
            try (OutputStream os = Files.newOutputStream(partPath)) {
                writer.write(os, list);
            }

            notifier.send(ProgressMessage.running(taskId, "EXPORT", total, list.size(),
                    "已导出 " + list.size() + " / " + total + " 条"));
            moveToFinal(partPath, target);

            String downloadUrl = downloadUrlPrefix + fileName;
            notifier.send(ProgressMessage.success(taskId, "EXPORT", total, list.size(),
                    "导出完成，共 " + list.size() + " 条", downloadUrl));
            log.info("进度导出完成 taskId={}, rows={}, file={}", taskId, list.size(), fileName);
        } catch (Exception e) {
            log.error("进度导出失败 taskId={}", taskId, e);
            notifier.send(ProgressMessage.failed(taskId, "EXPORT", "导出失败: " + e.getMessage()));
        } finally {
            if (partPath != null) {
                try {
                    Files.deleteIfExists(partPath);
                } catch (Exception ignored) {
                    // 忽略清理失败
                }
            }
        }
    }

    // ===================== 带进度条导入 =====================

    /**
     * 带 WebSocket 实时进度的异步导入（MultipartFile 版）：
     * 先转存为临时文件，再委托 {@link #importWithProgress(ProgressNotifier, String, File, Class, Consumer, int)} 执行。
     * 注意：仅适用于调用方线程内 MultipartFile 临时文件仍然有效（同步）的场景；
     * 异步线程（如 @Async）中必须先在请求线程内 transferTo 落盘，再调用 File 版。
     *
     * @param notifier   进度推送器
     * @param taskId     导入任务ID
     * @param file       上传的 Excel 文件
     * @param clazz      数据模型类（fastexcel 注解实体）
     * @param batchSaver 批次入库处理器（每批数据如何持久化，如 mapper.batchInsert）
     * @param batchSize  每批条数（如 500）
     * @return 导入结果（总数/成功/失败）
     */
    public static <T> ImportResult importWithProgress(ProgressNotifier notifier,
                                                      String taskId,
                                                      MultipartFile file,
                                                      Class<T> clazz,
                                                      Consumer<List<T>> batchSaver,
                                                      int batchSize) throws IOException {
        File temp = File.createTempFile("import_", ".xlsx");
        try {
            file.transferTo(temp);
            return importWithProgress(notifier, taskId, temp, clazz, batchSaver, batchSize);
        } finally {
            Files.deleteIfExists(temp.toPath());
        }
    }

    /**
     * 带 WebSocket 实时进度的异步导入（File 版，推荐异步场景使用）：
     * 第一遍解析统计总行数（保证百分比准确）→ 推送 start → 逐批读取并按批调用 batchSaver 入库
     * → 每批推送 running → 全部完成推送 success（含成功/失败条数）；异常推送 failed。
     *
     * <p>注意：会读两遍文件（第一遍计数、第二遍导入）。</p>
     *
     * @param notifier   进度推送器
     * @param taskId     导入任务ID
     * @param file       已落盘的 Excel 文件（调用方负责在完成后删除）
     * @param clazz      数据模型类（fastexcel 注解实体）
     * @param batchSaver 批次入库处理器（每批数据如何持久化，如 mapper.batchInsert）
     * @param batchSize  每批条数（如 500）
     * @return 导入结果（总数/成功/失败）
     */
    public static <T> ImportResult importWithProgress(ProgressNotifier notifier,
                                                      String taskId,
                                                      File file,
                                                      Class<T> clazz,
                                                      Consumer<List<T>> batchSaver,
                                                      int batchSize) throws IOException {
        long total = countRows(file, clazz);
        notifier.send(ProgressMessage.start(taskId, "IMPORT", total, "开始导入，共 " + total + " 条数据"));

        final long[] processed = {0};
        final long[] failed = {0};
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            FastExcel.read(inputStream, clazz, new AnalysisEventListener<T>() {
                private final List<T> cache = new ArrayList<>(batchSize);

                @Override
                public void invoke(T data, AnalysisContext context) {
                    cache.add(data);
                    if (cache.size() >= batchSize) {
                        flush();
                    }
                }

                private void flush() {
                    if (cache.isEmpty()) {
                        return;
                    }
                    List<T> batch = new ArrayList<>(cache);
                    try {
                        batchSaver.accept(batch);
                    } catch (Exception e) {
                        failed[0] += batch.size();
                        log.warn("导入批次失败：{}", e.getMessage(), e);
                    }
                    processed[0] += batch.size();
                    cache.clear();
                    notifier.send(ProgressMessage.running(taskId, "IMPORT", total, processed[0],
                            "已导入 " + processed[0] + " / " + total + " 条"));
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    flush();
                }
            }).sheet().doRead();
        } catch (Exception e) {
            notifier.send(ProgressMessage.failed(taskId, "IMPORT", "导入失败: " + e.getMessage()));
            throw new IOException("导入失败：" + e.getMessage(), e);
        }
        long success = processed[0] - failed[0];
        notifier.send(ProgressMessage.success(taskId, "IMPORT", total, processed[0],
                "导入完成，共 " + success + " 条成功" + (failed[0] > 0 ? "，" + failed[0] + " 条失败" : ""), null));
        return new ImportResult(total, success, failed[0]);
    }

    /** 第一遍解析：统计 Excel 总行数（不含表头） */
    private static long countRows(File file, Class<?> clazz) throws IOException {
        final long[] n = {0};
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            FastExcel.read(inputStream, clazz, new AnalysisEventListener<Object>() {
                @Override
                public void invoke(Object data, AnalysisContext context) {
                    n[0]++;
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 无需处理
                }
            }).sheet().doRead();
        } catch (Exception e) {
            throw new IOException("解析 Excel 失败：" + e.getMessage(), e);
        }
        return n[0];
    }

    // ===================== 下载封装 =====================

    /**
     * 下载异步导出的 Excel 文件（防路径穿越 + 扩展名白名单 + 中文展示名），供各模块 /download/{fileName} 复用。
     * 文件位于系统临时目录（见 {@link #getExportDir()}），输出到浏览器后立即删除服务器文件——
     * 导出的文件只保留在浏览器的下载路径，服务器不长期留存。
     *
     * @param response          HTTP 响应
     * @param fileName          请求的文件名，如 record_export_20260911103000.xlsx
     * @param filePrefix        文件名前缀（校验，防止下载非本模块文件），如 "record_export_"
     * @param displayNamePrefix 下载展示名前缀，如 "维护记录导出"（最终 维护记录导出_时间.xlsx）
     */
    public static void writeDownload(HttpServletResponse response,
                                     String fileName,
                                     String filePrefix,
                                     String displayNamePrefix) throws IOException {
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx") || !fileName.startsWith(filePrefix)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Path base = getExportDir();
        Path file = base.resolve(fileName).normalize();
        if (!file.startsWith(base) || !Files.exists(file)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String displayName = displayNamePrefix + fileName.substring(filePrefix.length());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encoded = URLEncoder.encode(displayName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        try {
            Files.copy(file, response.getOutputStream());
        } finally {
            // 输出完成后立即删除服务器临时文件，文件只保留在浏览器下载路径
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // 删除失败由系统临时目录后续清理
            }
        }
    }

    /** 将 .part 临时文件原子重命名为最终文件名；不支持原子移动时退回普通移动 */
    private static void moveToFinal(Path part, Path target) throws Exception {
        try {
            Files.move(part, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            Files.move(part, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
