package com.project.milo.utils;
import com.project.milo.common.AjaxResult;
import com.project.milo.utils.file.MiloConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

/**
 * 模板文件上传下载工具类
 *
 * @author yourname
 * @date 2026-08-28
 */
public class TemplateFileUtil {

    private static final Logger log = LoggerFactory.getLogger(TemplateFileUtil.class);

    /** 模板文件存放目录（位于上传根目录下） */
    private static final String TEMPLATE_PATH = "template/";

    /** 允许上传的文件类型 */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".doc", ".docx", ".xls", ".xlsx");

    /** 缓冲区大小 */
    private static final int BUFFER_SIZE = 4096;

    /**
     * 上传模板文件
     *
     * @param file 上传的文件
     * @param templateName 模板名称（不含扩展名）
     * @return AjaxResult 上传结果
     */
    public static AjaxResult uploadTemplate(MultipartFile file, String templateName) {
        try {
            // 1. 检查文件是否为空
            if (file == null || file.isEmpty()) {
                return AjaxResult.error("上传文件不能为空");
            }

            // 2. 获取文件信息
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return AjaxResult.error("文件名无效");
            }

            // 3. 验证文件类型
            String fileExtension = getFileExtension(originalFilename);
            if (!isValidFileType(fileExtension)) {
                return AjaxResult.error("只支持Word(.doc,.docx)和Excel(.xls,.xlsx)文件");
            }

            // 4. 构建保存路径
            String baseDir = MiloConfig.getProfile() + TEMPLATE_PATH;
            String saveFileName = templateName + fileExtension;
            File targetDir = new File(baseDir);

            // 5. 创建目录（如果不存在）
            if (!targetDir.exists()) {
                boolean created = targetDir.mkdirs();
                if (!created) {
                    return AjaxResult.error("创建目录失败：" + baseDir);
                }
            }

            // 6. 保存文件
            File targetFile = new File(baseDir + saveFileName);
            file.transferTo(targetFile);

            log.info("✅ 模板上传成功：{}，路径：{}", saveFileName, targetFile.getAbsolutePath());

            // 7. 返回成功信息
            AjaxResult result = AjaxResult.success("模板上传成功");
            result.put("fileName", saveFileName);
            result.put("filePath", targetFile.getAbsolutePath());
            return result;

        } catch (Exception e) {
            log.error("❌ 模板上传失败", e);
            return AjaxResult.error("模板上传失败：" + e.getMessage());
        }
    }

    /**
     * 下载模板文件
     *
     * @param templateName 模板名称（不含扩展名）
     * @param response HttpServletResponse对象
     * @return boolean 是否下载成功
     */
    public static AjaxResult downloadTemplate(String templateName, HttpServletResponse response) {
        FileInputStream fis = null;
        try {
            // 1. 构建文件路径
            String baseDir = MiloConfig.getProfile() + TEMPLATE_PATH;
            File templateDir = new File(baseDir);

            // 2. 检查目录是否存在
            if (!templateDir.exists()) {
                log.warn("模板目录不存在：{}", baseDir);
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "模板目录不存在");
                return AjaxResult.error("模板目录不存在");
            }

            // 3. 查找匹配的模板文件
            File[] files = templateDir.listFiles((dir, name) -> name.startsWith(templateName));
            if (files == null || files.length == 0) {
                log.warn("未找到模板文件：{}", templateName);
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "模板文件不存在");
                return AjaxResult.error("模板文件不存在");
            }

            // 4. 获取第一个匹配的文件
            File templateFile = files[0];
            String fileName = templateFile.getName();

            // 5. 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);

            // 6. 读取文件并输出
            fis = new FileInputStream(templateFile);
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, len);
            }
            response.getOutputStream().flush();

            log.info("✅ 模板下载成功：{}", fileName);
            return AjaxResult.success("模板下载成功");

        } catch (IOException e) {
            log.error("❌ 模板下载失败", e);
            try {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "下载失败：" + e.getMessage());
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
            return AjaxResult.error("下载失败：" + e.getMessage());
        } finally {
            closeStream(fis);
        }
    }

    /**
     * 获取文件的扩展名
     */
    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex).toLowerCase();
    }

    /**
     * 验证文件类型是否合法
     */
    private static boolean isValidFileType(String extension) {
        return extension != null && !extension.isEmpty() && ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * 发送错误响应
     */
    private static void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }

    /**
     * 关闭文件流
     */
    private static void closeStream(FileInputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                log.error("关闭文件流失败", e);
            }
        }
    }

    /**
     * 检查模板文件是否存在
     *
     * @param templateName 模板名称（不含扩展名）
     * @return boolean 是否存在
     */
    public static boolean templateExists(String templateName) {
        String baseDir = MiloConfig.getProfile() + TEMPLATE_PATH;
        File templateDir = new File(baseDir);
        if (!templateDir.exists()) {
            return false;
        }
        File[] files = templateDir.listFiles((dir, name) -> name.startsWith(templateName));
        return files != null && files.length > 0;
    }

    /**
     * 获取模板文件的完整路径
     *
     * @param templateName 模板名称（不含扩展名）
     * @return String 文件完整路径，如果不存在返回null
     */
    public static String getTemplatePath(String templateName) {
        String baseDir = MiloConfig.getProfile() + TEMPLATE_PATH;
        File templateDir = new File(baseDir);
        if (!templateDir.exists()) {
            return null;
        }
        File[] files = templateDir.listFiles((dir, name) -> name.startsWith(templateName));
        if (files == null || files.length == 0) {
            return null;
        }
        return files[0].getAbsolutePath();
    }

    /**
     * 删除模板文件
     *
     * @param templateName 模板名称（不含扩展名）
     * @return boolean 是否删除成功
     */
    public static boolean deleteTemplate(String templateName) {
        String baseDir = MiloConfig.getProfile() + TEMPLATE_PATH;
        File templateDir = new File(baseDir);
        if (!templateDir.exists()) {
            return false;
        }
        File[] files = templateDir.listFiles((dir, name) -> name.startsWith(templateName));
        if (files == null || files.length == 0) {
            return false;
        }
        boolean deleted = true;
        for (File file : files) {
            if (!file.delete()) {
                log.warn("删除模板文件失败：{}", file.getName());
                deleted = false;
            } else {
                log.info("删除模板文件成功：{}", file.getName());
            }
        }
        return deleted;
    }
}
