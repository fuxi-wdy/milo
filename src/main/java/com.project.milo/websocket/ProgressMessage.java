package com.project.milo.websocket;

/**
 * 通过 WebSocket 推送给前端的实时进度消息
 *
 * @param type   任务类型：IMPORT 导入 / EXPORT 导出 / SAMPLE 测试数据
 * @param status 状态：RUNNING 执行中 / SUCCESS 成功 / FAILED 失败
 */
public record ProgressMessage(
        String taskId,
        String type,
        long total,
        long processed,
        double percent,
        String status,
        String message,
        String downloadUrl) {

    public static ProgressMessage start(String taskId, String type, long total, String message) {
        return new ProgressMessage(taskId, type, total, 0, 0, "RUNNING", message, null);
    }

    public static ProgressMessage running(String taskId, String type, long total, long processed, String message) {
        double percent = total <= 0 ? 0 : Math.min(100.0, processed * 100.0 / total);
        return new ProgressMessage(taskId, type, total, processed, round1(percent), "RUNNING", message, null);
    }

    public static ProgressMessage success(String taskId, String type, long total, long processed, String message, String downloadUrl) {
        return new ProgressMessage(taskId, type, total, processed, 100, "SUCCESS", message, downloadUrl);
    }

    public static ProgressMessage failed(String taskId, String type, String message) {
        return new ProgressMessage(taskId, type, 0, 0, 0, "FAILED", message, null);
    }

    private static double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
