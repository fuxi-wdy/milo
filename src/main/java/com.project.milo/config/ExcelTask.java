package com.project.milo.config;

import lombok.Data;


@Data
public class ExcelTask {
    private String taskId;          // 任务唯一ID
    private String fileName;        // 文件名
    private String status;          // 状态: WAITING, RUNNING, SUCCESS, FAILED
    private Integer progress;       // 进度: 0-100
    private String downloadUrl;     // 下载链接
    private String errorMessage;    // 错误信息
    private Long userId;            // 所属用户
}